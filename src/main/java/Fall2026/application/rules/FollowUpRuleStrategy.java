package Fall2026.application.rules;

public class FollowUpRuleStrategy extends BaseRuleStrategy {
    @Override public int getMaxDurationMinutes() { return 45; }
    @Override public int getMaxCapacity()        { return 1;  }
    @Override public boolean isAutoApproved()    { return true; }
}
