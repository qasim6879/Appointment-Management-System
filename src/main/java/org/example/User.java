package org.example;

import java.time.*;
import java.util.*;

public class User {
    public String username;
    public String email;
    public String password;
    
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String Username) { this.username = Username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public User() {}

    public void bookAppointment(String adminUsername, LocalDate date, LocalTime startTime, int duration, AppointmentType type){
        List<Appointment> appointments = JsonHandler.loadList("appointments.json", Appointment.class);
        Administrator admin = Administrator.getAdministratorObject(adminUsername);

        Appointment appt = new Appointment(this, admin, date, startTime, duration, type, AppointmentStatus.PENDING);
        appointments.add(appt);
        JsonHandler.saveList(appointments, "appointments.json");

        ObserverManager.notifyObservers("Request sent to Admin: " + adminUsername + ". Status: Pending.", this, admin, NotificationType.CONFIRMATION);
        ObserverManager.notifyObservers("New booking request from " + this.username + " on " + date, admin, admin, NotificationType.REMINDER);
    }

    public void cancelAppointment(Appointment appt){
        List <Appointment> appts = JsonHandler.loadList("appointments.json", Appointment.class);
        for(Appointment obj : appts){
            if (obj.getAdmin().getUsername().equals(appt.getAdmin().getUsername()) && obj.getDate().equals(appt.getDate()) && obj.getStartTime().equals( appt.getStartTime())) {
                obj.setStatus(AppointmentStatus.CANCELLED);
                JsonHandler.saveList(appts, "appointments.json");

                String msgForUser = (this instanceof Administrator) ? "Admin cancelled your appointment on " + appt.getDate() : "You cancelled your appointment on " + appt.getDate();
                String msgForAdmin = (this instanceof Administrator) ? "You cancelled appointment for " + appt.getUser().getUsername() : "User " + this.username + " cancelled their appointment on " + appt.getDate();

                ObserverManager.notifyObservers(msgForUser, appt.getUser(), appt.getAdmin(), NotificationType.CANCELLATION);
                ObserverManager.notifyObservers(msgForAdmin, appt.getAdmin(), appt.getAdmin(), NotificationType.CANCELLATION);

                return;
            }
        }
    }

    public static User getUserObject(String username){
        List <User> users = JsonHandler.loadList("users.json", User.class);
        for (User obj: users){ if (obj.getUsername().equals(username)) return obj; }
        return null;
    }

    public ArrayList <Appointment> getUserAppointments(){
        List <Appointment> allAppts = JsonHandler.loadList("appointments.json", Appointment.class);
        ArrayList <Appointment> userAppts = new ArrayList <Appointment> ();
        for (Appointment obj : allAppts) {
            if (this instanceof Administrator && obj.getAdmin().getUsername().equals(this.getUsername())) userAppts.add(obj);
            else if (obj.getUser().getUsername().equals(this.getUsername())) userAppts.add(obj);
        }
        return userAppts;
    }

    public static Boolean signIn(String username, String password, String fileName ){
        List<User> Users = JsonHandler.loadList(fileName, User.class);
        for (User u : Users) { if (u.username.equals(username) && u.password.equals(password)) return true; }
        return false;
    }

    public static int signUp(String username, String email, String password, String fileName){
        List<User> Users = JsonHandler.loadList(fileName, User.class);
        if (!(email.contains("@") && email.contains("."))) return 2;
        for (User u : Users) { if (u.username.equals(username)) return 1; if (u.email.equals(email)) return 2; }
        Users.add(new User(username, email, password));
        JsonHandler.saveList(Users, fileName);
        return 0;
    }
}