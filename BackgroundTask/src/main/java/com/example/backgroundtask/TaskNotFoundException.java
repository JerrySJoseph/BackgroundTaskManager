package com.example.backgroundtask;

public class TaskNotFoundException extends IllegalStateException {

    public TaskNotFoundException(String s) {
        super(s);
    }

    public TaskNotFoundException() {
        super();
    }

    public TaskNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskNotFoundException(Throwable cause) {
        super(cause);
    }
}
