# Background Task Manager API for android

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
* uuhhmmmm...  nothing else.....

## Demo

### Parallel Processing

![animation](gifs/backgroundTaskManager2.gif)

### Serial Processing

![animation](gifs/backgroundTaskManager2.gif)


## Usage

### Adding the Dependencies

```gradle

    implementation 'com.github.JerrySJoseph:BackgroundTaskManager:v1.0.1-alpha'

```

Don't forget to add jitpack in your project level repositories

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

execute method is called to start the queue processing.

### Creating Tasks



BackgroundTaskManager provides add() method to add multiple tasks to the TaskQueue for execution. Here is an example of chaining these Tasks,

```java
    BackgroundTaskManager.getInstance(BackgroundTaskType.PARALLEL_PROCESSING)
                    .add("Download_Task_1",new DownloadTask("Download_Task_1","param1","param2","param3"))
                    .add("Download_Task_2",new DownloadTask("Download_Task_2","param1","param2","param3"))
                    .add("Download_Task_3",new DownloadTask("Download_Task_3","param1","param2","param3"))
                    .add("Download_Task_4",new DownloadTask("Download_Task_4","param1","param2","param3"))
                    .add("Download_Task_5",new DownloadTask("Download_Task_5","param1","param2","param3"))
                    .add("Download_Task_6",new DownloadTask("Download_Task_6","param1","param2","param3"))
                    .add("Download_Task_7",new DownloadTask("Download_Task_7","param1","param2","param3"))
                    .add("Download_Task_8",new DownloadTask("Download_Task_8","param1","param2","param3"))
                    .execute();
```

execute method is called to start the queue processing.

## Goals
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


## Contributing to this project
<!--- If your README is long or you have some specific process or steps you want contributors to follow, consider creating a separate CONTRIBUTING.md file--->
To contribute to this project, follow these steps:

1. Fork this repository.
2. Create a branch: `git checkout -b <branch_name>`.
3. Make your changes and commit them: `git commit -m '<commit_message>'`
4. Push to the original branch: `git push origin <project_name>/<location>`
5. Create the pull request.

Alternatively see the GitHub documentation on [creating a pull request](https://help.github.com/en/github/collaborating-with-issues-and-pull-requests/creating-a-pull-request).

## Contributors

Thanks to the following people who have contributed to this project:

* [@jerrysjoseph](https://github.com/JerrySJoseph) 📖

You might want to consider using something like the [All Contributors](https://github.com/all-contributors/all-contributors) specification and its [emoji key](https://allcontributors.org/docs/en/emoji-key).

## Contact

If you want to contact me you can reach me at <jerin.sebastian153@gmail.com>.

## License
<!--- If you're not sure which open license to use see https://choosealicense.com/--->

This project uses the following license: [<license_name>](<link>).
