package com.example.mobileapplication.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.CategoryEntity;
import com.example.mobileapplication.data.TaskDao;
import com.example.mobileapplication.data.TaskEntity;
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

        // 1) Posmatraj task
        dao.byId(taskId).observe(this, t -> {
            current = t;
            if (t != null) bind(t, catsById.get(t.categoryId));
        });

        // 2) Posmatraj kategorije (ime/boja)
        AppDatabase.get(this).categoryDao().all().observe(this, list -> {
            catsById.clear();
            if (list != null) for (CategoryEntity c : list) catsById.put(c.id, c);
            if (current != null) bind(current, catsById.get(current.categoryId));
        });

        // 3) Status akcije
        b.btnDone.setOnClickListener(v -> updateStatus("DONE"));
        b.btnPause.setOnClickListener(v -> updateStatus("PAUSED"));
        b.btnCancel.setOnClickListener(v -> updateStatus("CANCELED"));

        // 4) Izmjena / Brisanje
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

    // ---------- UI bind ----------

    private void bind(TaskEntity t, @Nullable CategoryEntity cat) {
        // Naslov / opis
        b.tvTitle.setText(t.title == null ? "" : t.title);
        b.tvDesc.setText(t.description == null ? "" : t.description);

        // Kategorija (chip bez ikonica!)
        String catName = (cat != null && cat.name != null) ? cat.name : ("Kategorija #" + t.categoryId);
        b.chCategory.setText(catName);
        int color = safeCatColor(cat, t.categoryId);
        b.chCategory.setChipBackgroundColor(ColorStateList.valueOf(color));

        // Vrsta i “kada”
        b.chKind.setText(t.kind == null ? "ONE_TIME" : t.kind);
        b.tvWhen.setText(buildWhenText(t));

        // XP
        int xp = (t.totalXp != 0 ? t.totalXp : (t.weightXp + t.importanceXp));
        b.tvXp.setText(xp + " XP");

        // Onemogući izmjenu/brisanje za DONE
        renderButtons();
    }

    private void renderButtons(){
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

    // ---------- Akcije ----------

    private void updateStatus(String status){
        AppDatabase.exec(() -> {
            dao.updateStatus(taskId, status);
            runOnUiThread(() -> {
                toast("Status: " + status);
                finish();
            });
        });
    }

    private void deleteTask(TaskEntity t){
        AppDatabase.exec(() -> {
            int rows;
            if ("ONE_TIME".equals(t.kind)){
                rows = dao.deleteOneTime(t.id, System.currentTimeMillis());
            } else {
                long endAt = startOfDay(System.currentTimeMillis()) - 1L; // do “juče”
                rows = dao.cancelRecurringFromNow(t.id, endAt);
            }
            runOnUiThread(() -> {
                if (rows == 0) toast("Nije moguće obrisati (prošao/ili nije ACTIVE).");
                else { toast("Obrisano."); finish(); }
            });
        });
    }

    // ---------- Helpers ----------

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
