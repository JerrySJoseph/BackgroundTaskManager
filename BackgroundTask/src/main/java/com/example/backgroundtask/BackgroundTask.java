package com.example.backgroundtask;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public abstract class BackgroundTask<Result,Progress,Params> implements Runnable{

    String TAG="Background Task";

    public enum Status{
        PENDING,
        RUNNING,
        FINISHED,
        CANCELLED,
        ERROR
    }

    Handler mHandler;

    Params[] params;

    Status status= Status.PENDING;

    public BackgroundTask(Params... params) {
        this.params = params;
        mHandler=getMainHandler();
    }

    public BackgroundTask(Handler handler, Params... params) {
        this.params = params;
        this.mHandler=handler;
    }

    public void setParams(Params... params)
    {
        this.params=params;
    }

    @Override
    public void run(){
        Result result=null;

        try{
            if(status!= Status.PENDING)
            {
                String exceptionMsg=status== Status.RUNNING?
                        "Attempting to start a running process":
                        "Attempting to start a finished process";
                throw new IllegalStateException(exceptionMsg);
            }
            Log.d(TAG,Thread.currentThread().getName());

            postPreExecute();
            result= doWork(params);
            postResult(result);
        } catch (InterruptedException e) {
            postCancelled();
        } catch (ExecutionException e) {
            throw new RuntimeException("An error occurred while executing doInBackground()", e.getCause());
        } catch (CancellationException e) {
            postResult(null);
        } catch (Exception e) {
            postException(e);
        }

    }

    @UiThread
    protected abstract void onResult(Result result);

    @WorkerThread
    protected abstract Result doWork(Params... params) throws Exception;

    protected void onStatusChanged(Status status){}

    protected void onPreExecute(){ }

    protected void onCancelled(){}

    @UiThread
    protected void onProgressUpdated(Progress progress){ }

    protected void publishProgress(Progress progress){
       postProgress(progress);
    }

    @UiThread
    protected abstract void onException(Exception exception);

    private Handler getMainHandler()
    {
        return new Handler(Looper.getMainLooper());
    }

    private void postResult(Result result)
    {
        status= Status.FINISHED;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onResult(result);
                onStatusChanged(status);
            }
        });
    }
    private void postProgress(Progress progress)
    {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onProgressUpdated(progress);
            }
        });
    }
    private void postException(Exception e)
    {
        status= Status.ERROR;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onException(e);
                onStatusChanged(status);
            }
        });
    }
    private void postPreExecute()
    {
        status= Status.RUNNING;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onPreExecute();
                onStatusChanged(status);
            }
        });
    }
    private void postCancelled()
    {
        status= Status.CANCELLED;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onCancelled();
                onStatusChanged(status);
            }
        });
    }

    @Override
    public String toString() {
        return "BackgroundTask{"+Thread.currentThread().getName()+"}";
    }
}
