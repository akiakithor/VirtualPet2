package com.example.virtualpet2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

<<<<<<< HEAD
import android.Manifest;

=======
>>>>>>> parent of 9abdde0 (done notif feature)
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
        foodInventory.add(new Food("Apple", 10.0, 10.0, R.drawable.apple));
        foodInventory.add(new Food("Steak", 30.0, 50.0, R.drawable.steak));
        foodInventory.add(new Food("Fish", 20.0, 20.0, R.drawable.fish));
        foodInventory.add(new Food("Banana", 20.0, 30.0, R.drawable.banana));
        foodInventory.add(new Food("Watermelon", 40.0, 60.0, R.drawable.watermelon));

        foodInventoryAdapter = new FoodInventoryAdapter(foodInventory);
        foodInventoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        foodInventoryRecyclerView.setAdapter(foodInventoryAdapter);

        petImage.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                Food droppedFood = (Food) event.getLocalState();
                feedPet(droppedFood.getExpGain());

                // Play the corresponding GIF based on the food type
                if (droppedFood.getName().equals("Steak")) {
                    playEatingGif(petImage, R.drawable.eatsteak);
                } else if (droppedFood.getName().equals("Fish")) {
                    playEatingGif(petImage, R.drawable.eatfish);
                } else if (droppedFood.getName().equals("Banana")) {
                    playEatingGif(petImage, R.drawable.eatbanana);
                } else if (droppedFood.getName().equals("Watermelon")) {
                    playEatingGif(petImage, R.drawable.eatmelon);
                } else if (droppedFood.getName().equals("Apple")) {
                    playEatingGif(petImage, R.drawable.eatapple);
                }
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

            // Play the cat GIF when the pet is tapped
            playCatGif(petImage);

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

        // Set idle GIF for the pet when the activity starts
        Glide.with(this)
                .asGif()
                .load(R.drawable.idle) // Replace with your idle GIF resource
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(petImage);
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

    private void playCatGif(ImageView petImage) {
        Glide.with(this)
                .asGif()
                .load(R.drawable.cat) // Replace with your cat GIF resource
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(petImage);

        // Reset to idle GIF after 3 seconds
        new Handler().postDelayed(() -> {
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.idle) // Replace with your idle GIF resource
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(petImage);
        }, 3000); // 3 seconds delay
    }

    private void playEatingGif(ImageView petImage, int gifResource) {
        Glide.with(this)
                .asGif()
                .load(gifResource)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(petImage);

        // Reset to idle GIF after 3 seconds
        new Handler().postDelayed(() -> {
            Glide.with(this)
                    .asGif()
                    .load(R.drawable.idle) // Replace with your idle GIF resource
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(petImage);
        }, 3000); // 3 seconds delay
    }
}
