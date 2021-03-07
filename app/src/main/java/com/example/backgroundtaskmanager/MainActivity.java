package com.example.backgroundtaskmanager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.backgroundtask.AdvancedExecutor;
import com.example.backgroundtask.BackgroundTask;
import com.example.backgroundtask.BackgroundTaskManager;
import com.example.backgroundtask.BackgroundTaskType;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BackgroundTaskManager.getInstance(BackgroundTaskType.PARALLEL_PROCESSING)
                .add("Download_Task_1",new DownloadTask("Download_Task_1","param1","param2","param3"))
                .add("Download_Task_2",new DownloadTask("Download_Task_2","param1","param2","param3"))
                .add("Download_Task_3",new DownloadTask("Download_Task_3","param1","param2","param3"))
                .add("Download_Task_4",new DownloadTask("Download_Task_4","param1","param2","param3"))
                .add("Download_Task_5",new DownloadTask("Download_Task_5","param1","param2","param3"))
                .add("Download_Task_6",new DownloadTask("Download_Task_6","param1","param2","param3"))
                .add("Download_Task_7",new DownloadTask("Download_Task_7","param1","param2","param3"))
                .add("Download_Task_8",new DownloadTask("Download_Task_8","param1","param2","param3"))
                .setCallback(callback)
                .execute();


    }
    AdvancedExecutor.AdvancedExecutorCallback callback = new AdvancedExecutor.AdvancedExecutorCallback() {
        @Override
        public void onExecutionBegin() {
            //Invoked just before execution starts
        }

        @Override
        public void onExecutionpaused() {
            //Invoked when execution paused
        }

        @Override
        public void onExecutionResumed() {
            //Invoked when execution resumes
        }

        @Override
        public void onExecutionComplete() {
            //Invoked when execution is complete
        }

        @Override
        public void onExecutionCancelled() {
            //Invoked when execution is cancelled
        }
    };
    private View inflateNewDownloadTask()
    {
        View view=LayoutInflater.from(this).inflate(R.layout.item_download_task,null);
        LinearLayout container=findViewById(R.id.container);
        container.addView(view);
        return view;
    }

    private class DownloadTask extends BackgroundTask<String,Integer,String>{

        LinearProgressIndicator progressIndicator;
        TextView status,title;
        ImageView cancel;
        String taskName;
        boolean hasException=false;

        public DownloadTask(String taskName, String... strings) {
            super(strings);
            this.taskName = taskName;
        }
        public DownloadTask(String taskName,boolean hasException, String... strings) {
            super(strings);
            this.taskName = taskName;
            this.hasException=hasException;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View root=inflateNewDownloadTask();
            progressIndicator=root.findViewById(R.id.pbar);
            status=root.findViewById(R.id.status);
            cancel=root.findViewById(R.id.cancel);
            title=root.findViewById(R.id.title);
            title.setText(taskName);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BackgroundTaskManager.cancelTask(taskName,true);
                }
            });
        }

        @Override
        protected void onResult(String s) {
            if(s!=null)
                progressIndicator.setIndicatorColor(getResources().getColor(android.R.color.holo_green_light));
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressIndicator.setIndicatorColor(getResources().getColor(android.R.color.holo_orange_light));
        }

        @Override
        protected String doWork(String... strings) throws Exception {
            for(int i=0;i<5;i++)
            {
                Thread.sleep(1000);
                publishProgress((i+1)*20);
                if(i==3 && hasException)
                    throw new Exception("Some fancy Exception while downloading the file");
            }

            return "complete";
        }

        @Override
        protected void onProgressUpdated(Integer integer) {
            super.onProgressUpdated(integer);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressIndicator.setProgress(integer,true);
            }
        }

        @Override
        protected void onException(Exception exception) {
            progressIndicator.setIndicatorColor(getResources().getColor(android.R.color.holo_red_light));
        }

        @Override
        protected void onStatusChanged(Status s) {
            super.onStatusChanged(s);
            status.setText(s.name());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DownloadTask that = (DownloadTask) o;
            return taskName.equals(that.taskName);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.cancel)
            BackgroundTaskManager.stopExecution();
        if(item.getItemId()==R.id.pauseResume)
        {
            if(BackgroundTaskManager.isExecutionPaused())
                BackgroundTaskManager.resumeExecution();
            else
                BackgroundTaskManager.pauseFurtherExecution();
        }

        return super.onOptionsItemSelected(item);
    }
}