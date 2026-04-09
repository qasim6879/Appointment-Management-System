package org.example;

import java.util.ArrayList;
import java.util.List;

public class ObserverManager {

    private static List<NotificationObserver> observers = new ArrayList<>();

    public static void addObserver(NotificationObserver o) {
        observers.add(o);
    }

    public static void notifyObservers(String message, User user, Administrator admin, NotificationType type) {
        for (NotificationObserver o : observers) {
            o.update(message, user, admin, type);
        }
    }
}