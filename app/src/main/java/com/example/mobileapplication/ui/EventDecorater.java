package com.example.mobileapplication.ui;

import android.graphics.Color;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Dekorator: nacrta tačke u 1-3 boje za dan koji ima zadatke. */
public class EventDecorater implements DayViewDecorator {

    private final Set<CalendarDay> days = new HashSet<>();
    private final int[] dotColors; // do 3 boje

    public EventDecorater(List<CalendarDay> days, int[] dotColors) {
        if (days != null) this.days.addAll(days);
        this.dotColors = dotColors != null ? dotColors : new int[]{Color.GRAY};
    }

    @Override public boolean shouldDecorate(CalendarDay day) {
        return days.contains(day);
    }

    @Override public void decorate(DayViewFacade view) {
        // do tri tačke u različitim bojama
        for (int i = 0; i < dotColors.length && i < 3; i++) {
            view.addSpan(new DotSpanEx(6, dotColors[i], i)); // 6dp radijus, offset i
        }
    }
}
