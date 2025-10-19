package com.example.mobileapplication.data.repository;

import androidx.lifecycle.LiveData;

import com.example.mobileapplication.data.models.TaskEntity;
import com.example.mobileapplication.data.dao.CategoryDao;
import com.example.mobileapplication.data.dao.TaskDao;
import com.example.mobileapplication.data.models.TaskModels;

import java.util.List;

public final class TaskRepository {
    private final TaskDao taskDao;
    private final CategoryDao catDao;

    public TaskRepository(TaskDao taskDao, CategoryDao catDao){
        this.taskDao = taskDao; this.catDao = catDao;
    }

    public long create(TaskModels.TaskDraft d){
        validate(d);
        TaskEntity e = toEntity(d);
        return taskDao.insert(e);
    }

    private void validate(TaskModels.TaskDraft d){
        if (d.title == null || d.title.trim().isEmpty())
            throw new IllegalArgumentException("Naziv je obavezan");

        if (d.weight == null || d.importance == null)
            throw new IllegalArgumentException("Težina i bitnost su obavezni");

        if (d.kind == TaskModels.TaskKind.ONE_TIME) {
            if (d.scheduledAtEpochMillis <= 0)
                throw new IllegalArgumentException("Vreme izvršenja je obavezno");
        } else {
            if (d.repeatEvery == null || d.repeatEvery < 1)
                throw new IllegalArgumentException("Interval ponavljanja je obavezan");
            if (d.repeatUnit == null)
                throw new IllegalArgumentException("Jedinica ponavljanja je obavezna");
            if (d.repeatStartEpochMillis == null || d.repeatEndEpochMillis == null
                    || d.repeatEndEpochMillis < d.repeatStartEpochMillis)
                throw new IllegalArgumentException("Neispravan opseg datuma");
        }
    }

    public LiveData<List<TaskEntity>> getAll(){
        return taskDao.getAll();
    }

    private TaskEntity toEntity(TaskModels.TaskDraft d){
        TaskEntity e = new TaskEntity();
        e.title = d.title.trim();
        e.description = d.description;
        e.categoryId = d.categoryId;

        e.kind = d.kind.name();
        e.scheduledAt = (d.kind== TaskModels.TaskKind.ONE_TIME) ? d.scheduledAtEpochMillis : null;

        e.repeatEvery   = (d.kind== TaskModels.TaskKind.RECURRING) ? d.repeatEvery : null;
        e.repeatUnit    = (d.kind== TaskModels.TaskKind.RECURRING) ? d.repeatUnit.name() : null;
        e.repeatStartAt = (d.kind== TaskModels.TaskKind.RECURRING) ? d.repeatStartEpochMillis : null;
        e.repeatEndAt   = (d.kind== TaskModels.TaskKind.RECURRING) ? d.repeatEndEpochMillis   : null;

        e.weightXp = d.weight.xp;
        e.importanceXp = d.importance.xp;
        e.totalXp = d.valueXp();
        e.createdAt = System.currentTimeMillis();
        return e;
    }
}
