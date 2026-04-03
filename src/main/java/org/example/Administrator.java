package org.example;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Administrator extends User {
    public Administrator(String username, String email, String password) {
        super(username, email, password);
    }

    @Override
    public void bookAppointment(String userUsername, LocalDate date, LocalTime startTime, int duration, AppointmentType type){
        List<Appointment> appointments = JsonHandler.loadList("appointments.json", Appointment.class);
        User targetUser = User.getUserObject(userUsername);

        // حجز مؤكد تلقائياً لأن الإدمن هو من قام به
        Appointment appt = new Appointment(targetUser, this, date, startTime, duration, type, AppointmentStatus.CONFIRMED);
        appointments.add(appt);
        JsonHandler.saveList(appointments, "appointments.json");

        // إرسال إشعارين لملف JSON
        List<Notification> notifications = JsonHandler.loadList("notifications.json", Notification.class);
        // 1. نسخة لليوزر
        notifications.add(new Notification("Administrator " + this.getUsername() + " has booked an appointment for you on " + date, true, targetUser, this, NotificationType.CONFIRMATION));
        // 2. نسخة للإدمن
        notifications.add(new Notification("You have booked an appointment for user: " + userUsername, true, this, this, NotificationType.CONFIRMATION));
        
        JsonHandler.saveList(notifications, "notifications.json");
    }

    public void editAppointment(Appointment appt, LocalDate newDate, LocalTime newTime, int newDuration, AppointmentType newType) {
        List<Appointment> allAppointments = JsonHandler.loadList("appointments.json", Appointment.class);
        for (Appointment obj : allAppointments) {
            if (obj.getAdmin().getUsername().equals(appt.getAdmin().getUsername())
                    && obj.getDate().equals(appt.getDate()) && obj.getStartTime().equals(appt.getStartTime())) {
                obj.setDate(newDate);
                obj.setDuration(newDuration);
                obj.setStartTime(newTime);
                obj.setType(newType);
                break;
            }
        }
        JsonHandler.saveList(allAppointments, "appointments.json");

        List<Notification> notifications = JsonHandler.loadList("notifications.json", Notification.class);
        notifications.add(new Notification("Admin updated your appointment to: " + newDate + " at " + newTime, true, appt.getUser(), this, NotificationType.CONFIRMATION));
        notifications.add(new Notification("You updated appointment for " + appt.getUser().getUsername(), true, this, this, NotificationType.CONFIRMATION));
        JsonHandler.saveList(notifications, "notifications.json");
    }

    public static void confirmAppointment(Appointment appt) {
        List<Appointment> allAppointments = JsonHandler.loadList("appointments.json", Appointment.class);
        for (Appointment obj : allAppointments) {
            if (obj.getAdmin().getUsername().equals(appt.getAdmin().getUsername())
                    && obj.getDate().equals(appt.getDate()) && obj.getStartTime().equals(appt.getStartTime())) {
                obj.setStatus(AppointmentStatus.CONFIRMED);
                break;
            }
        }
        JsonHandler.saveList(allAppointments, "appointments.json");

        List<Notification> notifications = JsonHandler.loadList("notifications.json", Notification.class);
        notifications.add(new Notification("Admin confirmed your appointment on " + appt.getDate(), true, appt.getUser(), appt.getAdmin(), NotificationType.CONFIRMATION));
        notifications.add(new Notification("You confirmed appointment for " + appt.getUser().getUsername(), true, appt.getAdmin(), appt.getAdmin(), NotificationType.CONFIRMATION));
        JsonHandler.saveList(notifications, "notifications.json");
    }

    public static Administrator getAdministratorObject(String username) {
        List<Administrator> admins = JsonHandler.loadList("admins.json", Administrator.class);
        for (Administrator obj : admins) {
            if (obj.getUsername().equals(username)) return obj;
        }
        return null;
    }

    public Administrator() {}
}