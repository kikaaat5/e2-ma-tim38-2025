package com.example.mobileapplication.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.CategoryEntity;
import com.example.mobileapplication.databinding.ActivityCreateTaskBinding;
import com.example.mobileapplication.domain.TaskModels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateTaskActivity extends ComponentActivity {
    private ActivityCreateTaskBinding b;
    private CreateTaskViewModel vm;

    // Kategorije (UI i referenca)
    private final List<CategoryEntity> catRef = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;
    private long selectedCategoryId = -1L;

    private long editId = -1;            // <— DODAJ OVO


    public static final String EXTRA_TASK_ID = "EXTRA_TASK_ID";

    public static void startEdit(android.content.Context ctx, long taskId){
        android.content.Intent i = new android.content.Intent(ctx, CreateTaskActivity.class);
        i.putExtra(EXTRA_TASK_ID, taskId);
        ctx.startActivity(i);
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCreateTaskBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        long editId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        if (editId != -1) {
            // Prefill polja postojećim taskom
            AppDatabase.get(this).taskDao().byId(editId).observe(this, t -> {
                if (t == null) return;
                // popuni formu
                b.etTitle.setText(t.title);
                b.etDesc.setText(t.description);
                selectedCategoryId = t.categoryId;

                boolean isRecurring = "RECURRING".equals(t.kind);
                b.swRecurring.setChecked(isRecurring);
                b.tilOneTime.setVisibility(isRecurring? View.GONE: View.VISIBLE);
                b.groupRecurring.setVisibility(isRecurring? View.VISIBLE: View.GONE);

                // težina/bitnost (preko enum mape ako koristiš)
                // ili direktno indeks – podešavanje spinnera:
                b.spWeight.setSelection(TaskModels.TaskWeightXP.indexOfXp(t.weightXp));
                b.spImportance.setSelection(TaskModels.TaskImportanceXP.indexOfXp(t.importanceXp));

                if (!isRecurring){
                    if (t.scheduledAt != null){
                        b.etOneTimeWhen.setText(new java.util.Date(t.scheduledAt).toString());
                        b.etOneTimeWhen.setTag(t.scheduledAt);
                    }
                } else {
                    if (t.repeatEvery != null) b.etRepeatEvery.setText(String.valueOf(t.repeatEvery));
                    if (t.repeatUnit != null)  b.spRepeatUnit.setSelection(TaskModels.RepeatUnit.indexOf(t.repeatUnit));
                    if (t.repeatStartAt != null){
                        b.etRepeatStart.setText(new java.util.Date(t.repeatStartAt).toString());
                        b.etRepeatStart.setTag(t.repeatStartAt);
                    }
                    if (t.repeatEndAt != null){
                        b.etRepeatEnd.setText(new java.util.Date(t.repeatEndAt).toString());
                        b.etRepeatEnd.setTag(t.repeatEndAt);
                    }
                }
            });
        }


        vm = new ViewModelProvider(
                this,
                new CreateTaskViewModel.Factory(getApplication())
        ).get(CreateTaskViewModel.class);

        editId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);   // <— SAČUVAJ GA
        if (editId != -1) {
            AppDatabase.get(this).taskDao().byId(editId).observe(this, t -> { /* ...prefill... */ });
        }

        // SEED ako nema kategorija
        new Thread(() -> {
            try {
                if (AppDatabase.get(this).categoryDao().count() < 2) {
                    CategoryEntity c = new CategoryEntity();
                    c.name = "General";
                    c.colorHex = "#9E9E9E"; // ili šta već imaš u entitetu
                    AppDatabase.get(this).categoryDao().insert(c);
                    CategoryEntity c1 = new CategoryEntity();
                    c.name = "Sport";
                    c.colorHex = "#9E9E9E"; // ili šta već imaš u entitetu
                    AppDatabase.get(this).categoryDao().insert(c1);
                }
            } catch (Exception ignored) { }
        }).start();

        setupSpinners();
        setupSwitch();
        setupPickers();
        b.btnSave.setOnClickListener(v -> onSave());
    }

    private void setupSpinners() {
        // Težina / Bitnost / Jedinica ponavljanja
        b.spWeight.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                TaskModels.TaskWeightXP.values()
        ));
        b.spImportance.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                TaskModels.TaskImportanceXP.values()
        ));
        b.spRepeatUnit.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                TaskModels.RepeatUnit.values()
        ));

        // Kategorije – prazan adapter, pa Observe iz Room-a
        categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>()
        );
        b.spCategory.setAdapter(categoryAdapter);

        b.spCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < catRef.size()) {
                    selectedCategoryId = catRef.get(position).id;
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // POSMATRANJE kategorija
        AppDatabase.get(this)
                .categoryDao()
                .all() // LiveData<List<CategoryEntity>>
                .observe(this, list -> {
                    catRef.clear();
                    categoryAdapter.clear();

                    if (list != null && !list.isEmpty()) {
                        catRef.addAll(list);
                        for (CategoryEntity c : list) {
                            categoryAdapter.add(c.name + " (ID=" + c.id + ")");
                        }
                        categoryAdapter.notifyDataSetChanged();

                        // Default – prva kategorija
                        if (selectedCategoryId <= 0) {
                            selectedCategoryId = list.get(0).id;
                            b.spCategory.setSelection(0);
                        }
                    } else {
                        selectedCategoryId = -1L;
                        categoryAdapter.notifyDataSetChanged();
                        Toast.makeText(this,
                                "Nema kategorija. Prvo ih kreiraj u sekciji Kategorije.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupSwitch() {
        b.swRecurring.setOnCheckedChangeListener((btn, checked) -> {
            b.groupRecurring.setVisibility(checked ? View.VISIBLE : View.GONE);
            b.tilOneTime.setVisibility(checked ? View.GONE : View.VISIBLE);
        });
    }

    private void setupPickers() {
        b.etOneTimeWhen.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (dp, y, m, d) -> {
                new TimePickerDialog(this, (tp, h, min) -> {
                    Calendar when = Calendar.getInstance();
                    when.set(y, m, d, h, min, 0);
                    b.etOneTimeWhen.setText(when.getTime().toString());
                    b.etOneTimeWhen.setTag(when.getTimeInMillis());
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        b.etRepeatStart.setOnClickListener(v -> pickDateInto(b.etRepeatStart));
        b.etRepeatEnd.setOnClickListener(v -> pickDateInto(b.etRepeatEnd));
    }

    private void pickDateInto(View target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (dp, y, m, d) -> {
            Calendar when = Calendar.getInstance();
            when.set(y, m, d, 0, 0, 0);
            if (target.getId() == b.etRepeatStart.getId()) {
                b.etRepeatStart.setText(when.getTime().toString());
                b.etRepeatStart.setTag(when.getTimeInMillis());
            } else {
                b.etRepeatEnd.setText(when.getTime().toString());
                b.etRepeatEnd.setTag(when.getTimeInMillis());
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void onSave() {
        try {
            // Validacije
            String title = String.valueOf(b.etTitle.getText()).trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Unesi naziv zadatka.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedCategoryId <= 0) {
                Toast.makeText(this, "Izaberi kategoriju.", Toast.LENGTH_SHORT).show();
                return;
            }

            TaskModels.TaskDraft d = new TaskModels.TaskDraft();
            d.title = title;
            d.description = String.valueOf(b.etDesc.getText()).trim();
            d.categoryId = selectedCategoryId;

            boolean recurring = b.swRecurring.isChecked();
            d.kind = recurring ? TaskModels.TaskKind.RECURRING : TaskModels.TaskKind.ONE_TIME;

            d.weight = (TaskModels.TaskWeightXP) b.spWeight.getSelectedItem();
            d.importance = (TaskModels.TaskImportanceXP) b.spImportance.getSelectedItem();

            if (!recurring) {
                Object tag = b.etOneTimeWhen.getTag();
                d.scheduledAtEpochMillis = (tag instanceof Long) ? (Long) tag : 0L;
            } else {
                d.repeatEvery = parseIntSafe(b.etRepeatEvery.getText());
                d.repeatUnit = (TaskModels.RepeatUnit) b.spRepeatUnit.getSelectedItem();
                Object sTag = b.etRepeatStart.getTag();
                Object eTag = b.etRepeatEnd.getTag();
                d.repeatStartEpochMillis = (sTag instanceof Long) ? (Long) sTag : null;
                d.repeatEndEpochMillis   = (eTag instanceof Long) ? (Long) eTag : null;
            }

            long id = vm.save(d, editId);
            Toast.makeText(this, "Sačuvano. ID=" + id, Toast.LENGTH_LONG).show();
            finish();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Integer parseIntSafe(CharSequence cs) {
        try {
            String s = String.valueOf(cs).trim();
            return s.isEmpty() ? null : Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }
}
