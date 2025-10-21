package com.example.mobileapplication.ui.equipment;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapplication.R;
import com.example.mobileapplication.data.models.EquipmentEntity;
import com.example.mobileapplication.ui.viewModel.EquipmentViewModel;

import java.util.List;

public class EquipmentActivity extends AppCompatActivity {

    private EquipmentViewModel vm;
    private EquipmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment);

        vm = new ViewModelProvider(this).get(EquipmentViewModel.class);

        RecyclerView recycler = findViewById(R.id.recyclerEquipment);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EquipmentAdapter(this::onActivateClick);
        recycler.setAdapter(adapter);

        vm.getEquipment().observe(this, this::updateList);
    }

    private void updateList(List<EquipmentEntity> list) {
        adapter.submitList(list);
    }

    private void onActivateClick(EquipmentEntity item) {
        if (item.isActive) {
            Toast.makeText(this, item.name + " je već aktivna!", Toast.LENGTH_SHORT).show();
            return;
        }

        int battles = item.duration > 0 ? item.duration : 0;
        vm.activateItem(item.id, battles);
        Toast.makeText(this, "Aktivirana oprema: " + item.name, Toast.LENGTH_SHORT).show();
    }
}
