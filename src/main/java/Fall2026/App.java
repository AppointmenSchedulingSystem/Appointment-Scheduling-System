package Fall2026;

import Fall2026.application.services.AppointmentService;
import Fall2026.application.services.AuthService;
import Fall2026.application.services.Session;
import Fall2026.domain.appointment.Schedule;
import Fall2026.infrastructure.notification.NotificationService;
import Fall2026.infrastructure.persistence.AdminFileManager;
import Fall2026.infrastructure.persistence.ScheduleFileManager;
import Fall2026.infrastructure.persistence.UserFileManager;
import Fall2026.shell.GuestShell;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Session session = new Session();
        AdminFileManager adminFileManager = new AdminFileManager();
        UserFileManager userFileManager = new UserFileManager();
        AuthService authService = new AuthService(session, adminFileManager, userFileManager);
        Schedule schedule = new Schedule();

        // Load time slots from Slots.txt on startup
        ScheduleFileManager scheduleFileManager = new ScheduleFileManager(schedule);

        AppointmentService appointmentService = new AppointmentService(schedule);


        printWelcome();

        GuestShell guestShell = new GuestShell(scanner, session, authService, appointmentService, adminFileManager, scheduleFileManager);
        guestShell.run();

        scanner.close();
    }

    private static void printWelcome() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   Appointment Scheduling System          ║");
        System.out.println("║   Type 'help' to see available commands  ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
    }
}