/**
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


import android.os.Build;
import android.util.ArrayMap;

import androidx.annotation.MainThread;
import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
/**
 * BackgroundTaskManager provides the ability to chain multiple tasks into a singleton
 * instance of backgroundTaskManager. It also includes the ability to process tasks
 * SERIALLY ( tasks processed one after another) or in PARALLEL ( multiple tasks processed simultaneously).
 *
 * BakgroundTaskManager handles thread processing as required and is optimised to handle
 * tasks efficiently. Maximum PARALLEL tasks that can be executed simultaneously is computed and
 * processed accordingly. If the TaskQueue exceeds the maximum available cores of a CPU, the all subsequent
 * tasks are queued for execution.
 *
 * This class leverages a custom executor class extended from the base ThreadPoolExecutor with added ability
 * to PAUSE,RESUME or STOP the execution. These features are handled by static methods:
 *
 * BackgroundTaskManager.stopExecution() -> for pausing the further execution of task Queue
 * NOTE: Its important to note that this method should not be used to cancel individual Tasks, but all.
 *
 * BackgroundTaskManager.pauseFurtherExecution() -> for resuming the previously paused execution
 *
 * BackgroundTaskManager.resumeExecution() -> for resuming the previously paused execution
 *
 * BackgroundTaskManager.cancelTask(taskID) -> for cancelling individual Tasks by ID if not running
 * NOTE: This method will not interrupt a tasks if it is RUNNING/FINISHED.
 * It will only cancel tasks which are safe to cancel, i.e. PENDING Tasks
 *
 * BackgroundTaskManager.cancelTask(taskID, mayInterruptIfRunning) -> for cancelling individual Tasks by ID even if running
 * NOTE: This method will interrupt a tasks if it is RUNNING/FINISHED. Use this only if necessary.
 * It is not recommended to interrupt a running process amidst tasks especially if its an I/O operation
 *
 * For general operations of tasks, a singleton instance of the BackgroundTaskManager is created
 *
 *
* */
public class BackgroundTaskManager {

    //Singleton properties
    static BackgroundTaskManager mInstance;

    static Map<String,Runnable> backgroundTaskArrayMap;
    static Map<String,Future<?>> futureArrayMap;

    static AdvancedExecutor sDefaultExecutor;

    static BackgroundTaskType sDefaultbackgroundTaskType=BackgroundTaskType.PARALLEL_PROCESSING;

    Callable thenCallable,beforeCallable;
    Runnable thenRunnable,beforeRunnable;

    AdvancedExecutor.AdvancedExecutorCallback internalExecutorCallback= new AdvancedExecutor.AdvancedExecutorCallback() {
        @Override
        public void onExecutionBegin() {
            try
            {
                if(beforeRunnable!=null)
                    beforeRunnable.run();
                if(beforeCallable!=null)
                    beforeCallable.call();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onExecutionpaused() {

        }

        @Override
        public void onExecutionResumed() {

        }

        @Override
        public void onExecutionComplete() {
            try
            {
                if(thenRunnable!=null)
                    thenRunnable.run();
                if(thenCallable!=null)
                    thenCallable.call();
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        @Override
        public void onExecutionCancelled() {

        }
    };

    BackgroundTaskManager(BackgroundTaskType backgroundTaskType) {

        sDefaultExecutor= new AdvancedExecutor(backgroundTaskType);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            backgroundTaskArrayMap=new ArrayMap<>();
            futureArrayMap=new ArrayMap<>();
        }
        else
        {
            backgroundTaskArrayMap=new HashMap<>();
            futureArrayMap=new HashMap<>();
        }
        sDefaultbackgroundTaskType=backgroundTaskType;
        sDefaultExecutor.setInternalExecutorCallback(internalExecutorCallback);

    }

    public static synchronized BackgroundTaskManager getInstance(BackgroundTaskType backgroundTaskType){
        if(mInstance==null || sDefaultbackgroundTaskType!=backgroundTaskType)
            mInstance=new BackgroundTaskManager(backgroundTaskType);

        return mInstance;
    }

    public BackgroundTaskManager add(String taskId, BackgroundTask backgroundTask) {
        if(taskId!=null)
        {
            if(backgroundTaskArrayMap.containsKey(taskId))
                throw new TaskExistsException("Task with id: "+taskId+" already exists in the queue.");
            backgroundTaskArrayMap.put(taskId,backgroundTask);
            return this;
        }
       throw new IllegalArgumentException("Task ID cannot be null.");

    }

    public BackgroundTaskManager add(BackgroundTask backgroundTask) {
        return add(generateID(),backgroundTask);
    }


    public static String getIDbyTask(BackgroundTask task) {
        if(backgroundTaskArrayMap!=null)
           for(Map.Entry<String,Runnable>entry:backgroundTaskArrayMap.entrySet())
           {
               if(task.equals(entry.getValue()))
                   return entry.getKey();
           }
       return null;
    }

    public BackgroundTaskManager execute() {
        for(Map.Entry<String,Runnable> b:backgroundTaskArrayMap.entrySet())
        {
            futureArrayMap.put(b.getKey(),sDefaultExecutor.submit(b.getValue()));
        }
        return this;
    }

    public static boolean cancelTask(String taskId,boolean mayInterruptIfRunning) {
        if(futureArrayMap!=null && futureArrayMap.size()>0)
        {
            Future<?> taskFuture=futureArrayMap.get(taskId);
            if(taskFuture!=null) {
                boolean res=taskFuture.cancel(mayInterruptIfRunning);
                sDefaultExecutor.purge();
                return res;
            }
            throw new TaskNotFoundException("No tasks found with the specified Id");
        }
        throw new TaskNotFoundException("Attempting to cancel an empty Queue");

    }

    public void cancelTask(String taskId) {
        cancelTask(taskId,false);
    }

    public static void pauseFurtherExecution()
    {
        sDefaultExecutor.pause();
    }

    public static void resumeExecution()
    {
        sDefaultExecutor.resume();
    }

    public BackgroundTaskManager setCallback(AdvancedExecutor.AdvancedExecutorCallback advancedExecutorCallback) {
        sDefaultExecutor.setAdvancedExecutorCallback(advancedExecutorCallback);
        return this;
    }
    public static boolean isExecutionPaused() {
        return sDefaultExecutor.isPaused();
    }
    public static void stopExecution()
    {
        sDefaultExecutor.shutdownNow();
    }

    public <T> BackgroundTaskManager then(Callable<T> callable)
    {
        this.thenCallable=callable;
        return this;
    }
    public  BackgroundTaskManager then(Runnable runnable)
    {
        this.thenRunnable=runnable;
        return this;
    }
    public <T> BackgroundTaskManager before(Callable<T> callable)
    {
        this.beforeCallable=callable;
        return this;
    }
    public BackgroundTaskManager before(Runnable runnable)
    {
        this.beforeRunnable=runnable;
        return this;
    }

    private String generateID()
    {
        return UUID.randomUUID().toString();
    }

}
