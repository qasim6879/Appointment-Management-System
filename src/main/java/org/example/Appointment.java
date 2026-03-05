package org.example;
import java.time.*;
	public class Appointment {
	    private String id;
	    private LocalDate date;
	    private LocalTime startTime;
	    private int duration;
	    private AppointmentType type;
	    private int participantCount;
	    private AppointmentStatus status;
	    
	    public Appointment(String id, LocalDate date, LocalTime startTime, int duration, AppointmentType type) {
	        this.id = id;
	        this.date = date;
	        this.startTime = startTime;
	        this.duration = duration;
	        this.type = type;
	        this.participantCount = 0;
	        this.status = AppointmentStatus.AVAILABLE;
	    }
	    public String getId() {
	        return id;
	        
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
	    
	    
	    
	}

