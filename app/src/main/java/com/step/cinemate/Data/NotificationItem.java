package com.step.cinemate.Data;

import java.util.UUID;

public class NotificationItem {
    private UUID id;
    private String text;
    private String time;
    private boolean isRead;

    public NotificationItem(UUID id, String text, String time, boolean isRead) {
        this.id = id;
        this.text = text;
        this.time = time;
        this.isRead = isRead;
    }

    public UUID getId() {
        return id;
    }
    public boolean getIsRead() {
        return isRead;
    }
    public void setIsRead(boolean flag) {
        isRead = flag;
    }
    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }
}
