package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {
    private String dateSent;
    private String message;
    private boolean active;

    public Notification() {}
       

    public Notification(String message, boolean active) {
        this.message = message;
        this.active = active;
        
        this.dateSent = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    
    public String getDateSent() { return dateSent; }
    public void setDateSent(String dateSent) { this.dateSent = dateSent; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}