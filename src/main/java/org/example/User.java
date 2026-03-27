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

	// setters:
	public void setUsername(String Username) {
		this.username = Username;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	// getters:
	public String getUsername() {
		return username;
	}
	public String getEmail() {
		return email;
	}
	public String getPassword() {
		return password;
	}

	public User() {
	}

	public static Boolean signIn(String username, String password, String fileName ){
		List<User> Users = JsonHandler.loadList(fileName, User.class);

		for (int i = 0; i < Users.size(); i++){
			if (Users.get(i).username.equals(username) && Users.get(i).password.equals(password))
				return true;
		}
		return false;
	}

	public static int signUp(String username, String email, String password, String fileName){
		List<User> Users = JsonHandler.loadList(fileName, User.class);

		if (!(email.contains("@") && email.contains("."))){
			return 2;
		}

		for (int i = 0; i < Users.size(); i++){
			if (Users.get(i).username.equals(username))
				return 1;
			if (Users.get(i).email.equals(email))
				return 2;
		}

		User a = new User(username, email, password);
		Users.add(a);

		JsonHandler.saveList(Users, fileName);
		return 0;
	}

	public void bookAppointment(String adminUsername, LocalDate date, LocalTime startTime, int duration, AppointmentType type){
	    List<Appointment> appointments = JsonHandler.loadList("appointments.json", Appointment.class);
	    Administrator admin = Administrator.getAdministratorObject(adminUsername);
	    Appointment appt = new Appointment(this, admin, date, startTime, duration, type, AppointmentStatus.PENDING);
	    appointments.add(appt);
	    JsonHandler.saveList(appointments, "Appointments.json");

	    // Create and save notification
	    List<Notification> notifications = JsonHandler.loadList("notifications.json", Notification.class);
	    Notification notif = new Notification(
	        "Your " + type.toString() + " appointment with " + adminUsername +
	        " on " + date.toString() + " at " + startTime.toString() + " has been requested. Status: Pending.",
	        true,
	        this,
	        admin,
	        NotificationType.CONFIRMATION
	    );
	    notifications.add(notif);
	    JsonHandler.saveList(notifications, "notifications.json");
	}

	public static User getUserObject(String username){
		List <User> users = JsonHandler.loadList("users.json", User.class);
		for (User obj: users){
			if (obj.getUsername().equals(username))
				return obj;
		}
		return null;
	}

	public ArrayList <Appointment> getUserAppointments(){
		List <Appointment> allAppts = JsonHandler.loadList("appointments.json", Appointment.class);
		ArrayList <Appointment> userAppts = new ArrayList <Appointment> ();
		for (Appointment obj : allAppts){
			if (obj.getUser().getUsername().equals(this.getUsername()))
				userAppts.add(obj);
		}
		return userAppts;
	}

	public void cancelAppointment(Appointment appt){
		List <Appointment> appts = JsonHandler.loadList("appointments.json", Appointment.class);

		for(Appointment obj : appts){
			if (obj.getAdmin().getUsername().equals(appt.getAdmin().getUsername()) && obj.getDate().equals(appt.getDate()) && obj.getStartTime().equals( appt.getStartTime())) {
				obj.setStatus(AppointmentStatus.CANCELLED);
				JsonHandler.saveList(appts, "appointments.json");
				return;
			}
		}
	}
}