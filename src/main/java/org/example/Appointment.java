package org.example;

import java.time.*;
import java.util.*;

public class Appointment {
	private User user;
	private Administrator admin;
	private LocalDate date;
	private LocalTime startTime;
	private int duration;
	private AppointmentType type;
	private AppointmentStatus status;
	private int maxParticipants;

	public Appointment(User user, Administrator admin, LocalDate date, LocalTime startTime, int duration, AppointmentType type, AppointmentStatus status) {
		this.user = user;
		this.admin = admin;
		this.date = date;
		this.startTime = startTime;
		this.duration = duration;
		this.type = type;
		this.status = status;

		switch (this.type) {
			case URGENT, INDIVIDUAL: maxParticipants = 1; break;
			case VIRTUAL: maxParticipants = 5; break;
			default: maxParticipants = 3;
		}
	}

	public User getUser() { return user; }
	public Administrator getAdmin() {
		return admin;
	}
	public LocalDate getDate() {
		return date;
	}
	public LocalTime getStartTime() {
		return startTime;
	}
	public int getDuration() {
		return duration;
	}
	public AppointmentType getType() {
		return type;
	}
	public AppointmentStatus getStatus() {
		return status;
	}

	public int getMaxParticipants() {
		return maxParticipants;
	}

	
	
	public void setDate(LocalDate date) { this.date = date; }
	public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
	public void setDuration(int duration) { this.duration = duration; }
	public void setStatus(AppointmentStatus status) { this.status = status; }

	public void setType(AppointmentType type) {
		this.type = type;
		switch (this.type) {
			case URGENT, INDIVIDUAL: maxParticipants = 1; break;
			case VIRTUAL: maxParticipants = 5; break;
			default: maxParticipants = 3;
		}
	}

	public static boolean[] availableTimeSlots(LocalDate date, String adminUsername, int duration){
		boolean[] available = new boolean[12];
		Arrays.fill(available, true);

		List <Appointment> appointments = JsonHandler.loadList("appointments.json", Appointment.class);

		for (int i = 0; i < appointments.size(); i++){
			if (appointments.get(i).getAdmin().getUsername().equals(adminUsername) && appointments.get(i).getDate().equals(date) && appointments.get(i).getStatus() != AppointmentStatus.CANCELLED){
				int hour = appointments.get(i).getStartTime().getHour();
				int minute = appointments.get(i).getStartTime().getMinute();
				int index = (hour - 9) * 2 + (minute / 30);

				available[index] = false;
				if (appointments.get(i).getDuration() == 60 && index < 11)
					available[index + 1] = false;

				if (duration == 60 && index > 0)
					available[index - 1] = false;
			}
		}
		return available;
	}

	public Appointment() {}
}