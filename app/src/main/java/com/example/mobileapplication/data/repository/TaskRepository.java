package com.example.mobileapplication.data.repository;

import androidx.lifecycle.LiveData;
import android.util.Log;

import com.example.mobileapplication.data.models.CategoryEntity;
import com.example.mobileapplication.data.models.TaskEntity;
import com.example.mobileapplication.data.dao.CategoryDao;
import com.example.mobileapplication.data.dao.TaskDao;
import com.example.mobileapplication.data.models.TaskModels;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public final class TaskRepository {
    private final TaskDao taskDao;
    private final CategoryDao catDao;

    // 🔹 Dodaj Firebase instance
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public TaskRepository(TaskDao taskDao, CategoryDao catDao){
        this.taskDao = taskDao;
        this.catDao = catDao;
    }

    public long create(TaskModels.TaskDraft d){
        validate(d);
        TaskEntity e = toEntity(d);
        long newId = taskDao.insert(e);

//         MOCKED DATA: ostaviti do odbrane
//        if (auth.getCurrentUser() != null) {
//            String uid = auth.getCurrentUser().getUid();
//            Map<String, Object> firebaseTask = new HashMap<>();
//            firebaseTask.put("id", newId);
//            firebaseTask.put("userId", uid);
//            firebaseTask.put("title", e.title);
//            firebaseTask.put("description", e.description);
//            firebaseTask.put("categoryId", e.categoryId);
//            firebaseTask.put("kind", e.kind);
//            firebaseTask.put("status", e.status);
//            firebaseTask.put("totalXp", e.totalXp);
//            firebaseTask.put("createdAt", e.createdAt);
//
//            firestore.collection("tasks")
//                    .document(uid + "_" + newId)
//                    .set(firebaseTask)
//                    .addOnSuccessListener(aVoid ->
//                            Log.d("FirebaseTask", " Task sinhronizovan u Firestore"))
//                    .addOnFailureListener(e1 ->
//                            Log.e("FirebaseTask", " Greška pri slanju u Firestore: " + e1.getMessage()));
//        }

        return newId;
    }

    private void validate(TaskModels.TaskDraft d){
        if (d.title == null || d.title.trim().isEmpty())
            throw new IllegalArgumentException("Naziv je obavezan");
        if (catDao.exists(d.categoryId)==0)
            throw new IllegalArgumentException("Kategorija ne postoji");

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

    public List<CategoryEntity> getCategoriesWithTaskCount() {
        List<CategoryEntity> categories = catDao.getAllCategoriesSync();
        List<TaskEntity> allTasks = taskDao.getAllTasksSync();

        Map<Long, Integer> countMap = new HashMap<>();
        for (TaskEntity task : allTasks) {
            countMap.put(task.categoryId, countMap.getOrDefault(task.categoryId, 0) + 1);
        }

        for (CategoryEntity c : categories) {
            c.taskCount = countMap.getOrDefault(c.id, 0);
        }
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();

            for (CategoryEntity c : categories) {
                Map<String, Object> data = new HashMap<>();
                data.put("name", c.name);
                data.put("taskCount", c.taskCount);
                data.put("colorHex", c.colorHex);

                firestore.collection("categories_stats")
                        .document(uid + "_" + c.id)
                        .set(data)
                        .addOnSuccessListener(aVoid ->
                                Log.d("FirebaseCategory", "" + c.name + " sinhronizovana"))
                        .addOnFailureListener(e ->
                                Log.e("FirebaseCategory", " Greška za " + c.name + ": " + e.getMessage()));
            }
        }


        return categories;
    }

}
