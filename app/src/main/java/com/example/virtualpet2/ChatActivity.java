package com.example.virtualpet2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageInput;
    private Button logoutButton;  // Change to Button
    private ImageView sendButton;
    private ChatAdapter chatAdapter;
    private List<Message> messageList = new ArrayList<>();
    private DatabaseReference chatRef;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Retrieve the logged-in user's name
        SharedPreferences preferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userName = preferences.getString("userName", "Anonymous");

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        logoutButton = findViewById(R.id.logoutButton);  // Correct the type here

        chatRef = FirebaseDatabase.getInstance().getReference("messages");

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
                // Clear the input field
                messageInput.setText("");

                // Increment the messagesSent count
                DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("pet/tasks/messagesSent");
                taskRef.get().addOnSuccessListener(snapshot -> {
                    int currentMessagesSent = snapshot.exists() && snapshot.getValue(Integer.class) != null ? snapshot.getValue(Integer.class) : 0;
                    taskRef.setValue(currentMessagesSent + 1);
                }).addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Failed to update messages count!", Toast.LENGTH_SHORT).show();
                });
            }
        });

        // Logout button functionality
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
}
