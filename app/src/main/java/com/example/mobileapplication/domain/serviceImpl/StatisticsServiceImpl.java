package com.example.mobileapplication.domain.serviceImpl;

import com.example.mobileapplication.data.models.StatisticsModel;
import com.example.mobileapplication.data.repository.StatisticsRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class StatisticsServiceImpl {
    private final StatisticsRepository repository;

    public StatisticsServiceImpl(StatisticsRepository repository) {
        this.repository = repository;
    }

    public void getStatistics(OnCompleteListener<StatisticsModel> listener) {
        Task<StatisticsModel> task = repository.loadStatistics();
        task.addOnCompleteListener(listener);
    }
}
