package Fall2026.application.rules;

import Fall2026.domain.appointment.AppointmentType;


public class AppointmentTypeRuleFactory {

    public static BookingRuleStrategy getStrategy(AppointmentType type) {
        switch (type) {
            case URGENT:     return new UrgentRuleStrategy();
            case FOLLOW_UP:  return new FollowUpRuleStrategy();
            case ASSESSMENT: return new AssessmentRuleStrategy();
            case VIRTUAL:    return new VirtualRuleStrategy();
            case IN_PERSON:  return new InPersonRuleStrategy();
            case INDIVIDUAL: return new IndividualRuleStrategy();
            case GROUP:      return new GroupRuleStrategy();
            default:
                throw new IllegalArgumentException("Unknown appointment type: " + type);
        }
    }
}
