package org.example;

public interface NotificationObserver {
    void update(String message, User user, Administrator admin, NotificationType type);
}