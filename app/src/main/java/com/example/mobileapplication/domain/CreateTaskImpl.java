package com.example.mobileapplication.domain;

import com.example.mobileapplication.data.TaskRepository;

public final class CreateTaskImpl implements CreateTask {
    private final TaskRepository repo;
    public CreateTaskImpl(TaskRepository repo){ this.repo = repo; }

    @Override public long handle(TaskModels.TaskDraft draft){
        return repo.create(draft);
    }
}
