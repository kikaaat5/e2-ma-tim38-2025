package com.example.mobileapplication.ui.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.dao.TaskDao;
import com.example.mobileapplication.data.models.TaskEntity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class TaskListViewModel extends AndroidViewModel {

    private final TaskDao taskDao;
    private final LiveData<List<TaskEntity>> tasks;

    private final LiveData<List<TaskEntity>> upcoming;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final String userId;


    public TaskListViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.get(application);
        userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        taskDao = db.taskDao();
        tasks = taskDao.getAll(userId);

        AppDatabase.exec(() -> {
            long limit = startOfDay(System.currentTimeMillis()) - 3L*24*60*60*1000; 
            AppDatabase.get(getApplication()).taskDao().sweepOverdueToNotDone(limit, userId);
        });


        MediatorLiveData<List<TaskEntity>> out = new MediatorLiveData<>();
        out.addSource(taskDao.getAll(userId), list -> out.setValue(filterUpcoming(list)));
        upcoming = out;
    }

    public LiveData<List<TaskEntity>> getUpcoming() { return upcoming; }

    private List<TaskEntity> filterUpcoming(List<TaskEntity> in){
        if (in == null) return List.of();
        long now = System.currentTimeMillis();
        List<TaskEntity> r = new ArrayList<>();
        for (TaskEntity t : in) {
            if (!"ACTIVE".equals(t.status)) continue;
            if ("ONE_TIME".equals(t.kind)) {
                if (t.scheduledAt != null && t.scheduledAt >= startOfToday(now)) {
                    r.add(t);
                }
            } else {
                long start = t.repeatStartAt != null ? t.repeatStartAt : now;
                Long end = t.repeatEndAt;
                if (startOfDay(now) >= start && (end == null || startOfDay(now) <= end)) {
                    r.add(t);
                }
            }
        }
        r.sort((a,b) -> Long.compare(nextTime(a, now), nextTime(b, now)));
        return r;
    }

    private long startOfToday(long ts){ return startOfDay(ts); }
    private long startOfDay(long ts){
        Calendar c = Calendar.getInstance(); c.setTimeInMillis(ts);
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0); c.set(Calendar.MILLISECOND,0);
        return c.getTimeInMillis();
    }

    private long nextTime(TaskEntity t, long now){
        if ("ONE_TIME".equals(t.kind) && t.scheduledAt != null) return t.scheduledAt;
        return startOfDay(now);
    }

    public void setStatus(long id, String status){ AppDatabase.exec(() -> taskDao.updateStatus(id, status, userId)); }


    public LiveData<List<TaskEntity>> getTasks() {
        return tasks;
    }
}
