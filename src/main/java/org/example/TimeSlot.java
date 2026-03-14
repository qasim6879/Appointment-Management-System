package org.example;
import java.time.*;
public class TimeSlot {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime duration;
    
    public TimeSlot(LocalDate date, LocalTime startTime, LocalTime duration) {
		this.date = date;
		this.startTime = startTime;
		this.duration = duration;
	}
    
    public LocalDate getDate() {
		return date;
	}
    public LocalTime getStartTime() {
    			return startTime;
    }
    public LocalTime getEndTime() { return duration; }
}