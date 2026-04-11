package Fall2026.application.rules;

import Fall2026.domain.appointment.AppointmentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AppointmentTypeRuleFactory")
class AppointmentTypeRuleFactoryTest {

    @Nested
    @DisplayName("getStrategy")
    class GetStrategyTests {

        @Test
        @DisplayName("returns UrgentRuleStrategy for URGENT type")
        void returnsUrgentStrategyForUrgent() {
            BookingRuleStrategy strategy = AppointmentTypeRuleFactory.getStrategy(AppointmentType.URGENT);

            assertNotNull(strategy);
            assertInstanceOf(UrgentRuleStrategy.class, strategy);
        }

        @Test
        @DisplayName("returns FollowUpRuleStrategy for FOLLOW_UP type")
        void returnsFollowUpStrategyForFollowUp() {
            BookingRuleStrategy strategy = AppointmentTypeRuleFactory.getStrategy(AppointmentType.FOLLOW_UP);

            assertNotNull(strategy);
            assertInstanceOf(FollowUpRuleStrategy.class, strategy);
        }

        @Test
        @DisplayName("returns AssessmentRuleStrategy for ASSESSMENT type")
        void returnsAssessmentStrategyForAssessment() {
            BookingRuleStrategy strategy = AppointmentTypeRuleFactory.getStrategy(AppointmentType.ASSESSMENT);

            assertNotNull(strategy);
            assertInstanceOf(AssessmentRuleStrategy.class, strategy);
        }

        @Test
        @DisplayName("returns VirtualRuleStrategy for VIRTUAL type")
        void returnsVirtualStrategyForVirtual() {
            BookingRuleStrategy strategy = AppointmentTypeRuleFactory.getStrategy(AppointmentType.VIRTUAL);

            assertNotNull(strategy);
            assertInstanceOf(VirtualRuleStrategy.class, strategy);
        }

        @Test
        @DisplayName("returns InPersonRuleStrategy for IN_PERSON type")
        void returnsInPersonStrategyForInPerson() {
            BookingRuleStrategy strategy = AppointmentTypeRuleFactory.getStrategy(AppointmentType.IN_PERSON);

            assertNotNull(strategy);
            assertInstanceOf(InPersonRuleStrategy.class, strategy);
        }

        @Test
        @DisplayName("returns IndividualRuleStrategy for INDIVIDUAL type")
        void returnsIndividualStrategyForIndividual() {
            BookingRuleStrategy strategy = AppointmentTypeRuleFactory.getStrategy(AppointmentType.INDIVIDUAL);

            assertNotNull(strategy);
            assertInstanceOf(IndividualRuleStrategy.class, strategy);
        }

        @Test
        @DisplayName("returns GroupRuleStrategy for GROUP type")
        void returnsGroupStrategyForGroup() {
            BookingRuleStrategy strategy = AppointmentTypeRuleFactory.getStrategy(AppointmentType.GROUP);

            assertNotNull(strategy);
            assertInstanceOf(GroupRuleStrategy.class, strategy);
        }

        @Test
        @DisplayName("throws IllegalArgumentException for null type")
        void throwsIllegalArgumentExceptionForNullType() {
            assertThrows(IllegalArgumentException.class, () -> {
                AppointmentTypeRuleFactory.getStrategy(null);
            });
        }

        @Test
        @DisplayName("never returns null strategy")
        void neverReturnsNullStrategy() {
            for (AppointmentType type : AppointmentType.values()) {
                BookingRuleStrategy strategy = AppointmentTypeRuleFactory.getStrategy(type);
                assertNotNull(strategy, "Strategy for " + type + " should not be null");
            }
        }
    }
}
