package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Notification implements NotificationObserver{
    private String dateSent;
    private String message;
    private boolean active;
    private User user;
    private Administrator admin; 
    private NotificationType type;
    
    public Notification(String message, boolean active, User user, Administrator admin, NotificationType type) {
        this.message = message;
        this.active = active;
        this.user = user;
        this.admin = admin;
        this.type = type;
        this.dateSent = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public Notification() {

    }

    public User getUser() { return user; }
    public Administrator getAdmin() { return admin; }
    public NotificationType getType() { return type; }
    public String getDateSent() { return dateSent; }
    public String getMessage() { return message; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }


    public static List<Notification> getNotifications(String username) {
        List<Notification> all = JsonHandler.loadList("notifications.json", Notification.class);
        List<Notification> filtered = new ArrayList<>();
        for (Notification n : all) {
            if (n != null && n.getUser() != null && n.getUser().getUsername().equals(username)) {
                filtered.add(n);
            }
        }
        return filtered;
    }
    
    public static void deleteNotification(Notification toDelete) {
        List<Notification> all = JsonHandler.loadList("notifications.json", Notification.class);

        all.removeIf(n -> n != null && 
                          toDelete != null &&
                          Objects.equals(n.getMessage(), toDelete.getMessage()) && 
                          Objects.equals(n.getDateSent(), toDelete.getDateSent()));
                          
        JsonHandler.saveList(all, "notifications.json");
    }

    public static void deleteAllNotifications(String username) {
        List<Notification> all = JsonHandler.loadList("notifications.json", Notification.class);

        all.removeIf(n -> n != null && n.getUser() != null && n.getUser().getUsername().equals(username));
        
        JsonHandler.saveList(all, "notifications.json");
    }

    public static void addNotification(String message, User user, Administrator admin, NotificationType type){
        List <Notification> notifs = JsonHandler.loadList("notifications.json", Notification.class);

        Notification notification = new Notification(message, true, user, admin, type);

        notifs.add(notification);
        JsonHandler.saveList(notifs, "notifications.json");
    }

    @Override
    public void update(String message, User user, Administrator admin, NotificationType type) {
        addNotification(message, user, admin, type);
    }
}