package com.example.mobileapplication.ui.viewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.models.TaskEntity;
import com.example.mobileapplication.data.repository.TaskRepository;
import com.example.mobileapplication.domain.services.ITaskService;
import com.example.mobileapplication.domain.serviceImpl.TaskServiceImpl;
import com.example.mobileapplication.data.models.TaskModels;

public class CreateTaskViewModel extends ViewModel {
    private final ITaskService createTask;
    private final AppDatabase db;
    public CreateTaskViewModel(ITaskService createTask, @NonNull Application app){
        this.createTask = createTask;
        db = AppDatabase.get(app);}

    public long save(TaskModels.TaskDraft d){ return createTask.handle(d); }

    public long save(TaskModels.TaskDraft d, long editId) {
        TaskEntity e = new TaskEntity();
        var dao = db.taskDao();

        e.title = d.title;
        e.description = d.description;
        e.categoryId = d.categoryId;
        e.kind = (d.kind == TaskModels.TaskKind.RECURRING) ? "RECURRING" : "ONE_TIME";
        e.scheduledAt = (e.kind.equals("ONE_TIME")) ? d.scheduledAtEpochMillis : null;
        e.repeatEvery = (e.kind.equals("RECURRING")) ? d.repeatEvery : null;
        e.repeatUnit = (e.kind.equals("RECURRING")) ? d.repeatUnit.name() : null;
        e.repeatStartAt = (e.kind.equals("RECURRING")) ? d.repeatStartEpochMillis : null;
        e.repeatEndAt = (e.kind.equals("RECURRING")) ? d.repeatEndEpochMillis : null;
        e.weightXp = (d.weight != null ? d.weight.xp : 0);
        e.importanceXp = (d.importance != null ? d.importance.xp : 0);
        e.totalXp = e.weightXp + e.importanceXp;
        if (editId>0) {
            e.id=editId;
            if ("ONE_TIME".equals(e.kind)) {
                long now = System.currentTimeMillis();
                dao.updateOneTime(
                        e.id, e.title, e.description, e.weightXp, e.importanceXp, e.totalXp,
                        e.scheduledAt, now
                );
            } else {
                long nowStart = startOfToday();
                dao.updateRecurring(
                        e.id, e.title, e.description,
                        e.weightXp, e.importanceXp, e.totalXp,
                        e.repeatEvery, e.repeatUnit, e.repeatStartAt, e.repeatEndAt, nowStart
                );
            }
            return editId;
        } else {
            e.createdAt = System.currentTimeMillis();
            e.status = "ACTIVE";
            return dao.insert(e);
        }

    }



    private long startOfToday(){
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(java.util.Calendar.HOUR_OF_DAY,0);
        c.set(java.util.Calendar.MINUTE,0);
        c.set(java.util.Calendar.SECOND,0);
        c.set(java.util.Calendar.MILLISECOND,0);
        return c.getTimeInMillis();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application app;
        public Factory(Application app){ this.app = app; }

        @NonNull @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass){
            AppDatabase db = AppDatabase.get(app.getApplicationContext());
            TaskRepository repo = new TaskRepository(db.taskDao(), db.categoryDao());
            ITaskService uc = new TaskServiceImpl(repo);
            return (T) new CreateTaskViewModel(uc, app);
        }
    }
}
