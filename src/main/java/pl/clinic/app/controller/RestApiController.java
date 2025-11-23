package pl.clinic.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.clinic.app.model.Appointment;
import pl.clinic.app.service.AppointmentService;

import java.util.List;

@RestController
@Deprecated
public class RestApiController {

    private final AppointmentService appointmentService;
    // konstruktor
    public RestApiController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/appointments_api")
    public List<Appointment> appointments() {
        return appointmentService.getAllAppointments();
    }
}
