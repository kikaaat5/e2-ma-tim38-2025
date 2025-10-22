package com.example.mobileapplication.ui.friends;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapplication.R;
import com.example.mobileapplication.ui.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    private com.example.mobileapplication.ui.friends.FriendsViewModel vm;
    private FriendsAdapter adapter;         // adapter za pretragu korisnika
    private FriendsAdapter myFriendsAdapter; // adapter za prikaz mojih prijatelja
    private MutableLiveData<List<DocumentSnapshot>> searchResults = new MutableLiveData<>(new ArrayList<>());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        setTitle("👥 Prijatelji");

        // 🔹 ViewModel
        vm = new ViewModelProvider(this).get(com.example.mobileapplication.ui.friends.FriendsViewModel.class);

//        // 🔹 Recycler za moje prijatelje (horizontalno)
//        RecyclerView recyclerMyFriends = findViewById(R.id.recyclerMyFriends);
//        recyclerMyFriends.setLayoutManager(
//                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        );

        myFriendsAdapter = new FriendsAdapter(
                new ArrayList<>(),
                userDoc -> vm.addFriend(userDoc.getId()), // ne koristi se ovde
                userDoc -> { // klik na PROFIL
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra("uid", userDoc.getId());
                    startActivity(intent);
                }
        );
        //recyclerMyFriends.setAdapter(myFriendsAdapter);

        // 🔹 Recycler za rezultate pretrage (vertikalno)
        RecyclerView recycler = findViewById(R.id.recyclerFriends);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FriendsAdapter(
                new ArrayList<>(),
                userDoc -> { // klik na DODAJ
                    String friendUid = userDoc.getId();
                    vm.addFriend(friendUid);
                    Toast.makeText(this, "✅ Prijatelj dodat!", Toast.LENGTH_SHORT).show();
                },
                userDoc -> { // klik na PROFIL
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra("uid", userDoc.getId());
                    startActivity(intent);
                }
        );
        recycler.setAdapter(adapter);

        // 🔹 Posmatranje promena u listi mojih prijatelja
        vm.friendDocs.observe(this, myFriendsAdapter::update);
        vm.friendIds.observe(this, ids -> {
            adapter.setCurrentFriends(ids); // da sakrije dugme "Dodaj" ako je već prijatelj
        });

        // 🔹 Učitavanje postojećih prijatelja
        vm.loadFriends();

        // 🔹 Pretraga korisnika
        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    vm.searchUsers(query, searchResults);
                } else {
                    adapter.update(new ArrayList<>()); // očisti listu kad se obriše tekst
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 🔹 Kad stignu rezultati pretrage, osveži adapter
        searchResults.observe(this, list -> adapter.update(list));

        findViewById(R.id.btnCreateAlliance).setOnClickListener(v -> showCreateAllianceDialog());

        findViewById(R.id.btnViewAlliance).setOnClickListener(v -> {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore.getInstance()
                    .collection("alliances")
                    .whereArrayContains("members", uid)
                    .get()
                    .addOnSuccessListener(qs -> {
                        if (qs.isEmpty()) {
                            Toast.makeText(this, "Nisi član nijednog saveza ⚔️", Toast.LENGTH_SHORT).show();
                        } else {
                            // ✅ Otvori prikaz saveza
                            Intent intent = new Intent(this, com.example.mobileapplication.ui.friends.AllianceActivity.class);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Greška pri proveri saveza", Toast.LENGTH_SHORT).show();
                    });
        });

    }

    private void showCreateAllianceDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Novi savez");

        final EditText input = new EditText(this);
        input.setHint("Unesi naziv saveza");
        builder.setView(input);

        builder.setPositiveButton("Kreiraj", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) return;

            String leaderUid = vm.getCurrentUserUid();
            vm.createAlliance(name, leaderUid);
        });

        builder.setNegativeButton("Otkaži", null);
        builder.show();
    }
}
