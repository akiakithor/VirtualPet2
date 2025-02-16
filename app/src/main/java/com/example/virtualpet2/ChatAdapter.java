package com.example.virtualpet2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.text.format.DateFormat;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Message> messages;
    private String currentUser;

    public ChatAdapter(List<Message> messages, String currentUser) {
        this.messages = messages;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messages.get(position);

        // Format the timestamp
        String formattedTimestamp = DateFormat.format("hh:mm a", message.getTimestamp()).toString();

        if (message.getSender().equals(currentUser)) {
            holder.userMessage.setText(message.getMessage());
            holder.userMessageTimestamp.setText(formattedTimestamp);
            holder.userMessage.setVisibility(View.VISIBLE);
            holder.userMessageTimestamp.setVisibility(View.VISIBLE);
            holder.otherMessage.setVisibility(View.GONE);
            holder.otherMessageTimestamp.setVisibility(View.GONE);
        } else {
            holder.otherMessage.setText(message.getSender() + ": " + message.getMessage());
            holder.otherMessageTimestamp.setText(formattedTimestamp);
            holder.otherMessage.setVisibility(View.VISIBLE);
            holder.otherMessageTimestamp.setVisibility(View.VISIBLE);
            holder.userMessage.setVisibility(View.GONE);
            holder.userMessageTimestamp.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView userMessage, userMessageTimestamp, otherMessage, otherMessageTimestamp;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessage = itemView.findViewById(R.id.userMessage);
            userMessageTimestamp = itemView.findViewById(R.id.userMessageTimestamp);
            otherMessage = itemView.findViewById(R.id.otherMessage);
            otherMessageTimestamp = itemView.findViewById(R.id.otherMessageTimestamp);
        }
    }
}
