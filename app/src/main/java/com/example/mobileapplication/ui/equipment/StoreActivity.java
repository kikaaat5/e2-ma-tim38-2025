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

import java.util.ArrayList;
import java.util.List;

public class StoreActivity extends AppCompatActivity {

    private EquipmentViewModel vm;
    private StoreAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        setTitle("🏪 Prodavnica");

        vm = new ViewModelProvider(this).get(EquipmentViewModel.class);

        RecyclerView recycler = findViewById(R.id.recyclerStore);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StoreAdapter(this::onBuyClick);
        recycler.setAdapter(adapter);

        adapter.submitList(getStoreItems());
    }

    private void onBuyClick(EquipmentEntity item) {
        vm.buyItem(item);

    }

    private List<EquipmentEntity> getStoreItems() {
        List<EquipmentEntity> list = new ArrayList<>();

        // 🔹 Napitci
        list.add(makeItem("Napitak +20% snage (1 borba)", "POTION",
                "Jednokratno povećava PP za 20%", 0.20, 50, 1));
        list.add(makeItem("Napitak +40% snage (1 borba)", "POTION",
                "Jednokratno povećava PP za 40%", 0.40, 70, 1));
        list.add(makeItem("Napitak +5% snage (trajno)", "POTION",
                "Trajno povećava PP za 5%", 0.05, 200, 0));
        list.add(makeItem("Napitak +10% snage (trajno)", "POTION",
                "Trajno povećava PP za 10%", 0.10, 1000, 0));

        // 🔹 Odeća
        list.add(makeItem("Rukavice snage +10%", "ARMOR",
                "Povećavaju snagu napada za 10%", 0.10, 60, 2));
        list.add(makeItem("Štit uspešnog napada +10%", "ARMOR",
                "Povećava šansu uspešnog napada za 10%", 0.10, 60, 2));
        list.add(makeItem("Čizme dodatni napad +40%", "ARMOR",
                "Povećavaju šansu za dodatni napad za 40%", 0.40, 80, 2));

        // 🔹 Oružje
        list.add(makeItem("Mač +5% snage", "WEAPON",
                "Trajno povećava snagu za 5%", 0.05, 300, 0));
        list.add(makeItem("Luk i strela +5% novčića", "WEAPON",
                "Trajno povećava dobijeni novac za 5%", 0.05, 300, 0));

        return list;
    }

    private EquipmentEntity makeItem(String name, String type, String effect,
                                     double value, int price, int duration) {
        EquipmentEntity e = new EquipmentEntity();
        e.name = name;
        e.type = type;
        e.effect = effect;
        e.value = value;
        e.price = price;
        e.duration = duration;
        e.isActive = false;
        e.battlesLeft = 0;
        return e;
    }
}
