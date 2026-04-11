package Fall2026.application.rules;

import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.exceptions.ValidationException;

public interface BookingRuleStrategy {
    int getMaxDurationMinutes();
    int getMaxCapacity();
    boolean isAutoApproved();
    void validate(Appointment apt) throws ValidationException;

}
