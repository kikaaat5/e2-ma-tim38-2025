package com.example.mobileapplication.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.TaskDao;
import com.example.mobileapplication.data.TaskEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * ViewModel za listu zadataka.
 * Drži LiveData iz Room-a tako da Activity samo posmatra promjene.
 */
public class TaskListViewModel extends AndroidViewModel {

    private final TaskDao taskDao;
    private final LiveData<List<TaskEntity>> tasks;

    private final LiveData<List<TaskEntity>> upcoming;


    public TaskListViewModel(@NonNull Application application) {
        super(application);
        // Uzmi singleton baze i DAO
        AppDatabase db = AppDatabase.get(application);
        taskDao = db.taskDao();
        // LiveData lista svih zadataka (definiši getAll() u TaskDao)
        tasks = taskDao.getAll();

        MediatorLiveData<List<TaskEntity>> out = new MediatorLiveData<>();
        out.addSource(taskDao.getAll(), list -> out.setValue(filterUpcoming(list)));
        upcoming = out;
    }

    public LiveData<List<TaskEntity>> getUpcoming() { return upcoming; }

    private List<TaskEntity> filterUpcoming(List<TaskEntity> in){
        if (in == null) return List.of();
        long now = System.currentTimeMillis();
        List<TaskEntity> r = new ArrayList<>();
        for (TaskEntity t : in) {
            if (!"ACTIVE".equals(t.status)) continue; // u listi prikazujemo aktivne
            if ("ONE_TIME".equals(t.kind)) {
                if (t.scheduledAt != null && t.scheduledAt >= startOfToday(now)) {
                    r.add(t);
                }
            } else { // RECURRING
                long start = t.repeatStartAt != null ? t.repeatStartAt : now;
                Long end = t.repeatEndAt;
                if (startOfDay(now) >= start && (end == null || startOfDay(now) <= end)) {
                    r.add(t);
                }
            }
        }
        // Sortiraj po vremenu sljedećeg pojavljivanja
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
        // gruba procjena za recurring: danas 00:00 kao “slot”
        return startOfDay(now);
    }

    public void setStatus(long id, String status){ AppDatabase.exec(() -> taskDao.updateStatus(id, status)); }


    /** LiveData sa svim zadacima, sortiranim kako definiraš u DAO upitu. */
    public LiveData<List<TaskEntity>> getTasks() {
        return tasks;
    }
}
