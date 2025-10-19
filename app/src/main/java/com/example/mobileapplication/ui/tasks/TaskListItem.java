package com.example.mobileapplication.ui.tasks;

import com.example.mobileapplication.data.models.TaskEntity;
import com.example.mobileapplication.data.models.TaskModels;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskListItem {
    public long id;
    public String title;
    public String description;
    public String kind;
    public Long scheduledAtEpochMillis;
    public Integer repeatEvery;
    public TaskModels.RepeatUnit repeatUnit;
    public Long repeatStartEpochMillis;
    public Long repeatEndEpochMillis;
    public TaskModels.TaskWeightXP weight;
    public TaskModels.TaskImportanceXP importance;
    public String whenText;

    public int valueXp;

    public int totalXp() {
        int w = (weight != null) ? weight.xp : 0;
        int i = (importance != null) ? importance.xp : 0;
        return w + i;
    }

    public static List<TaskListItem> fromEntities(@Nullable List<TaskEntity> src){
        List<TaskListItem> out = new ArrayList<>();
        if (src == null) return out;

        for (TaskEntity e : src){
            TaskListItem it = new TaskListItem();
            it.id = e.id;
            it.title = e.title;
            it.description = e.description;
            it.kind = e.kind;

            // WHEN
            if ("ONE_TIME".equals(e.kind)) {
                it.whenText = fmt(e.scheduledAt);
            } else if ("RECURRING".equals(e.kind)) {
                String unit  = e.repeatUnit == null ? "" : e.repeatUnit.toLowerCase(Locale.getDefault());
                String base  = "svakih " + safeNum(e.repeatEvery) + " " + unit;
                String start = e.repeatStartAt == null ? "" : " · " + fmt(e.repeatStartAt);
                String end   = e.repeatEndAt   == null ? "" : " – " + fmt(e.repeatEndAt);
                it.whenText = base + start + end;
            } else {
                it.whenText = "";
            }

            it.valueXp = safeNum(e.weightXp) + safeNum(e.importanceXp);

            out.add(it);
        }
        return out;
    }

    private static String fmt(@Nullable Long epochMillis){
        if (epochMillis == null) return "";
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return df.format(new Date(epochMillis));
    }
    private static int safeNum(@Nullable Integer x){ return x == null ? 0 : x; }
    public TaskListItem() {}

}
