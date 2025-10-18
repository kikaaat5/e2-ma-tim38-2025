package com.example.mobileapplication.ui.profile;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobileapplication.R;
import com.example.mobileapplication.data.models.StatisticsModel;
import com.example.mobileapplication.ui.viewModel.StatisticsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    private StatisticsViewModel viewModel;
    private TextView tvDaysActive, tvLongestStreak, tvSpecialMissions;
    private PieChart chartTasks;
    private BarChart chartCategories;
    private LineChart chartDifficulty, chartXP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        tvDaysActive = findViewById(R.id.tvDaysActive);
        tvLongestStreak = findViewById(R.id.tvLongestStreak);
        tvSpecialMissions = findViewById(R.id.tvSpecialMissions);
        chartTasks = findViewById(R.id.chartTasks);
        chartCategories = findViewById(R.id.chartCategories);
        chartDifficulty = findViewById(R.id.chartDifficulty);
        chartXP = findViewById(R.id.chartXP);

        viewModel.loadStatistics();

        viewModel.getStatistics().observe(this, this::updateUI);
    }

    private void updateUI(StatisticsModel stats) {
        if (stats == null) return;

        tvDaysActive.setText("Aktivan: " + stats.getActiveDays() + " dana");
        tvLongestStreak.setText("Najduži niz: " + stats.getLongestStreak() + " dana");
        tvSpecialMissions.setText("Specijalne misije: " + stats.getStartedMissions() + " započete, " + stats.getFinishedMissions() + " završene");

        setupTaskDonutChart(stats);
        setupCategoryBarChart(stats.getTasksByCategory());
        setupDifficultyLineChart(stats.getAvgDifficultyXp());
        setupXPLineChart(stats.getXpByDay());
    }

    private void setupTaskDonutChart(StatisticsModel stats) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(stats.getCompletedTasks(), "Urađeni"));
        entries.add(new PieEntry(stats.getUncompletedTasks(), "Neurađeni"));
        entries.add(new PieEntry(stats.getCanceledTasks(), "Otkazani"));
        entries.add(new PieEntry(stats.getCreatedTasks(), "Kreirani"));

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(Color.GREEN, Color.RED, Color.GRAY, Color.CYAN);
        PieData data = new PieData(set);
        data.setValueTextColor(Color.WHITE);
        chartTasks.setData(data);
        chartTasks.getDescription().setEnabled(false);
        chartTasks.invalidate();
    }

    private void setupCategoryBarChart(Map<String, Integer> map) {
        List<BarEntry> entries = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            entries.add(new BarEntry(index++, entry.getValue()));
        }
        BarDataSet dataSet = new BarDataSet(entries, "Zadaci po kategorijama");
        dataSet.setColor(Color.CYAN);
        BarData data = new BarData(dataSet);
        chartCategories.setData(data);
        chartCategories.getDescription().setEnabled(false);
        chartCategories.invalidate();
    }

    private void setupDifficultyLineChart(Map<String, Integer> map) {
        List<Entry> entries = new ArrayList<>();
        int index = 1;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            entries.add(new Entry(index++, entry.getValue()));
        }
        LineDataSet dataSet = new LineDataSet(entries, "Prosečna XP po težini");
        dataSet.setColor(Color.YELLOW);
        dataSet.setCircleColor(Color.MAGENTA);
        LineData data = new LineData(dataSet);
        chartDifficulty.setData(data);
        chartDifficulty.getDescription().setEnabled(false);
        chartDifficulty.invalidate();
    }

    private void setupXPLineChart(Map<String, Integer> map) {
        List<Entry> entries = new ArrayList<>();
        int i = 1;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            entries.add(new Entry(i++, entry.getValue()));
        }
        LineDataSet dataSet = new LineDataSet(entries, "XP poslednjih 7 dana");
        dataSet.setColor(Color.CYAN);
        dataSet.setCircleColor(Color.WHITE);
        LineData data = new LineData(dataSet);
        chartXP.setData(data);
        chartXP.getDescription().setEnabled(false);
        chartXP.invalidate();
    }
}
