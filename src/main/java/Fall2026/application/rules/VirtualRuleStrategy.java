package Fall2026.application.rules;

public class VirtualRuleStrategy extends BaseRuleStrategy {
    @Override public int getMaxDurationMinutes() { return 60; }
    @Override public int getMaxCapacity()        { return 1;  }
    @Override public boolean isAutoApproved()    { return true; }
}