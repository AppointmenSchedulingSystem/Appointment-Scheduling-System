package Fall2026;

import Fall2026.application.services.AppointmentService;
import Fall2026.application.services.AuthService;
import Fall2026.application.services.Session;
import Fall2026.domain.appointment.Schedule;
import Fall2026.infrastructure.notification.NotificationService;
import Fall2026.infrastructure.persistence.AdminFileManager;
import Fall2026.infrastructure.persistence.AppointmentFileManager;
import Fall2026.infrastructure.persistence.ScheduleFileManager;
import Fall2026.infrastructure.persistence.UserFileManager;
import Fall2026.shell.GuestShell;
import java.util.Scanner;
import Fall2026.domain.account.User;
import Fall2026.application.services.Session;

public class App {
//hello
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Session session = new Session();
        AdminFileManager adminFileManager = new AdminFileManager();
        UserFileManager userFileManager = new UserFileManager();
        AuthService authService = new AuthService(session, adminFileManager, userFileManager);

        Schedule schedule = new Schedule();
        ScheduleFileManager scheduleFileManager = new ScheduleFileManager(schedule);

        NotificationService emailService = new NotificationService();
        AppointmentService appointmentService = new AppointmentService(schedule, session, emailService);

        AppointmentFileManager appointmentFileManager = new AppointmentFileManager(schedule);
        appointmentService.setAppointmentFileManager(appointmentFileManager);
        appointmentService.loadAppointments();
        GuestShell guestShell = new GuestShell(scanner, session, authService, appointmentService, adminFileManager, scheduleFileManager);
        printWelcome();
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