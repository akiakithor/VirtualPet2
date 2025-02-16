package com.example.virtualpet2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ShopActivity extends AppCompatActivity {
    private DatabaseReference petRef;
    private double foodCost = 5.0;  // Cost of food

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        Button buyFoodButton = findViewById(R.id.buyFoodButton);
        petRef = FirebaseDatabase.getInstance().getReference("pet");

        buyFoodButton.setOnClickListener(v -> {
            petRef.child("money").get().addOnSuccessListener(snapshot -> {
                double currentMoney = snapshot.exists() ? snapshot.getValue(Double.class) : 0.0;
                if (currentMoney >= foodCost) {
                    double newMoney = currentMoney - foodCost;
                    petRef.child("money").setValue(newMoney);
                    Toast.makeText(ShopActivity.this, "Bought food for $5.00", Toast.LENGTH_SHORT).show();

                    // Optionally, increase EXP when buying food
                    petRef.child("exp").get().addOnSuccessListener(expSnapshot -> {
                        double currentExp = expSnapshot.exists() ? expSnapshot.getValue(Double.class) : 0.0;
                        double newExp = currentExp + 10.0;  // Add 10 EXP for feeding the pet
                        petRef.child("exp").setValue(newExp);
                    });
                } else {
                    Toast.makeText(ShopActivity.this, "Not enough money to buy food", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
