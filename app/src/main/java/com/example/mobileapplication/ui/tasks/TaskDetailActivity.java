package com.example.mobileapplication.ui.tasks;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.models.CategoryEntity;
import com.example.mobileapplication.data.dao.TaskDao;
import com.example.mobileapplication.data.models.TaskEntity;
import com.example.mobileapplication.databinding.ActivityTaskDetailBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaskDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "task_id";

    private ActivityTaskDetailBinding b;
    private TaskDao dao;
    private long taskId;

    @Nullable private TaskEntity current;
    private final Map<Long, CategoryEntity> catsById = new HashMap<>();

    public static void start(AppCompatActivity a, long id){
        Intent i = new Intent(a, TaskDetailActivity.class);
        i.putExtra(EXTRA_ID, id);
        a.startActivity(i);
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityTaskDetailBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setTitle("Zadatak");

        dao = AppDatabase.get(this).taskDao();
        taskId = getIntent().getLongExtra(EXTRA_ID, -1);
        if (taskId <= 0) { finish(); return; }


        dao.byId(taskId).observe(this, t -> {
            current = t;
            if (t != null) bind(t, catsById.get(t.categoryId));
        });


        AppDatabase.get(this).categoryDao().all().observe(this, list -> {
            catsById.clear();
            if (list != null) for (CategoryEntity c : list) catsById.put(c.id, c);
            if (current != null) bind(current, catsById.get(current.categoryId));
        });


        b.btnDone.setOnClickListener(v -> onDone());
        b.btnPause.setOnClickListener(v -> onpause());
        b.btnCancel.setOnClickListener(v -> onCancel());
        b.btnActivate.setOnClickListener(v -> onActivate());


        b.btnEdit.setOnClickListener(v -> {
            if (current == null) return;
            if ("DONE".equals(current.status)) { toast("Završene nije moguće menjati."); return; }
            CreateTaskActivity.startEdit(this, current.id);
        });

        b.btnDelete.setOnClickListener(v -> {
            if (current == null) return;
            if ("DONE".equals(current.status)) { toast("Završene nije moguće obrisati."); return; }
            new AlertDialog.Builder(this)
                    .setTitle("Obriši zadatak?")
                    .setMessage("Za ponavljajući, biće uklonjena SVA buduća pojavljivanja.")
                    .setPositiveButton("Obriši", (d,w) -> deleteTask(current))
                    .setNegativeButton("Otkaži", null)
                    .show();
        });
    }



    private void bind(TaskEntity t, @Nullable CategoryEntity cat) {

        b.tvTitle.setText(t.title == null ? "" : t.title);
        b.tvDesc.setText(t.description == null ? "" : t.description);


        String catName = (cat != null && cat.name != null) ? cat.name : ("Kategorija #" + t.categoryId);
        b.chCategory.setText(catName);
        int color = safeCatColor(cat, t.categoryId);
        b.chCategory.setChipBackgroundColor(ColorStateList.valueOf(color));


        b.chKind.setText(t.kind == null ? "ONE_TIME" : t.kind);
        b.tvWhen.setText(buildWhenText(t));


        int xp = (t.totalXp != 0 ? t.totalXp : (t.weightXp + t.importanceXp));
        b.tvXp.setText(xp + " XP");


        renderButtons();
    }

    private void renderButtons(){
        if (current == null) return;
        boolean isRecurring = "RECURRING".equals(current.kind);
        String st = current.status;

        boolean canResolve = "ACTIVE".equals(st);
        b.btnDone.setEnabled(canResolve && "ONE_TIME".equals(current.kind));
        b.btnCancel.setEnabled(canResolve);

        b.btnPause.setEnabled(isRecurring && "ACTIVE".equals(st));
        b.btnActivate.setEnabled(isRecurring && "PAUSED".equals(st));

        boolean canModify = current != null && !"DONE".equals(current.status);
        b.btnEdit.setEnabled(canModify);
        b.btnDelete.setEnabled(canModify);
    }

    private int safeCatColor(@Nullable CategoryEntity c, long id){
        try {
            if (c != null && c.colorHex != null && !c.colorHex.isEmpty()){
                return android.graphics.Color.parseColor(c.colorHex);
            }
        } catch (Exception ignore) {}
        int[] palette = {
                0xFFE57373, 0xFF64B5F6, 0xFF81C784, 0xFFFFB74D,
                0xFFBA68C8, 0xFFFF8A65, 0xFF4DB6AC, 0xFFA1887F
        };
        return palette[(int)(Math.abs(id) % palette.length)];
    }

    private String buildWhenText(TaskEntity t){
        if ("ONE_TIME".equals(t.kind)){
            return fmtDateTime(t.scheduledAt);
        }
        String unit = "WEEK".equals(t.repeatUnit) ? "nedelja" : "dan";
        int every = t.repeatEvery == null ? 0 : t.repeatEvery;
        String start = fmtDate(t.repeatStartAt);
        String end   = fmtDate(t.repeatEndAt);
        return "svakih " + every + " " + unit +
                (start.isEmpty() ? "" : " · od " + start) +
                (end.isEmpty()   ? "" : " do " + end);
    }



    private void updateStatus(String status){
        AppDatabase.exec(() -> {
            dao.updateStatus(taskId, status);
            runOnUiThread(() -> {
                toast("Status: " + status);
                finish();
            });
        });
    }

    private void onDone() {
        if (current == null) return;
        long now = System.currentTimeMillis();
        long threeDaysAgo = now - 3L*24*60*60*1000;

        AppDatabase.exec(() -> {
            int rows;
            if ("ONE_TIME".equals(current.kind)) {
                rows = dao.markDoneOneTime(current.id, now, threeDaysAgo);
            } else {

                rows = 0;
            }
            runOnUiThread(() -> {
                if (rows > 0) {
                    awardXp(xpFor(current));
                    toast("Označeno kao urađeno (+XP)");
                    finish();
                } else {
                    toast("Ne može: zadatak je u budućnosti ili stariji od 3 dana, ili nije ACTIVE.");
                }
            });
        });
    }

    private void onCancel() {
        if (current == null) return;
        AppDatabase.exec(() -> {
            int rows = dao.markCanceled(current.id);
            runOnUiThread(() -> {
                if (rows > 0) { toast("Otkazano."); finish(); }
                else toast("Ne može: zadatak nije ACTIVE.");
            });
        });
    }

    private void onpause() {
        if (current == null) return;
        AppDatabase.exec(() -> {
            int rows = dao.pauseRecurring(current.id);
            runOnUiThread(() -> {
                if (rows > 0) { toast("Pauzirano."); finish(); }
                else toast("Ne može: samo za aktivne ponavljajuće zadatke.");
            });
        });
    }

    private void onActivate() {
        if (current == null) return;
        AppDatabase.exec(() -> {
            int rows = dao.activateRecurring(current.id);
            runOnUiThread(() -> {
                if (rows > 0) { toast("Aktivirano."); finish(); }
                else toast("Ne može: samo iz statusa PAUSED (ponavljajući).");
            });
        });
    }

    private int xpFor(TaskEntity t) {
        return (t.totalXp != 0) ? t.totalXp : (t.weightXp + t.importanceXp);
    }

    private void awardXp(int delta){
        var sp = getSharedPreferences("stats", MODE_PRIVATE);
        int cur = sp.getInt("xp", 0);
        sp.edit().putInt("xp", Math.max(0, cur + delta)).apply();
    }


    private void deleteTask(TaskEntity t){
        AppDatabase.exec(() -> {
            int rows;
            if ("ONE_TIME".equals(t.kind)){
                rows = dao.deleteOneTime(t.id, System.currentTimeMillis());
            } else {
                long endAt = startOfDay(System.currentTimeMillis()) - 1L;
                rows = dao.cancelRecurringFromNow(t.id, endAt);
            }
            runOnUiThread(() -> {
                if (rows == 0) toast("Nije moguće obrisati (prošao/ili nije ACTIVE).");
                else { toast("Obrisano."); finish(); }
            });
        });
    }



    private long startOfDay(long ts){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static String fmtDateTime(@Nullable Long ms){
        if (ms == null) return "—";
        return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date(ms));
    }
    private static String fmtDate(@Nullable Long ms){
        if (ms == null) return "";
        return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                .format(new Date(ms));
    }

    private void toast(String s){ Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
