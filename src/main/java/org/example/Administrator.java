package org.example;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Administrator extends User{
	public Administrator(String username,String email,String password) {
		super(username, email, password);
	}

	@Override
	public void bookAppointment(String userUsername, LocalDate date, LocalTime startTime, int duration, AppointmentType type){
		List<Appointment> appointments = JsonHandler.loadList("appointments.json", Appointment.class);
		User user = User.getUserObject(userUsername);

		Appointment appt = new Appointment(user, this, date, startTime, duration, type, AppointmentStatus.CONFIRMED);
		appointments.add(appt);

		JsonHandler.saveList(appointments, "Appointments.json");
	}

	public static Administrator getAdministratorObject(String username) {
		List<Administrator> admins = JsonHandler.loadList("admins.json", Administrator.class);
		for (Administrator obj : admins) {
			if (obj.getUsername().equals(username))
				return obj;
		}
		return null;
	}
	
	public void editAppointment(Appointment appt, LocalDate newDate, LocalTime newTime, int newDuration, AppointmentType newType) {
		List<Appointment> allAppointments = JsonHandler.loadList("Appointments.json", Appointment.class);
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
		
		JsonHandler.saveList(allAppointments, "Appointments.json");
	}

	public Administrator() {}

	public static void confirmAppointment(Appointment appt) {
		List<Appointment> allAppointments = JsonHandler.loadList("Appointments.json", Appointment.class);
		for (Appointment obj : allAppointments) {
			if (obj.getAdmin().getUsername().equals(appt.getAdmin().getUsername())
					&& obj.getDate().equals(appt.getDate()) && obj.getStartTime().equals(appt.getStartTime())) {
				obj.setStatus(AppointmentStatus.CONFIRMED);
				break;
			}
		}
		JsonHandler.saveList(allAppointments, "Appointments.json");
	}
}