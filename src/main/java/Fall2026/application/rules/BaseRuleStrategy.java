package Fall2026.application.rules;

import Fall2026.domain.appointment.Appointment;
import Fall2026.domain.exceptions.ValidationException;

public abstract class BaseRuleStrategy implements BookingRuleStrategy {
    @Override
    public void validate(Appointment apt) throws ValidationException {
        long durationMinutes = apt.getTimeSlot().getDuration().toMinutes();

        if (durationMinutes > getMaxDurationMinutes()) {//time
            throw new ValidationException(
                    "Slot duration (" + durationMinutes + " min) exceeds the maximum allowed "
                            + "for this appointment type (" + getMaxDurationMinutes() + " min)."
            );
        }

        if (apt.getMaxCapacity() > getMaxCapacity()) {//number of personals
            throw new ValidationException(
                    "Slot capacity (" + apt.getMaxCapacity() + ") exceeds the maximum allowed "
                            + "for this appointment type (" + getMaxCapacity() + ")."
            );
        }
    }
}
