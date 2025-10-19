package com.example.mobileapplication.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mobileapplication.MobileApplication;
import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.models.StatisticsModel;
import com.example.mobileapplication.data.repository.StatisticsRepository;
import com.example.mobileapplication.data.repository.TaskRepository;
import com.example.mobileapplication.domain.serviceImpl.StatisticsServiceImpl;

public class StatisticsViewModel extends ViewModel {

    private final MutableLiveData<StatisticsModel> statistics = new MutableLiveData<>();
    private final StatisticsServiceImpl service;

    public StatisticsViewModel() {
        AppDatabase db = AppDatabase.get(MobileApplication.getAppContext());

        TaskRepository taskRepo = new TaskRepository(db.taskDao(), db.categoryDao());

        this.service = new StatisticsServiceImpl(new StatisticsRepository(taskRepo, db.taskDao()));
    }

    public LiveData<StatisticsModel> getStatistics() {
        return statistics;
    }

    public void loadStatistics() {
        service.getStatistics(task -> {
            if (task.isSuccessful()) {
                statistics.postValue(task.getResult());
            }
        });
    }
}
