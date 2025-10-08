package com.example.mobileapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mobileapplication.R;
import com.example.mobileapplication.data.TaskEntity;
import com.example.mobileapplication.databinding.ActivityTaskListBinding;
import com.example.mobileapplication.domain.TaskModels;
import java.util.ArrayList;
import java.util.List;

public class TaskListActivity extends AppCompatActivity {

    private ActivityTaskListBinding binding;
    private TaskListAdapter adapter;

    private TaskListViewModel vm;

    private List<TaskListItem> cacheOneTime = new ArrayList<>();
    private List<TaskListItem> cacheRecurring = new ArrayList<>();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        adapter = new TaskListAdapter(

                item -> TaskDetailActivity.start(this, item.id),

                (item, action) -> vm.setStatus(item.id, action)
        );
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(this));

        binding.rvTasks.setAdapter(adapter);

        binding.tabKind.addTab(binding.tabKind.newTab().setText("Jednokratni"));
        binding.tabKind.addTab(binding.tabKind.newTab().setText("Ponavljajući"));

        vm = new ViewModelProvider(this).get(TaskListViewModel.class);

        vm.getTasks().observe(this, entities -> {      // ili vm.getTasks() ako želiš baš sve
            cacheOneTime.clear();
            cacheRecurring.clear();

            long today0 = startOfToday(System.currentTimeMillis());

            if (entities != null) {
                for (TaskEntity e : entities) {
                    if (!"ACTIVE".equals(e.status)) continue;

                    TaskListItem it = toItem(e);

                    if ("ONE_TIME".equals(e.kind)) {
                        if (e.scheduledAt == null || e.scheduledAt >= today0) {
                            cacheOneTime.add(it);
                        }
                    } else if ("RECURRING".equals(e.kind)) {
                        Long end   = e.repeatEndAt; // may be null (open-ended)

                        // Show only if series still spans today (start <= today <= end|∞)
                        if (end == null || end >= today0) {
                            cacheRecurring.add(it);
                        }
                    }
                }
            }
            int pos = binding.tabKind.getSelectedTabPosition();
            showTab(pos == 0 ? cacheOneTime : cacheRecurring);
        });


        binding.tabKind.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                showTab(tab.getPosition() == 0 ? cacheOneTime : cacheRecurring);
            }
            @Override public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            @Override public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
        });


        binding.tabKind.selectTab(binding.tabKind.getTabAt(0));


        binding.fabCalendar.setOnClickListener(v ->
                startActivity(new Intent(this, CalendarActivity.class)));


        binding.fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, CreateTaskActivity.class)));


    }

    private long startOfToday(long ts){
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(ts);
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }


    @Override protected void onResume() {
        super.onResume();

    }

    private void showTab(List<TaskListItem> data) {
        adapter.setItems(data);
        adapter.notifyDataSetChanged();
    }

    private TaskListItem toItem(TaskEntity e) {
        TaskListItem it = new TaskListItem();
        it.id = e.id;
        it.title = e.title;
        it.description = e.description;
        it.kind = e.kind;

        if ("ONE_TIME".equals(e.kind)) {
            it.whenText = fmt(e.scheduledAt);
        } else {
            String unit = e.repeatUnit == null ? "" : e.repeatUnit.toLowerCase();
            String base = "svakih " + safeNum(e.repeatEvery) + " " + unit;
            String start = e.repeatStartAt == null ? "" : " · " + fmt(e.repeatStartAt);
            String end   = e.repeatEndAt   == null ? "" : " – " + fmt(e.repeatEndAt);
            it.whenText = base + start + end;
        }

        it.valueXp = (e.weightXp) + (e.importanceXp);
        return it;
    }

    private static String fmt(Long epochMillis) {
        if (epochMillis == null) return "";
        java.text.SimpleDateFormat df =
                new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());
        return df.format(new java.util.Date(epochMillis));
    }
    private static int safeNum(Integer x) { return x == null ? 0 : x; }
}



