package com.example.mobileapplication.ui.tasks;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.models.CategoryEntity;
import com.example.mobileapplication.data.dao.TaskDao;
import com.example.mobileapplication.data.models.TaskEntity;
import com.example.mobileapplication.databinding.ActivityCalendarBinding;
import com.example.mobileapplication.ui.tasks.helpers.EventDecorater;
import com.google.firebase.auth.FirebaseAuth;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {
    private static FirebaseAuth auth = FirebaseAuth.getInstance();
    private static String userId = auth.getCurrentUser().getUid();
    private ActivityCalendarBinding b;
    private TaskDao taskDao;

    private DayTaskAdapter dayAdapter;

    private List<TaskEntity> lastTasks = new ArrayList<>();

    private final Map<Long, Integer> catColors = new HashMap<>();
    private long currentDayStart;
    private final Map<Long, List<TaskEntity>> byDay = new HashMap<>();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        taskDao = AppDatabase.get(this).taskDao();


        b.rvDay.setLayoutManager(new LinearLayoutManager(this));
        dayAdapter = new DayTaskAdapter(

                item -> TaskDetailActivity.start(CalendarActivity.this, item.id),

                (item, action) -> {
                    AppDatabase.exec(() -> {
                        taskDao.updateStatus(item.id, action, userId);
                        runOnUiThread(this::refreshForSelectedDay);
                    });
                }
        );
        b.rvDay.setAdapter(dayAdapter);

        b.mcal.setOnDateChangedListener((widget, date, selected) -> {
            long ts = atStartOfDay(date);
            List<TaskEntity> list = byDay.getOrDefault(ts, new ArrayList<>());
            dayAdapter.submit(list);
        });


        taskDao.getAll(userId).observe(this, tasks -> {
            if (tasks == null) tasks = new ArrayList<>();
            prepareCalendarData(tasks);
            lastTasks = tasks;
            prepareCalendarData(lastTasks);

            CalendarDay sel = b.mcal.getSelectedDate();
            if (sel == null) sel = CalendarDay.today();

            long today = startOfDay(System.currentTimeMillis());
            dayAdapter.submit(byDay.getOrDefault(today, new ArrayList<>()));
        });

        AppDatabase.get(this).categoryDao().all().observe(this, list -> {
            catColors.clear();
            if (list != null) {
                for (CategoryEntity c : list) {
                    int color = (c.colorHex != null && !c.colorHex.isEmpty())
                            ? android.graphics.Color.parseColor(c.colorHex)
                            : fallbackColorFor(c.id);
                    catColors.put(c.id, color);
                }
            }
            prepareCalendarData(lastTasks);
            CalendarDay sel = b.mcal.getSelectedDate();
            if (sel == null) sel = CalendarDay.today();
            long ts = atStartOfDay(sel);
            dayAdapter.submit(byDay.getOrDefault(ts, new ArrayList<>()));
        });
    }

    private void refreshForSelectedDay() {
        CalendarDay sel = b.mcal.getSelectedDate();
        if (sel == null) sel = CalendarDay.today();
        long ts = atStartOfDay(sel);
        dayAdapter.submit(byDay.getOrDefault(ts, new ArrayList<>()));
    }

    private void prepareCalendarData(List<TaskEntity> tasks) {
        byDay.clear();


        loadCategoryColors();

        for (TaskEntity t : tasks) {

            if ("ONE_TIME".equals(t.kind)) {
                if (t.scheduledAt != null) {
                    long d = startOfDay(t.scheduledAt);
                    putOnDay(d, t);
                }
            } else if ("RECURRING".equals(t.kind)) {
                int every = (t.repeatEvery == null ? 0 : t.repeatEvery);
                if (every <= 0 || t.repeatUnit == null || t.repeatStartAt == null) continue;                long periodMs;
                if ("DAY".equals(t.repeatUnit)) {
                    periodMs = every * 24L * 60 * 60 * 1000;
                } else if ("WEEK".equals(t.repeatUnit)) {
                    periodMs = every * 7L * 24L * 60 * 60 * 1000;
                } else {
                    continue;
                }
                long from = startOfDay(t.repeatStartAt);
                long to   = (t.repeatEndAt != null) ? startOfDay(t.repeatEndAt) : from + 90L * 24 * 60 * 60 * 1000;
                for (long d = from; d <= to; d += periodMs) putOnDay(d, t);
            }
        }


        b.mcal.removeDecorators();
        List<CalendarDay> allDays = new ArrayList<>();
        Map<Long, int[]> colorPerDay = new HashMap<>();

        for (Map.Entry<Long, List<TaskEntity>> e : byDay.entrySet()) {
            long dayTs = e.getKey();
            List<TaskEntity> list = e.getValue();


            List<Integer> colors = new ArrayList<>();
            for (TaskEntity t : list) {
                Integer c = catColors.get(t.categoryId);
                if (c == null) c = 0xFF9E9E9E;
                if (!colors.contains(c)) {
                    colors.add(c);
                    if (colors.size() == 3) break;
                }
            }

            CalendarDay cd = toCalendarDay(dayTs);
            allDays.add(cd);
            int[] arr = new int[colors.size()];
            for (int i = 0; i < colors.size(); i++) arr[i] = colors.get(i);
            colorPerDay.put(dayTs, arr);
        }


        for (Map.Entry<Long, int[]> e : colorPerDay.entrySet()) {
            CalendarDay cd = toCalendarDay(e.getKey());
            List<CalendarDay> one = new ArrayList<>();
            one.add(cd);
            b.mcal.addDecorator(new EventDecorater(one, e.getValue()));
        }
    }

    private void putOnDay(long dayTs, TaskEntity t) {
        List<TaskEntity> lst = byDay.get(dayTs);
        if (lst == null) {
            lst = new ArrayList<>();
            byDay.put(dayTs, lst);
        }
        lst.add(t);
    }

    private Map<Long, Integer> loadCategoryColors() {
        catColors.clear();


        AppDatabase.get(this).categoryDao().all().observe(this, list -> { if (list != null) {
            for (CategoryEntity c : list) {

                int color = (c.colorHex != null && !c.colorHex.isEmpty())
                        ? Color.parseColor(c.colorHex)
                        : fallbackColorFor(c.id);
                catColors.put(c.id, color);
            }
        } });


        return catColors;
    }


    private int fallbackColorFor(long id) {
        int[] palette = {
                0xFFE57373, 0xFF64B5F6, 0xFF81C784, 0xFFFFB74D,
                0xFFBA68C8, 0xFFFF8A65, 0xFF4DB6AC, 0xFFA1887F
        };
        return palette[(int)(Math.abs(id) % palette.length)];
    }

    private CalendarDay toCalendarDay(long dayTs) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dayTs);
        return CalendarDay.from(c);
    }

    private long startOfDay(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long atStartOfDay(CalendarDay cd) {
        Calendar c = Calendar.getInstance();
        c.set(cd.getYear(), cd.getMonth() , cd.getDay(), 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }



    private void showTaskSheet(TaskEntity t) {
        View v = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, null, false);
        TextView t1 = v.findViewById(android.R.id.text1);
        TextView t2 = v.findViewById(android.R.id.text2);

        t1.setText(t.title);
        String when = (t.scheduledAt != null)
                ? DayTaskAdapter.formatTs(t.scheduledAt)
                : "Periodično";
        t2.setText(String.format(Locale.getDefault(),
                "Kategorija #%d  •  %s  •  XP=%d",
                t.categoryId, when, (t.totalXp != 0 ? t.totalXp : 0)));

        new AlertDialog.Builder(this)
                .setView(v)
                .setPositiveButton("Urađeno", (d, w) -> updateStatusAndRefresh(t.id, "DONE"))
                .setNeutralButton("Pauza",   (d, w) -> updateStatusAndRefresh(t.id, "PAUSED"))
                .setNegativeButton("Otkaži", (d, w) -> updateStatusAndRefresh(t.id, "CANCELED"))
                .setCancelable(true)
                .show();
    }

    private void updateStatusAndRefresh(long id, String action) {
        AppDatabase.get(this).taskDao().updateStatus(id, action,userId);


    }
}
