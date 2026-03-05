package org.example;
import java.time.*;
public class TimeSlot {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    
    public TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
	}
    
    public LocalDate getDate() {
		return date;
	}
    public LocalTime getStartTime() {
    			return startTime;
    }
    public LocalTime getEndTime() {
				return endTime;
	}
    
}