package com.step.cinemate.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.step.cinemate.Data.NotificationItem;
import com.step.cinemate.R;
import com.step.cinemate.Services.BackendService;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notificationList;
    private NotificationClickListener listener;

    public NotificationAdapter(List<NotificationItem> notificationList, NotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    public interface NotificationClickListener {
        void onMarkAsReadClicked(UUID id);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);

        holder.notificationText.setText(item.getText());
        holder.notificationTime.setText(item.getTime());
        if (item.getIsRead()) {
            holder.markAsReadButton.setVisibility(View.GONE);
            holder.item_notificationLinearLayout.setBackgroundColor(Color.parseColor("#333333"));
        } else {
            holder.markAsReadButton.setVisibility(View.VISIBLE);
            holder.item_notificationLinearLayout.setBackgroundColor(Color.parseColor("#FF0000"));
        }


        holder.markAsReadButton.setOnClickListener(v -> {
            listener.onMarkAsReadClicked(item.getId());
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationText, notificationTime;
        Button markAsReadButton;
        LinearLayout item_notificationLinearLayout;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notificationText = itemView.findViewById(R.id.notificationText);
            notificationTime = itemView.findViewById(R.id.notificationTime);
            markAsReadButton = itemView.findViewById(R.id.markAsReadButton);
            item_notificationLinearLayout = itemView.findViewById(R.id.item_notificationLinearLayout);
        }
    }
}
