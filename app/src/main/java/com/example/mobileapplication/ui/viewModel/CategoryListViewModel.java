package com.example.mobileapplication.ui.viewModel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.models.CategoryEntity;
import java.util.List;

public class CategoryListViewModel extends AndroidViewModel {
    private final LiveData<List<CategoryEntity>> categories;

    public CategoryListViewModel(@NonNull Application app) {
        super(app);
        categories = AppDatabase.get(app).categoryDao().all();
    }
    public LiveData<List<CategoryEntity>> getCategories() { return categories; }
}
