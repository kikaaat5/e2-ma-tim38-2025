package com.example.mobileapplication.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executors;

@Database(
        entities = { TaskEntity.class, CategoryEntity.class },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TaskDao taskDao();
    public abstract CategoryDao categoryDao();

    private static volatile AppDatabase INSTANCE;



    // Callback koji se izvrši SAMO prvi put kad se baza kreira – ubacujemo default kategoriju
    private static final RoomDatabase.Callback SEED = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            Executors.newSingleThreadExecutor().execute(() -> {
                if (INSTANCE != null) {
                    CategoryEntity c = new CategoryEntity();
                    c.name = "General";
                    c.colorHex = "#FF9800";
                    INSTANCE.categoryDao().insert(c);
                }
            });
        }
    };

    public static AppDatabase get(Context ctx){
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    ctx.getApplicationContext(),
                                    AppDatabase.class,
                                    "habitquest.db"

                            )
                            .fallbackToDestructiveMigration()   // za razvoj
                            .allowMainThreadQueries()
                            .addCallback(SEED)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // data/AppDatabase.java
    public static void exec(Runnable r){
        Executors.newSingleThreadExecutor().execute(r);
    }

}
