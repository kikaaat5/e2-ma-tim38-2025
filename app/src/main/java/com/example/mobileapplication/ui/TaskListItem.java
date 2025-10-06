package com.example.mobileapplication.ui;

import com.example.mobileapplication.data.TaskEntity;
import com.example.mobileapplication.domain.TaskModels;

import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskListItem {
    public long id; // ako imaš iz baze; inače 0
    public String title;
    public String description;
    public String kind;
    public Long scheduledAtEpochMillis;        // za ONE_TIME
    public Integer repeatEvery;                // za RECURRING
    public TaskModels.RepeatUnit repeatUnit;   // za RECURRING
    public Long repeatStartEpochMillis;        // za RECURRING
    public Long repeatEndEpochMillis;          // za RECURRING
    public TaskModels.TaskWeightXP weight;
    public TaskModels.TaskImportanceXP importance;
    public String whenText;      // gotov tekst za prikaz

    public int valueXp;          // ukupni XP

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

            // XP – pretpostavljamo da su u entitetu numerička polja weightXp i importanceXp
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
