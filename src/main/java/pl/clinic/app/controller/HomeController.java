package pl.clinic.app.controller;

import jakarta.websocket.server.PathParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.clinic.app.model.Appointment;
import pl.clinic.app.service.AppointmentService;

import java.util.List;

@Controller
public class HomeController {

    private final AppointmentService appointmentService;
    // konstruktor
    public HomeController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }
    @GetMapping("/")
    public String home(Model model) {
        return "home";
    }
    @GetMapping("/appointments")
    public String appointments(Model model) {
        model.addAttribute("title", "Dostępne wizyty");
        model.addAttribute("appointments", appointmentService.getAllAppointments());
        return "appointments";
    }
    @GetMapping("/login")
    public String login(){
        return "login";
    }
    @GetMapping("/appointments/{id}/book")
    public String showBook(@PathVariable Long id, Model model){
        Appointment appt = appointmentService.findById(id);
        model.addAttribute("appointment", appt);
        return "book";
    }
    @PostMapping("/appointments/{id}/book")
    public String bookAppointment(
            @PathVariable Long id,
            @RequestParam String firstName, // RequestParam a nie PathParam- do WebSocketow; Request do formularza/query
            @RequestParam String lastName,
            @RequestParam String phoneNumber
            )
    {
        appointmentService.bookAppointment(id, firstName, lastName, phoneNumber);
        return "redirect:/appointments";
    }

}
