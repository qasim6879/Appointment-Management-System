package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
public class Notification {
    private String dateSent;
    private String message;
    private boolean active;
    private User user;
   private Administrator admin;
   private NotificationType type;
    
  
    public Notification(String message, boolean active,User user, Administrator admin, NotificationType type) {
        this.message = message;
        this.active = active;
        this.user = user;
        this.admin = admin;
        this.type = type;

       this.dateSent = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
   
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Administrator getAdmin() { return admin; }
    public void setAdmin(Administrator admin) { this.admin = admin; }
    
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    
    
   
    public String getDateSent() { return dateSent; }
    public void setDateSent(String dateSent) { this.dateSent = dateSent; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public static List<Notification> getNotifications(String username) {
    	
    List<Notification> notifications = JsonHandler.loadList("notifications.json", Notification.class);
    	List<Notification> userNotifications = new ArrayList<>();
		for (int i = 0; i < notifications.size(); i++) {
			if (notifications.get(i).user.getUsername().equals(username)) {
				userNotifications.add(notifications.get(i));
			}
			
		}
		return userNotifications;
	}
    
    
    
    public static void deleteNotification(Notification toDelete) {
        List<Notification> all = JsonHandler.loadList("notifications.json", Notification.class);
        all.removeIf(n ->
            n.getUser().getUsername().equals(toDelete.getUser().getUsername()) &&
            n.getMessage().equals(toDelete.getMessage()) &&
            n.getDateSent().equals(toDelete.getDateSent())
        );
        JsonHandler.saveList(all ,"notifications.json");
    }
    
}