# Background Task Manager API for android :zap:

<!--- These are examples. See https://shields.io for others or to customize this set of shields. You might want to include dependencies, project status and licence info here --->
![Github versions](https://img.shields.io/github/v/release/jerrysjoseph/BackgroundTaskManager?include_prereleases)
![GitHub repo size](https://img.shields.io/github/repo-size/jerrysjoseph/BackgroundTaskManager)
![GitHub contributors](https://img.shields.io/github/contributors/jerrysjoseph/BackgroundTaskManager)
![GitHub stars](https://img.shields.io/github/stars/jerrysjoseph/BackgroundTaskManager?style=social)
![GitHub forks](https://img.shields.io/github/forks/jerrysjoseph/BackgroundTaskManager?style=social)

BackgroundTaskManager API provides the ability to chain multiple tasks into a singleton instance of backgroundTaskManager.It also includes the ability to process tasks SERIALLY ( tasks processed one after another) or in PARALLEL ( multiple tasks processed simultaneously).
This library handles thread processing as required and is optimised to handle tasks efficiently. Maximum PARALLEL tasks that can be executed simultaneously is computed and processed accordingly. 
If the TaskQueue exceeds the maximum available cores of a CPU, the all subsequent tasks are queued for execution. 
This also includes extended features invoking a callable function before or after completion of the tasks. This feature is wrapped in a familiar and easy method called before() and then().



## Prerequisites

Before you begin, ensure you have met the following requirements:
<!--- These are just example requirements. Add, duplicate or remove as required --->
* You have installed the latest version of Android Studio and gradle.
* uuhhmmmm...  nothing else..... :relaxed:

## Demo

### Parallel Processing

Tasks are processed simultaneously reducing the runtime.

![animation](gifs/backgroundTaskManager2.gif)

### Serial Processing

Tasks are processed one after other.

![animation](gifs/backgroundTaskManager3.gif)


## Usage

### Adding the Dependencies

Add this gradle dependecy to your app level build.gradle file

```gradle

    implementation 'com.github.JerrySJoseph:BackgroundTaskManager:v1.0.1-alpha'

```

Don't forget to add jitpack in your project level build.gradle file

```gradle
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

### Creating an instance of BackgroundTaskManager

```java
    BackgroundTaskManager.getInstance(BackgroundTaskType.PARALLEL_PROCESSING) //for running tasks simultaneously
    
                            OR
    
    BackgroundTaskManager.getInstance(BackgroundTaskType.SERIAL_PROCESSING)  // for runnning tasks one after other
```

### Adding tasks to TaskQueue and executing

BackgroundTaskManager provides add() method to add multiple tasks to the TaskQueue for execution. Here is an example of chaining these Tasks,

```java
    BackgroundTaskManager.getInstance(BackgroundTaskType.PARALLEL_PROCESSING)
                .add(bgTask)
                .execute();
```

add() method accepts runnable or an abstract implementation of BackgroundTask class provided in this library.

```java
   Runnable bgTask= new Runnable() {
                @Override
                public void run() {
                    //Some LONG Running background task
                }
            };
```
> NOTE: 
> Use Runnable when you do not require any result from the task. for eg: a backup operation, sending an status ping, etc..
> If you want a result returned from the task, you will have to implement BackgroundTask class (just like AsyncTask<Param,Progress,Result>)



### Chaining multiple tasks

You can add multiple tasks to the TaskQueue for execution. If the instance is created for SERIAL processing, all tasks will be executed one after other. On the other hand if the instance is of PARALLEL Processing, the library will calculate maxing possible simultaneous tasks that can be run maximizing the efficiency. All subsequent tasks will be queued and processed when other threads become idle.
Here is an example of how to chain multiple tasks,

```java
    BackgroundTaskManager.getInstance(BackgroundTaskType.PARALLEL_PROCESSING)
                    .add(bgTask1)
                    .add(bgTask2)
                    .add(bgTask3)
                    .add(bgTask4)
                    .add(bgTask5)
                    ....
                    .execute();
```

invoke execute() method to start the queue processing.

### Creating Custom Tasks

This library provides an abstract class BackgroundTask to implement your own custom task class. Below is an example demonstrating a custom class tailored to facilitate Download of a file from Network.

```java

/**
BackgroundTask<Result,Progress,Params> follows AsyncTask pattern to create custom task. 
Result -> Type of Result you are expecting from the Task ( String in this case)
Progress -> Type of progress update ( Integer in this case to denote % of download completed )
Params -> Type of the Parameters that we need to pass (String in this case for URL of file to be downloaded)

*/
   public class DownloadTask extends BackgroundTask<String,Integer,String>{
        
        String taskName;

        public DownloadTask(String taskName, String... strings) {
            super(strings);
            this.taskName = taskName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Invoked before starting execution of Queue
            //This is the block where you would want to show any type of dialog for progress update 
        }

        @Override
        protected void onResult(String s) {
           //Invoked when a result from this task is recieved.
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            //Invoked when this task gets cancelled anyhow.
        }

        @Override
        protected String doWork(String... strings) throws Exception {
            //Faking a long download progress
            //You can implement your own logic to fetch Data from Network
            for(int i=0;i<5;i++)
            {
                Thread.sleep(1000);
                //method to publish progress update
                publishProgress((i+1)*20);
            }
            return "complete";
        }

        @Override
        protected void onProgressUpdated(Integer integer) {
            super.onProgressUpdated(integer);
            //This is invoked when an update in progress is recieved.
        }

        @Override
        protected void onException(Exception exception) {
            //Invoked when an exception is occured while doing the task
        }

        @Override
        protected void onStatusChanged(Status s) {
            super.onStatusChanged(s);
            //Invoked when status of the task changes
            //Status available are : PENDING,RUNNING,FINISHED,CANCELLED,ERROR
        }

       


    }

```

After creating this custom class, we can chain objects of this task like

```java
    BackgroundTaskManager.getInstance(BackgroundTaskType.PARALLEL_PROCESSING)
                    .add(new DownloadTask("Download_Task_1","param1","param2","param3"))
                    .add(new DownloadTask("Download_Task_2","param1","param2","param3"))
                    .add(new DownloadTask("Download_Task_3","param1","param2","param3"))
                    .add(new DownloadTask("Download_Task_4","param1","param2","param3"))
                    .execute()
```
## Cancelling Tasks

Tasks which are referenced by ID can be cancelled individually. To cancel a task by its ID,

```java
    BackgroundTaskManager.cancelTask(taskID);   //Cancel the task with taskID if its not running.
                    ....
    BackgroundTaskManager.cancelTask(taskID,true); //Cancel the task with taskID even if its running.
```
Inorder for cancelling to work, you need to have the taskID for every process. Task ID can be fetched from BackgroundTaskManager by a static method getIDbyTask();

 ```java
 String taskID = BackgroundTaskManager.getIDbyTask(taskObject);
 ```
 Alternatively, we can pass custom taskID while adding tasks to Queues,
 
 ```java
 BackgroundTaskManager.getInstance(BackgroundTaskType.PARALLEL_PROCESSING)
                    .add("Download_Task_1",new DownloadTask("Download_Task_1","param1","param2","param3"))  //passing taskID
                    .add("Download_Task_2",new DownloadTask("Download_Task_2","param1","param2","param3"))  //passing taskID
                    .add("Download_Task_3",new DownloadTask("Download_Task_3","param1","param2","param3"))  //passing taskID
                    .add("Download_Task_4",new DownloadTask("Download_Task_4","param1","param2","param3"))  //passing taskID
                    .add("Download_Task_5",new DownloadTask("Download_Task_5","param1","param2","param3"))  //passing taskID
                    .add("Download_Task_6",new DownloadTask("Download_Task_6","param1","param2","param3"))  //passing taskID
                    .add("Download_Task_7",new DownloadTask("Download_Task_7","param1","param2","param3"))  //passing taskID
                    .add("Download_Task_8",new DownloadTask("Download_Task_8","param1","param2","param3"))  //passing taskID
                    .execute();
 ```
 
## Pausing, Resuming and Stopping execution

This library also provides methods to implement pause,resume and stop the entire execution. Below is an example,

```java
     
     //For pausing further execution of the Queue
     BackgroundTaskManager.pauseFurtherExecution();
     
     //For resuming the execution if paused
     BackgroundTaskManager.resumeExecution();
     
     //Stop entire execution , cancelling all tasks and reclamation of all resources
     BackgroundTaskManager.stopExecution();
```
 ## then() and before() 
 Often we need to implement some UI code before and after execution of the background task. To enable this, we have implemented then() and before() inspired by promises in Javascript. This is how we can impplement that,
 
 ```java
      BackgroundTaskManager.getInstance(BackgroundTaskType.PARALLEL_PROCESSING)
                    .before(new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            //any UI logic needs to be executed just before processing the queue. like opening a progress dialog.
                            Toast.makeText(getApplicationContext(),"Execution Started",Toast.LENGTH_SHORT).show();
                            return null;
                        }
                    })
                    .add("Download_Task_1",new DownloadTask("Download_Task_1","param1","param2","param3"))
                    ....
                    .add("Download_Task_8",new DownloadTask("Download_Task_8","param1","param2","param3"))
                    .then(new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            //any UI logic that needs to be executed after completion of the queue. like closing progress dialog.
                            Toast.makeText(getApplicationContext(),"Execution Complete",Toast.LENGTH_SHORT).show();
                            return null;
                        }
                    })
                    .execute();
 ```
## Execution Callbacks
Alternately, you can use AdvancedExecutorCallback for more control over executor events.
```java
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
```
Then, set the callback in executor instance,

```java
BackgroundTaskManager.getInstance(BackgroundTaskType.PARALLEL_PROCESSING)
                .add("Download_Task_1",new DownloadTask("Download_Task_1","param1","param2","param3"))
                ...
                .add("Download_Task_8",new DownloadTask("Download_Task_8","param1","param2","param3"))
                .setCallback(callback)
                .execute();
```
## Goals :fire:
- [x] Adding multiple _Runnable_ by chaining and execution in a single line.
- [x] Implement Serial and Parallel processing.
- [x] Adding multiple tasks which return any result.
- [x] Cancelling individual Tasks
- [x] Pausing and resuming execution
- [x] Handle errors and exception for individual tasks
- [x] Callbacks for Execution events like onExecutionBegin, onExecutionComplete, onExecutionCancell 
- [x] adding then() and before() which is invoked after and before execution.
- [ ] Implementing seperate optimised background tasks for processes like Networking, I/O operations.
- [ ] Introducing ability to set any task as daemon.


## Contributing to this project :boy:
<!--- If your README is long or you have some specific process or steps you want contributors to follow, consider creating a separate CONTRIBUTING.md file--->
To contribute to this project, follow these steps:

1. Fork this repository.
2. Create a branch: `git checkout -b <branch_name>`.
3. Make your changes and commit them: `git commit -m '<commit_message>'`
4. Push to the original branch: `git push origin <project_name>/<location>`
5. Create the pull request.

Alternatively see the GitHub documentation on [creating a pull request](https://help.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request).

## Contributors :boy:

Thanks to the following people who have contributed to this project:

* [@jerrysjoseph](https://github.com/JerrySJoseph) :memo: :computer:


## Contact :telephone:

If you want to contact me you can reach me at <jerin.sebastian153@gmail.com>.

## License
<!--- If you're not sure which open license to use see https://choosealicense.com/--->

This project uses the following license: [Apache-2.0](LICENSE.txt).
