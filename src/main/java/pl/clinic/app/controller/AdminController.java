package pl.clinic.app.controller;


import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.clinic.app.service.AppointmentService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * Kontroler panelu administratora.
 *
 * <p>Udostępnia widok zarządzania terminami wizyt oraz lekarzami.</p>
 */
@Controller
@RequestMapping ("/admin")
public class AdminController {

    /** Serwis zawierający logikę biznesową panelu administratora (wizyty i lekarze). */
    private final AppointmentService appointmentService;
    /**
     * Tworzy kontroler panelu administratora.
     *
     * @param appointmentService serwis obsługujący operacje administracyjne
     */
    public AdminController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Wyświetla panel administratora wraz z listą wizyt i listą lekarzy.
     *
     * <p>Lista wizyt jest sortowana rosnąco po dacie. Parametr {@code status} pozwala filtrować widok:</p>
     * <ul>
     *   <li>{@code all} – wszystkie wizyty,</li>
     *   <li>{@code booked} – tylko zajęte,</li>
     *   <li>{@code free} – tylko wolne.</li>
     * </ul>
     *
     * @param model model widoku z danymi do wyświetlenia
     * @param status filtr statusu wizyt (domyślnie {@code all})
     * @return nazwa szablonu Thymeleaf panelu administratora
     */
    @GetMapping
    public String adminPanel(Model model,
                             @RequestParam(required = false, defaultValue = "all") String status) {
        model.addAttribute("appointments", appointmentService.getAppointmentsForAdmin(status));
        model.addAttribute("doctors", appointmentService.getAllDoctors());
        model.addAttribute("status", status);
        return "admin";
    }

    /**
     * Dodaje nowy wolny termin wizyty dla wskazanego lekarza.
     *
     * <p>Data i godzina są przekazywane z formularza HTML (typ {@code datetime-local}).
     * W przypadku próby dodania terminu, który już istnieje, metoda przekazuje komunikat błędu
     * poprzez atrybuty flash.</p>
     *
     * @param doctorId identyfikator lekarza, dla którego dodawany jest termin
     * @param dateTime data i godzina terminu wizyty
     * @param redirectAttributes atrybuty przekierowania (komunikat sukcesu/błędu)
     * @return przekierowanie do panelu administratora
     * @throws IllegalStateException jeśli termin dla lekarza o tej dacie już istnieje
     */
    @PostMapping("/appointments")
    public String addAppointmentSlot(@RequestParam Long doctorId,
                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")LocalDateTime dateTime, RedirectAttributes redirectAttributes){
        try{
            appointmentService.addAppointmentSlot(doctorId, dateTime);
            redirectAttributes.addFlashAttribute("sukces", "Pomyślnie dodano termin");
        } catch (IllegalStateException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    /**
     * Anuluje (zwalnia) wskazany termin wizyty.
     *
     * @param id identyfikator terminu wizyty
     * @return przekierowanie do panelu administratora
     */
    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id){
        appointmentService.cancelAppointment(id);
        return "redirect:/admin";
    }
    /**
     * Dodaje nowego lekarza do systemu.
     *
     * @param firstName imię lekarza
     * @param lastName nazwisko lekarza
     * @param specialization specjalizacja lekarza
     * @param ra atrybuty przekierowania (komunikat sukcesu/błędu)
     * @return przekierowanie do sekcji lekarzy w panelu administratora
     */
    @PostMapping("/doctors")
    public String addDoctor(@RequestParam String firstName,
                            @RequestParam String lastName,
                            @RequestParam String specialization,
                            RedirectAttributes ra) {
        try {
            appointmentService.addDoctor(firstName, lastName, specialization);
            ra.addFlashAttribute("sukces", "Dodano lekarza.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin#doctors";
    }

    /**
     * Usuwa lekarza z systemu.
     *
     * @param id identyfikator lekarza do usunięcia
     * @param redirectAttributes atrybuty przekierowania (komunikat sukcesu/błędu)
     * @return przekierowanie do sekcji lekarzy w panelu administratora
     */
    @PostMapping("/doctors/{id}/delete")
    public String deleteDoctor(@PathVariable Long id, RedirectAttributes redirectAttributes){
        try{
            appointmentService.delteDoctor(id);
            redirectAttributes.addFlashAttribute("sukces", "Usunięto lekarza");
        } catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin#doctors";
    }
}
