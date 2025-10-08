package com.example.mobileapplication.domain;

public interface CreateTask {
    long handle(TaskModels.TaskDraft draft);
}
