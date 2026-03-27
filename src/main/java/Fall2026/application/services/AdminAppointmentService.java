package Fall2026.application.services;

import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.appointment.TimeSlot;
import Fall2026.domain.exceptions.AuthorizationException;
import Fall2026.domain.exceptions.ValidationException;

/**
 * Provides admin-only appointment management operations.
 * US4.2: Admin Appointment Management + Authorization
 */
public class AdminAppointmentService {
    private final AppointmentService appointmentService;
    private final AuthService authService;

    public AdminAppointmentService(AppointmentService appointmentService, AuthService authService) {
        this.appointmentService = appointmentService;
        this.authService = authService;
    }

    /**
     * Cancels an appointment with admin authorization check.
     * US4.2: Admin can cancel any appointment
     *
     * @param appt the appointment to cancel
     * @throws AuthorizationException if user is not an admin
     * @throws ValidationException if appointment cannot be cancelled
     */
    public void adminCancel(Appointment appt) {
        authService.requireAdmin();
        appointmentService.cancelAppointment(appt);
    }

    /**
     * Modifies an appointment with admin authorization check.
     * US4.2: Admin can modify any appointment
     *
     * @param oldAppt the appointment to modify
     * @param newSlot the new time slot
     * @return the modified appointment
     * @throws AuthorizationException if user is not an admin
     * @throws ValidationException if appointment cannot be modified
     */
    public Appointment adminModify(Appointment oldAppt, TimeSlot newSlot) {
        authService.requireAdmin();
        return appointmentService.modifyAppointment(oldAppt, newSlot);
    }
}

