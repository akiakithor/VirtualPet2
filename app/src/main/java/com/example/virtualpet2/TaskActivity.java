package com.example.virtualpet2;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class TaskActivity extends AppCompatActivity {
    private DatabaseReference petRef;
    private static final double REWARD_AMOUNT = 10.0;
    private static final long RESET_INTERVAL = 30 * 60 * 1000; // 30 minutes in milliseconds

    private int messagesSent = 0;
    private int petTaps = 0;
    private long lastClaimTime = 0;
    private long resetTime = 0;

    private TextView messageTaskStatus, petTapTaskStatus, resetTimerText;
    private Button claimMessageTaskButton, claimPetTapTaskButton;
    private CountDownTimer resetTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        petRef = FirebaseDatabase.getInstance().getReference("pet");

        messageTaskStatus = findViewById(R.id.messageTaskStatus);
        petTapTaskStatus = findViewById(R.id.petTapTaskStatus);
        resetTimerText = findViewById(R.id.resetTimerText);
        claimMessageTaskButton = findViewById(R.id.claimMessageTaskButton);
        claimPetTapTaskButton = findViewById(R.id.claimPetTapTaskButton);

        loadTaskData();

        claimMessageTaskButton.setOnClickListener(v -> claimReward("messagesSent", 5));
        claimPetTapTaskButton.setOnClickListener(v -> claimReward("petTaps", 50));

        loadResetTime();
    }

    private void loadTaskData() {
        petRef.child("tasks").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                messagesSent = snapshot.child("messagesSent").getValue(Integer.class) != null ? snapshot.child("messagesSent").getValue(Integer.class) : 0;
                petTaps = snapshot.child("petTaps").getValue(Integer.class) != null ? snapshot.child("petTaps").getValue(Integer.class) : 0;
                lastClaimTime = snapshot.child("lastClaimTime").getValue(Long.class) != null ? snapshot.child("lastClaimTime").getValue(Long.class) : 0;
            }
            updateTaskStatus();
        });
    }

    private void claimReward(String taskType, int requiredCount) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastClaimTime < RESET_INTERVAL) {
            Toast.makeText(TaskActivity.this, "You can only claim rewards once every 30 minutes!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (taskType.equals("messagesSent") && messagesSent >= requiredCount) {
            rewardUser();
            messagesSent = 0;
            petRef.child("tasks/messagesSent").setValue(messagesSent);
            Toast.makeText(TaskActivity.this, "Message task completed! Earned $10.00", Toast.LENGTH_SHORT).show();
        } else if (taskType.equals("petTaps") && petTaps >= requiredCount) {
            rewardUser();
            petTaps = 0;
            petRef.child("tasks/petTaps").setValue(petTaps);
            Toast.makeText(TaskActivity.this, "Pet tap task completed! Earned $10.00", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(TaskActivity.this, "Task not complete yet!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update last claim time and save to Firebase
        lastClaimTime = currentTime;
        petRef.child("tasks/lastClaimTime").setValue(lastClaimTime);

        updateTaskStatus();
    }

    private void rewardUser() {
        petRef.child("money").get().addOnSuccessListener(snapshot -> {
            double currentMoney = snapshot.exists() && snapshot.getValue(Double.class) != null ? snapshot.getValue(Double.class) : 0.0;
            double newMoney = currentMoney + REWARD_AMOUNT;
            petRef.child("money").setValue(newMoney);
        });
    }

    private void updateTaskStatus() {
        messageTaskStatus.setText("Messages sent: " + messagesSent + " / 5");
        petTapTaskStatus.setText("Pet taps: " + petTaps + " / 50");
    }

    private void loadResetTime() {
        petRef.child("tasks/resetTime").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                resetTime = snapshot.getValue(Long.class);
                long currentTime = System.currentTimeMillis();
                long remainingTime = resetTime - currentTime;

                if (remainingTime > 0) {
                    startResetTimer(remainingTime);
                } else {
                    resetTasks();
                }
            } else {
                // If resetTime doesn't exist, set a new one
                resetTime = System.currentTimeMillis() + RESET_INTERVAL;
                petRef.child("tasks/resetTime").setValue(resetTime);
                startResetTimer(RESET_INTERVAL);
            }
        });
    }

    private void startResetTimer(long remainingTime) {
        resetTimer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timeLeft = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60);
                resetTimerText.setText("Time until reset: " + timeLeft);
            }

            @Override
            public void onFinish() {
                resetTasks();
            }
        }.start();
    }

    private void resetTasks() {
        messagesSent = 0;
        petTaps = 0;
        petRef.child("tasks/messagesSent").setValue(messagesSent);
        petRef.child("tasks/petTaps").setValue(petTaps);

        // Set new reset time
        resetTime = System.currentTimeMillis() + RESET_INTERVAL;
        petRef.child("tasks/resetTime").setValue(resetTime);

        updateTaskStatus();
        Toast.makeText(TaskActivity.this, "Tasks have been reset!", Toast.LENGTH_SHORT).show();

        // Start a new timer
        startResetTimer(RESET_INTERVAL);
    }
}
