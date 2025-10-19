// ui/CategoryListActivity.java
package com.example.mobileapplication.ui.tasks;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.ComponentActivity;


import com.example.mobileapplication.R;
import com.example.mobileapplication.data.AppDatabase;
import com.example.mobileapplication.data.dao.CategoryDao;
import com.example.mobileapplication.data.models.CategoryEntity;
import com.example.mobileapplication.databinding.ActivityCategoryListBinding;

import java.util.Arrays;
import java.util.List;

public class CategoryListActivity extends AppCompatActivity {

    private ActivityCategoryListBinding b;
    private CategoryDao dao;
    private CategoryAdapter adapter;

    public static final String EXTRA_PICK_MODE = "pick_mode";
    public static final String EXTRA_RESULT_ID = "category_id";
    public static final String EXTRA_RESULT_NAME = "category_name";
    public static final String EXTRA_RESULT_COLOR = "category_color";


    private static final String[] PALETTE = {
            "#E57373","#64B5F6","#81C784","#FFB74D",
            "#BA68C8","#FF8A65","#4DB6AC","#A1887F",
            "#F06292","#9575CD","#4FC3F7","#AED581",
            "#FFD54F","#4DB6AC","#90A4AE"
    };

    private RecyclerView rv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);
        dao = AppDatabase.get(this).categoryDao();

        rv = findViewById(R.id.rvCats);

        boolean pickMode = getIntent().getBooleanExtra(EXTRA_PICK_MODE, false);

        adapter = new CategoryAdapter(new CategoryAdapter.Actions() {
            @Override public void onEdit(CategoryEntity c) { showUpsertDialog(c); }
            @Override public void onDelete(CategoryEntity c) { tryDelete(c); }
            @Override public void onPick(CategoryEntity c) {
                Intent data = new Intent()
                        .putExtra(EXTRA_RESULT_ID,    c.id)
                        .putExtra(EXTRA_RESULT_NAME,  c.name)
                        .putExtra(EXTRA_RESULT_COLOR, c.colorHex == null ? "" : c.colorHex);
                setResult(RESULT_OK, data);
                finish();
            }
        });
        adapter.setPickMode(pickMode);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        com.google.android.material.floatingactionbutton.FloatingActionButton fab =
                findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> showUpsertDialog(null));

        AppDatabase.get(this).categoryDao().all().observe(this, list -> {
            adapter.submit(list);
        });
    }

    private void showUpsertDialog(@Nullable CategoryEntity existing){
        var dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category, null, false);
        EditText etName = dialogView.findViewById(R.id.etName);
        var vPreview = dialogView.findViewById(R.id.vPreview);
        var btnPick = dialogView.findViewById(R.id.btnPick);
        TextView tvHex    = dialogView.findViewById(R.id.tvHex);

        final String[] chosen = { existing != null && existing.colorHex != null ? existing.colorHex : "#FF9800" };
        etName.setText(existing != null ? existing.name : "");
        vPreview.setBackgroundColor(Color.parseColor(chosen[0]));
        tvHex.setText(chosen[0]);

        btnPick.setOnClickListener(v -> {
            List<String> colors = Arrays.asList(PALETTE);
            CharSequence[] items = colors.toArray(new CharSequence[0]);
            new AlertDialog.Builder(this)
                    .setTitle("Izaberi boju")
                    .setItems(items, (d, which) -> {
                        chosen[0] = colors.get(which);
                        vPreview.setBackgroundColor(Color.parseColor(chosen[0]));
                        tvHex.setText(chosen[0]);
                    })
                    .show();
        });

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Nova kategorija" : "Izmena kategorije")
                .setView(dialogView)
                .setPositiveButton("Sačuvaj", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String hex  = chosen[0];

                    if (name.isEmpty()){
                        toast("Unesi naziv.");
                        return;
                    }

                    AppDatabase.exec(() -> {
                        try {
                            if (existing == null) {
                                if (dao.existsColor(hex) > 0) {
                                    runOnUiThread(() -> toast("Ta boja je već iskorišćena."));
                                    return;
                                }
                                CategoryEntity c = new CategoryEntity();
                                c.name = name;
                                c.colorHex = hex;
                                dao.insert(c);
                            } else {
                                if (dao.existsColorExcluding(hex, existing.id) > 0) {
                                    runOnUiThread(() -> toast("Ta boja je već iskorišćena."));
                                    return;
                                }
                                existing.name = name;
                                existing.colorHex = hex;
                                dao.update(existing);
                            }
                        } catch (Exception e){
                            runOnUiThread(() -> toast("Greška: " + e.getMessage()));
                        }
                    });
                })
                .setNegativeButton("Otkaži", null)
                .show();
    }

    private void tryDelete(CategoryEntity c){
        AppDatabase.exec(() -> {
            int active = dao.activeTasksIn(c.id);
            if (active > 0) {
                runOnUiThread(() -> toast("Nije moguće obrisati: postoje aktivni zadaci u toj kategoriji."));
                return;
            }
            int rows = dao.deleteById(c.id);
            runOnUiThread(() -> {
                if (rows > 0) toast("Kategorija obrisana.");
                else toast("Brisanje nije uspelo.");
            });
        });
    }

    private void toast(String s){ Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
