package com.example.mobileapplication.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.TaskEntity;
import com.example.mobileapplication.data.TaskRepository;
import com.example.mobileapplication.domain.CreateTask;
import com.example.mobileapplication.domain.CreateTaskImpl;
import com.example.mobileapplication.domain.TaskModels;

public class CreateTaskViewModel extends ViewModel {
    private final CreateTask createTask;
    private final AppDatabase db;
    public CreateTaskViewModel(CreateTask createTask, @NonNull Application app){ this.createTask = createTask;
        db = AppDatabase.get(app);}

    public long save(TaskModels.TaskDraft d){ return createTask.handle(d); }

    public long save(TaskModels.TaskDraft d, long editId){
        TaskEntity e = new TaskEntity();
        e.title = d.title;
        e.description = d.description;
        e.categoryId = d.categoryId;
        e.kind = (d.kind == TaskModels.TaskKind.RECURRING) ? "RECURRING" : "ONE_TIME";
        e.scheduledAt = (e.kind.equals("ONE_TIME")) ? d.scheduledAtEpochMillis : null;
        e.repeatEvery = (e.kind.equals("RECURRING")) ? d.repeatEvery : null;
        e.repeatUnit  = (e.kind.equals("RECURRING")) ? d.repeatUnit.name() : null;
        e.repeatStartAt = (e.kind.equals("RECURRING")) ? d.repeatStartEpochMillis : null;
        e.repeatEndAt   = (e.kind.equals("RECURRING")) ? d.repeatEndEpochMillis   : null;
        e.weightXp = (d.weight != null ? d.weight.xp : 0);
        e.importanceXp = (d.importance != null ? d.importance.xp : 0);
        e.totalXp = e.weightXp + e.importanceXp;

        if (editId > 0){
            e.id = editId; // bitno!
            AppDatabase.exec(() -> db.taskDao().updateCore(
                    e.id, e.title, e.description, e.categoryId,
                    e.kind, e.scheduledAt,
                    e.repeatEvery, e.repeatUnit, e.repeatStartAt, e.repeatEndAt,
                    e.weightXp, e.importanceXp, e.totalXp
            ));
            return editId;
        } else {
            return db.taskDao().insert(e);
        }
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application app;
        public Factory(Application app){ this.app = app; }

        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass){
            AppDatabase db = AppDatabase.get(app.getApplicationContext());
            TaskRepository repo = new TaskRepository(db.taskDao(), db.categoryDao());
            CreateTask uc = new CreateTaskImpl(repo);
            return (T) new CreateTaskViewModel(uc, app);
        }
    }
}
