package com.example.virtualpet2;

import android.content.Intent;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private int level = 1;
    private double exp = 0;
    private double expRate = 1.0;
    private double expNeeded = 100;
    private double money = 0.0;
    private TextView petLevel, petExp, moneyBalance;
    private DatabaseReference petRef;
    private DatabaseReference tasksRef;
    private List<Food> foodInventory;
    private FoodInventoryAdapter foodInventoryAdapter;
    private RecyclerView foodInventoryRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        petLevel = findViewById(R.id.petLevel);
        petExp = findViewById(R.id.petExp);
        moneyBalance = findViewById(R.id.moneyBalance);
        ImageView petImage = findViewById(R.id.petImage);
        ImageView chatButton = findViewById(R.id.chatButton);
        ImageView taskButton = findViewById(R.id.taskButton);
        ImageView shopButton = findViewById(R.id.shopButton);

        petRef = FirebaseDatabase.getInstance().getReference("pet");
        tasksRef = FirebaseDatabase.getInstance().getReference("pet").child("tasks");

        foodInventoryRecyclerView = findViewById(R.id.foodInventoryRecyclerView);
        foodInventory = new ArrayList<>();
        foodInventory.add(new Food("Apple", 10.0, 10.0));
        foodInventory.add(new Food("Steak", 30.0, 30.0));
        foodInventory.add(new Food("Fish", 20.0, 20.0));

        foodInventoryAdapter = new FoodInventoryAdapter(foodInventory);
        foodInventoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        foodInventoryRecyclerView.setAdapter(foodInventoryAdapter);

        petImage.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                Food droppedFood = (Food) event.getLocalState();
                feedPet(droppedFood.getExpGain());
            }
            return true;
        });

        petImage.setOnClickListener(v -> {
            exp += expRate;
            if (exp >= expNeeded) {
                exp -= expNeeded;
                level++;
                expRate = 1.0 + (level - 1) * 0.1;
                expNeeded = 100 * level;
            }
            petLevel.setText("Level: " + level);
            petExp.setText("EXP: " + String.format("%.1f", exp) + " / " + expNeeded);
            petRef.child("level").setValue(level);
            petRef.child("exp").setValue(exp);
            petRef.child("expRate").setValue(expRate);
            petRef.child("expNeeded").setValue(expNeeded);

            // Increment pet taps in tasks
            tasksRef.child("petTaps").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int petTaps = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                    tasksRef.child("petTaps").setValue(petTaps + 1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        });

        chatButton.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));
        taskButton.setOnClickListener(v -> startActivity(new Intent(this, TaskActivity.class)));
        shopButton.setOnClickListener(v -> startActivity(new Intent(this, ShopActivity.class)));

        petRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    level = snapshot.child("level").getValue(Integer.class) != null ? snapshot.child("level").getValue(Integer.class) : 1;
                    exp = snapshot.child("exp").getValue(Double.class) != null ? snapshot.child("exp").getValue(Double.class) : 0.0;
                    expRate = snapshot.child("expRate").getValue(Double.class) != null ? snapshot.child("expRate").getValue(Double.class) : 1.0;
                    expNeeded = snapshot.child("expNeeded").getValue(Double.class) != null ? snapshot.child("expNeeded").getValue(Double.class) : 100.0;
                    money = snapshot.child("money").getValue(Double.class) != null ? snapshot.child("money").getValue(Double.class) : 0.0;
                    petLevel.setText("Level: " + level);
                    petExp.setText("EXP: " + String.format("%.1f", exp) + " / " + expNeeded);
                    moneyBalance.setText("Money: $" + String.format("%.2f", money));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void feedPet(double expGain) {
        exp += expGain;
        if (exp >= expNeeded) {
            exp -= expNeeded;
            level++;
            expRate = 1.0 + (level - 1) * 0.1;
            expNeeded = 100 * level;
        }
        petLevel.setText("Level: " + level);
        petExp.setText("EXP: " + String.format("%.1f", exp) + " / " + expNeeded);
        petRef.child("level").setValue(level);
        petRef.child("exp").setValue(exp);
        petRef.child("expRate").setValue(expRate);
        petRef.child("expNeeded").setValue(expNeeded);
    }
}
