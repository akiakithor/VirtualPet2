package com.example.virtualpet2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageInput;
    private Button logoutButton;
    private ImageView sendButton;
    private ChatAdapter chatAdapter;
    private List<Message> messageList = new ArrayList<>();
    private DatabaseReference chatRef;
    private DatabaseReference petRef;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
<<<<<<< HEAD
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        Intent serviceIntent = new Intent(this, PetBackgroundService.class);
        startService(serviceIntent);
=======
>>>>>>> parent of 9abdde0 (done notif feature)

        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userName = preferences.getString("userName", "Anonymous");

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        logoutButton = findViewById(R.id.logoutButton);

        chatRef = FirebaseDatabase.getInstance().getReference("messages");
        petRef = FirebaseDatabase.getInstance().getReference("pet");

        chatAdapter = new ChatAdapter(messageList, userName);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                long timestamp = System.currentTimeMillis();
                Message message = new Message(userName, messageText, timestamp);

                // Push the message to Firebase
                chatRef.push().setValue(message);
                messageInput.setText("");

                // Increment the messagesSent count
                DatabaseReference taskRef = petRef.child("tasks/messagesSent");
                taskRef.get().addOnSuccessListener(snapshot -> {
                    int currentMessagesSent = snapshot.exists() && snapshot.getValue(Integer.class) != null ? snapshot.getValue(Integer.class) : 0;
                    taskRef.setValue(currentMessagesSent + 1);
                });

                // Show notification with pet stats and the latest message
                showPetStatusNotification(messageText);
            }
        });

        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    messageList.add(message);
                }
                chatAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showPetStatusNotification(String latestMessage) {
        petRef.child("pet").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                double health = snapshot.child("health").getValue(Double.class) != null ? snapshot.child("health").getValue(Double.class) : 100.0;
                double happiness = snapshot.child("happiness").getValue(Double.class) != null ? snapshot.child("happiness").getValue(Double.class) : 100.0;

                String channelId = "pet_status_channel";
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(channelId, "Pet Status", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                }

                Intent intent = new Intent(this, TaskActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_pet_status) // Replace with your drawable resource
                        .setContentTitle("Pet Status Update")
                        .setContentText("Health: " + health + ", Happiness: " + happiness + ". Latest message: " + latestMessage)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                notificationManager.notify(1, builder.build());
            }
        });
    }
}
