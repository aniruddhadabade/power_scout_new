package com.example.powerscout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>{
    private List<String> notificationList;  // This will hold your notification data

    // Constructor to initialize the notification list
    public NotificationAdapter(List<String> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each notification item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);  // Return a new view holder
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        // Set the notification text
        holder.notificationText.setText(notificationList.get(position));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();  // Return the size of the notification list
    }

    // ViewHolder class to hold the references for the views in each item
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationText;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            notificationText = itemView.findViewById(R.id.notificationText);  // Reference to the TextView in notification_item.xml
        }
    }
}
