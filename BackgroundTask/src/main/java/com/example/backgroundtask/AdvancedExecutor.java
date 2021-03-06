/*
 *
 *  * Copyright (C) 2021 Jerry S Joseph
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.example.backgroundtask;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class AdvancedExecutor extends ThreadPoolExecutor {

    String TAG="Advanced Executor";
    private static final int MAXIMUM_POOL_SIZE = 20;
    private static final int BACKUP_POOL_SIZE = 10;
    private static final int KEEP_ALIVE_SECONDS = 3;

    Handler handler;

    static LinkedBlockingQueue<Runnable> rejectedbackgroundTasks;

    private boolean isPaused;
    private ReentrantLock pauseLock = new ReentrantLock();
    private Condition unpaused = pauseLock.newCondition();

    AtomicBoolean onStarFired=new AtomicBoolean(false);

    AdvancedExecutorCallback advancedExecutorCallback,internalExecutorCallback;

    public void setAdvancedExecutorCallback(AdvancedExecutorCallback advancedExecutorCallback) {
        this.advancedExecutorCallback = advancedExecutorCallback;
    }

    public void setInternalExecutorCallback(AdvancedExecutorCallback internalExecutorCallback) {
        this.internalExecutorCallback = internalExecutorCallback;
    }

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        public Thread newThread(Runnable r) {
            return new Thread(r, "Backgroundtask #" + mCount.getAndIncrement());
        }
    };

    private static final RejectedExecutionHandler sRejectedExecutionHandler=new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            rejectedbackgroundTasks.add(r);
        }
    };

    public boolean isPaused() {
        return isPaused;
    }

    public AdvancedExecutor(BackgroundTaskType taskType) {
        super(taskType==BackgroundTaskType.SERIAL_PROCESSING?1:Runtime.getRuntime().availableProcessors(),
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                sThreadFactory,
                sRejectedExecutionHandler);
        handler=getMainHandler();
        rejectedbackgroundTasks=new LinkedBlockingQueue<>();
    }

    private Handler getMainHandler()
    {
        return new Handler(Looper.getMainLooper());
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        if(getCompletedTaskCount()==0 && !onStarFired.get())
        {
            postPreExecute();
            onStarFired.set(true);
        }

        pauseLock.lock();
        try {
            while (isPaused)
                unpaused.await();
        } catch (InterruptedException ie) {
            t.interrupt();
        } finally {
            pauseLock.unlock();
        }
    }

    @Override
    public void execute(Runnable command) {
        super.execute(command);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        Log.e(TAG,"completed :"+getCompletedTaskCount());
        if(getCompletedTaskCount()==getTaskCount()-1)
            postPostExecute();
    }

    @Override
    public List<Runnable> shutdownNow() {

        postCancelled();
        return super.shutdownNow();
    }


    public void pause() {
        pauseLock.lock();
        try {
            isPaused = true;
        } finally {
            postPause();
            pauseLock.unlock();
        }
    }

    public void resume() {
        pauseLock.lock();
        try {
            isPaused = false;
            unpaused.signalAll();
        } finally {
            postResume();
            pauseLock.unlock();
        }
    }

    public void postPostExecute(){
     handler.post(new Runnable() {
         @Override
         public void run() {
            if(advancedExecutorCallback!=null)
                advancedExecutorCallback.onExecutionComplete();
            if(internalExecutorCallback!=null)
                internalExecutorCallback.onExecutionComplete();
         }
     });
    }
    public void postCancelled() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(advancedExecutorCallback!=null)
                    advancedExecutorCallback.onExecutionCancelled();
                if(internalExecutorCallback!=null)
                    internalExecutorCallback.onExecutionCancelled();
            }
        });
    }
    public void postPreExecute() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(advancedExecutorCallback!=null)
                    advancedExecutorCallback.onExecutionBegin();
                if(internalExecutorCallback!=null)
                    internalExecutorCallback.onExecutionBegin();
            }
        });
    }
    public void postPause() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(advancedExecutorCallback!=null)
                    advancedExecutorCallback.onExecutionpaused();
                if(internalExecutorCallback!=null)
                    internalExecutorCallback.onExecutionpaused();
            }
        });
    }
    public void postResume() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(advancedExecutorCallback!=null)
                    advancedExecutorCallback.onExecutionResumed();
                if(internalExecutorCallback!=null)
                    internalExecutorCallback.onExecutionResumed();
            }
        });
    }
    public interface AdvancedExecutorCallback {
        void onExecutionBegin();
        void onExecutionpaused();
        void onExecutionResumed();
        void onExecutionComplete();
        void onExecutionCancelled();
    }
}
