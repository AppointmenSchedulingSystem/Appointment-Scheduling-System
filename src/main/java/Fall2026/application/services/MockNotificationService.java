package Fall2026.application.services;

import java.util.ArrayList;
import java.util.List;

public class MockNotificationService implements NotificationService {

    private final List<String> sentMessages = new ArrayList<>();

    @Override
    public void sendNotification(String userEmail, String message) {
        sentMessages.add("To: " + userEmail + " | Message: " + message);
    }

    public List<String> getSentMessages() {
        return sentMessages;
    }
}