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

public class BackgroundTaskManager {

    //Singleton properties
    static BackgroundTaskManager mInstance;

    static Map<String,BackgroundTask> backgroundTaskArrayMap;
    static Map<String,Future<?>> futureArrayMap;

    static AdvancedExecutor sDefaultExecutor;


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

    }

    public static synchronized BackgroundTaskManager getInstance(BackgroundTaskType backgroundTaskType){
        if(mInstance==null)
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

    public BackgroundTaskManager add( BackgroundTask backgroundTask) {
        return add(generateID(),backgroundTask);
    }


    public static String getIDbyTask(BackgroundTask task) {
        if(backgroundTaskArrayMap!=null)
           for(Map.Entry<String,BackgroundTask>entry:backgroundTaskArrayMap.entrySet())
           {
               if(task.equals(entry.getValue()))
                   return entry.getKey();
           }
       return null;
    }

    public BackgroundTaskManager execute() {
        for(Map.Entry<String,BackgroundTask> b:backgroundTaskArrayMap.entrySet())
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

    public BackgroundTaskManager setCallback(AdvancedExecutor.AdvancedExecutorCallback advancedExecutorCallback)
    {
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
    private String generateID()
    {
        return UUID.randomUUID().toString();
    }

}
