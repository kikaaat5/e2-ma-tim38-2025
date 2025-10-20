package com.example.mobileapplication.ui.equipment;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapplication.R;
import com.example.mobileapplication.data.models.EquipmentEntity;
import com.example.mobileapplication.ui.viewModel.EquipmentInventoryViewModel;

public class EquipmentInventoryActivity extends AppCompatActivity {

    private EquipmentInventoryViewModel viewModel;
    private EquipmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_inventory);

        RecyclerView recycler = findViewById(R.id.recyclerEquipment);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EquipmentAdapter(item -> {
            viewModel.activate(item);
            adapter.markItemAsActivated(item.id);
            Toast.makeText(this, "Aktivirana oprema: " + item.name, Toast.LENGTH_SHORT).show();
        });
        recycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(EquipmentInventoryViewModel.class);
        viewModel.getUserEquipment().observe(this, list -> {
            if (list != null) adapter.submitList(list);
        });
    }
}
