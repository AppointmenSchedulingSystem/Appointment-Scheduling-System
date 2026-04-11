package Fall2026.application.rules;

public class AssessmentRuleStrategy extends BaseRuleStrategy {
    @Override public int getMaxDurationMinutes() { return 90; }
    @Override public int getMaxCapacity()        { return 2;  }
    @Override public boolean isAutoApproved()    { return false; }
}