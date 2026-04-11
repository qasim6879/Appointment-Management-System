package org.example;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Administrator user with higher access level to manage appointments.
 */
public class Administrator extends User {

    /**
     * constructor for the administrator
     */
    public Administrator(String username, String email, String password) {
        super(username, email, password);
    }

    /**
     * Books an appointment for a user.
     */
    @Override
    public void bookAppointment(String userUsername, LocalDate date, LocalTime startTime, int duration, AppointmentType type){
        List<Appointment> appointments = JsonHandler.loadList("appointments.json", Appointment.class);
        User targetUser = User.getUserObject(userUsername);

        Appointment appt = new Appointment(targetUser, this, date, startTime, duration, type, AppointmentStatus.CONFIRMED);
        appointments.add(appt);
        JsonHandler.saveList(appointments, "appointments.json");

        Notification.addNotification("Admin booked an appointment for you on " + date, targetUser, this, NotificationType.CONFIRMATION);
        Notification.addNotification("You booked an appointment for " + userUsername, this, this, NotificationType.CONFIRMATION);
    }

    /**
     * Edits an existing appointment, by updating its parameters based on input.
     */
    public void editAppointment(Appointment appt, LocalDate newDate, LocalTime newTime, int newDuration, AppointmentType newType) {
        List<Appointment> allAppointments = JsonHandler.loadList("appointments.json", Appointment.class);

        for (Appointment obj : allAppointments) {
            if (obj.getAdmin().getUsername().equals(appt.getAdmin().getUsername())
                    && obj.getDate().equals(appt.getDate())
                    && obj.getStartTime().equals(appt.getStartTime())) {

                obj.setDate(newDate);
                obj.setDuration(newDuration);
                obj.setStartTime(newTime);
                obj.setType(newType);
                break;
            }
        }

        JsonHandler.saveList(allAppointments, "appointments.json");

        ObserverManager.notifyObservers("Appointment updated to " + newDate + " at " + newTime,
                appt.getUser(), this, NotificationType.CONFIRMATION);
    }

    /**
     * Confirms an appointment, changing its status to CONFIRMED
     */
    public static void confirmAppointment(Appointment appt) {
        List<Appointment> allAppointments = JsonHandler.loadList("appointments.json", Appointment.class);

        for (Appointment obj : allAppointments) {
            if (obj.getAdmin().getUsername().equals(appt.getAdmin().getUsername())
                    && obj.getDate().equals(appt.getDate())
                    && obj.getStartTime().equals(appt.getStartTime())) {

                obj.setStatus(AppointmentStatus.CONFIRMED);
                break;
            }
        }

        JsonHandler.saveList(allAppointments, "appointments.json");

        ObserverManager.notifyObservers("Appointment confirmed on " + appt.getDate(),
                appt.getUser(), appt.getAdmin(), NotificationType.CONFIRMATION);
    }

    /**
     * Finds an administrator by username.
     * @return the administrator object if found
     */
    public static Administrator getAdministratorObject(String username) {
        List<Administrator> admins = JsonHandler.loadList("admins.json", Administrator.class);

        for (Administrator obj : admins) {
            if (obj.getUsername().equals(username)) return obj;
        }
        return null;
    }

    /**
     * Default constructor.
     */
    public Administrator() {}
}