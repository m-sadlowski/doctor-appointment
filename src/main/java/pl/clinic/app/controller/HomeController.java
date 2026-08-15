package pl.clinic.app.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.clinic.app.model.Appointment;
import pl.clinic.app.service.AppointmentService;
import pl.clinic.app.config.AdminAuth;

/**
 * Kontroler widoków publicznych (pacjent) oraz logowania administratora.
 *
 * <p>Odpowiada za:</p>
 * <ul>
 *   <li>stronę startową,</li>
 *   <li>listę dostępnych terminów wizyt (z filtrowaniem),</li>
 *   <li>rezerwację wizyty przez pacjenta,</li>
 *   <li>widok „Moje wizyty” oraz anulowanie po numerze telefonu,</li>
 *   <li>logowanie i wylogowanie administratora (na podstawie sesji HTTP).</li>
 * </ul>
 */
@Controller
public class HomeController {
    /** Serwis zawierający logikę biznesową związaną z wizytami, pacjentami i lekarzami. */
    private final AppointmentService appointmentService;

    /**
     * Hasło administratora wczytywane z konfiguracji aplikacji.
     */
    @Value("${app.admin-password:123}")
    private String adminPassword;

    /**
     * Tworzy kontroler i wstrzykuje wymagany serwis.
     */
    public HomeController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }
    /**
     * Wyświetla stronę startową aplikacji.
     */
    @GetMapping("/")
    public String home(Model model) {
        return "home";
    }
    /**
     * Wyświetla listę dostępnych (wolnych) terminów wizyt dla pacjenta.
     *
     * <p>Lista jest sortowana rosnąco po dacie/godzinie. Umożliwia filtrowanie
     * po lekarzu oraz (opcjonalnie) po specjalizacji.</p>
     *
     * @param model model widoku z danymi do wyświetlenia w szablonie
     * @param doctorId opcjonalny identyfikator lekarza do filtrowania
     * @param specialization opcjonalna specjalizacja do filtrowania
     * @return nazwa szablonu Thymeleaf z listą terminów
     */
    @GetMapping("/appointments")
    public String appointments(Model model,
                               @RequestParam(required = false) Long doctorId,
                               @RequestParam(required = false) String specialization) {
        model.addAttribute("title", "Dostępne wizyty");
        model.addAttribute("doctors", appointmentService.getAllDoctors());
        model.addAttribute("specializations", appointmentService.getAllSpecializations());
        model.addAttribute("selectedDoctorId", doctorId);
        model.addAttribute("selectedSpecialization", specialization);
        model.addAttribute("appointments", appointmentService.getAvailableAppointmentsFiltered(doctorId, specialization));
        return "appointments";
    }
    /**
     * Wyświetla formularz logowania administratora.
     *
     * <p>Jeśli administrator jest już zalogowany, następuje
     * przekierowanie bezpośrednio do panelu admina.</p>
     *
     * @param session sesja HTTP użytkownika
     * @return widok logowania lub przekierowanie do panelu admina
     */
    @GetMapping("/login")
    public String login(HttpSession session){
        Boolean logged =  (Boolean) session.getAttribute(AdminAuth.ADMIN_AUTH_SESSION_KEY);
        if (Boolean.TRUE.equals(logged)){
            return "redirect:/admin";
        }
        return "login";
    }
    /**
     * Obsługuje logowanie administratora na podstawie hasła.
     *
     * <p>Po poprawnym zalogowaniu ustawia w sesji flagę
     * {@link pl.clinic.app.config.AdminAuth#ADMIN_AUTH_SESSION_KEY}, dzięki czemu
     * dostęp do {@code /admin/**} zostaje odblokowany przez interceptor.</p>
     *
     * @param password hasło podane w formularzu
     * @param session sesja HTTP, w której zapisywana jest informacja o zalogowaniu
     * @param model model widoku (do przekazania komunikatu o błędzie)
     * @return przekierowanie do panelu admina lub ponowne wyświetlenie formularza z błędem
     */
    @PostMapping("/login")
    public String doLogin(@RequestParam String password, HttpSession session, Model model){
        if(adminPassword.equals(password) && adminPassword != null){
            session.setAttribute(AdminAuth.ADMIN_AUTH_SESSION_KEY, true);
            return "redirect:/admin";
        }
        model.addAttribute("error", "Błędne hasło");
        return "login";
    }
    /**
     * Wylogowuje administratora poprzez unieważnienie sesji HTTP.
     *
     * @param session sesja HTTP administratora
     * @return przekierowanie na stronę startową
     */
    @PostMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    /**
     * Wyświetla formularz rezerwacji dla wybranego terminu wizyty.
     *
     * @param id identyfikator terminu wizyty
     * @param model model widoku (z danymi o wybranej wizycie)
     * @return nazwa szablonu formularza rezerwacji
     * @throws IllegalArgumentException jeśli termin o podanym ID nie istnieje
     */
    @GetMapping("/appointments/{id}/book")
    public String showBook(@PathVariable Long id, Model model){
        Appointment appt = appointmentService.findById(id);
        model.addAttribute("appointment", appt);
        return "book";
    }
    /**
     * Rezerwuje wskazany termin wizyty dla pacjenta.
     *
     * <p>Metoda zapisuje dane pacjenta i przypina go do terminu wizyty,
     * oznaczając termin jako zajęty.</p>
     *
     * @param id identyfikator terminu wizyty
     * @param firstName imię pacjenta
     * @param lastName nazwisko pacjenta
     * @param phoneNumber numer telefonu pacjenta
     * @return przekierowanie do listy dostępnych terminów
     * @throws IllegalStateException jeśli termin jest już zajęty
     */
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
    /**
     * Wyświetla stronę „Moje wizyty” umożliwiającą wyszukiwanie rezerwacji po numerze telefonu.
     *
     * @return nazwa szablonu Thymeleaf strony „Moje wizyty”
     */
    @GetMapping("/my-appointments")
    public String myAppointments() {
        return "my-appointments";
    }
    /**
     * Wyszukuje zarezerwowane wizyty na podstawie numeru telefonu i wyświetla je w widoku „Moje wizyty”.
     *
     * @param phoneNumber numer telefonu użyty przy rezerwacji
     * @param model model widoku (z listą wizyt i wpisanym numerem telefonu)
     * @return nazwa szablonu Thymeleaf strony „Moje wizyty”
     */
    @PostMapping("/my-appointments")
    public String showMyAppointments(@RequestParam String phoneNumber, Model model) {
        model.addAttribute("phoneNumber", phoneNumber);
        model.addAttribute("appointments", appointmentService.getBookedAppointmentsByPhone(phoneNumber));
        return "my-appointments";
    }
    /**
     * Anuluje rezerwację wizyty po weryfikacji numeru telefonu.
     *
     * <p>Jeśli numer telefonu nie pasuje do rezerwacji, anulowanie jest blokowane.
     * Informacja o powodzeniu/niepowodzeniu przekazywana jest jako komunikat flash.</p>
     *
     * @param id identyfikator wizyty do anulowania
     * @param phoneNumber numer telefonu użyty przy rezerwacji
     * @param redirectAttributes atrybuty przekierowania (komunikaty success/error)
     * @return przekierowanie do strony „Moje wizyty”
     */
    @PostMapping("/my-appointments/{id}/cancel")
    public String cancelMyAppointment(@PathVariable Long id,
                                      @RequestParam String phoneNumber,
                                      org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelByPatientPhone(id, phoneNumber);
            redirectAttributes.addFlashAttribute("success", "Wizyta została anulowana.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        redirectAttributes.addFlashAttribute("phoneNumber", phoneNumber);
        return "redirect:/my-appointments";
    }
}
