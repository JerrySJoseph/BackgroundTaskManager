package com.example.backgroundtask;

public class TaskExistsException extends IllegalStateException{

    public TaskExistsException(String message) {
        super(message);
    }

    public TaskExistsException() {
        super();
    }

    public TaskExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskExistsException(Throwable cause) {
        super(cause);
    }
}
