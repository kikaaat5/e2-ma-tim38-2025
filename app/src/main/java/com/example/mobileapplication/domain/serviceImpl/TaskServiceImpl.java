package com.example.mobileapplication.domain.serviceImpl;

import com.example.mobileapplication.data.repository.TaskRepository;
import com.example.mobileapplication.data.models.TaskModels;
import com.example.mobileapplication.domain.services.ITaskService;

public final class TaskServiceImpl implements ITaskService {
    private final TaskRepository repo;
    public TaskServiceImpl(TaskRepository repo){ this.repo = repo; }

    @Override public long handle(TaskModels.TaskDraft draft){
        return repo.create(draft);
    }
}
