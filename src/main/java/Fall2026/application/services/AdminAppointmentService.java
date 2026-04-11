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

    /**
     * Modifies an appointment with new time slot and description.
     * Admin can change all appointment properties.
     * US4.2: Admin Appointment Management (Enhanced)
     *
     * @param oldAppt the appointment to modify
     * @param newSlot the new time slot (date and time)
     * @param newDescription the new description/reason
     * @return the modified appointment
     * @throws AuthorizationException if user is not an admin
     * @throws ValidationException if appointment cannot be modified
     */
    public Appointment adminModifyFull(Appointment oldAppt, TimeSlot newSlot, String newDescription) {
        authService.requireAdmin();
        return appointmentService.modifyAppointmentFull(oldAppt, newSlot, newDescription);
    }

    /**
     * Removes a time slot (and any associated appointment) from the schedule.
     * Admin can remove/delete time slots.
     * US4.2: Admin Schedule Management
     *
     * @param slot the time slot to remove
     * @throws AuthorizationException if user is not an admin
     */
    public void adminRemoveSlot(TimeSlot slot) {
        authService.requireAdmin();
        appointmentService.removeSlot(slot);
    }



    /**
     * Approves a PENDING appointment.
     * Sets status to CONFIRMED and returns the appointment.
     *
     * @param appt the appointment to approve
     * @throws AuthorizationException if user is not an admin
     * @throws ValidationException if appointment is not in PENDING state
     */
    public Appointment adminApprove(Appointment appt) {
        authService.requireAdmin();

        if (appt.getStatus() != Appointment.AppointmentStatus.PENDING) {
            throw new ValidationException("Appointment is not pending approval.");
        }

        appt.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        return appt;
    }

    /**
     * Rejects a PENDING appointment — cancels it entirely.
     *
     * @param appt the appointment to reject
     * @throws AuthorizationException if user is not an admin
     */
    public void adminReject(Appointment appt) {
        authService.requireAdmin();

        if (appt.getStatus() != Appointment.AppointmentStatus.PENDING) {
            throw new ValidationException("Appointment is not pending approval.");
        }

        appointmentService.cancelAppointment(appt);
    }



}

