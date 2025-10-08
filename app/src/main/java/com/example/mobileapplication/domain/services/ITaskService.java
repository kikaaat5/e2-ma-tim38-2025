package com.example.mobileapplication.domain.services;

import com.example.mobileapplication.data.models.TaskModels;

public interface ITaskService {
    long handle(TaskModels.TaskDraft draft);
}
