package org.example;

import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    private List<Appointment> appointments = new ArrayList<>();

    public void bookAppointment(Appointment appointment) {
        appointments.add(appointment);
        System.out.println("Appointment booked: " + appointment.getId());
    }

    public void cancelAppointment(String id) {
        appointments.removeIf(a -> a.getId().equals(id));
        System.out.println("Appointment cancelled: " + id);
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }
}