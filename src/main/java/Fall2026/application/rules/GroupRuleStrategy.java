package Fall2026.application.rules;

public class GroupRuleStrategy extends BaseRuleStrategy {
    @Override public int getMaxDurationMinutes() { return 120; }
    @Override public int getMaxCapacity()        { return 20; }
    @Override public boolean isAutoApproved()    { return false; }
}
