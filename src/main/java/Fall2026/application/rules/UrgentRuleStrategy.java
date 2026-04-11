package Fall2026.application.rules;

public class UrgentRuleStrategy extends BaseRuleStrategy {
    @Override public int getMaxDurationMinutes() { return 30; }// Urgent appointments can be up to 30 minutes long
    @Override public int getMaxCapacity()        { return 1;  }
    @Override public boolean isAutoApproved()    { return true; }
}
