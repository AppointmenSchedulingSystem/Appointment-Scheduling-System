package Fall2026.infrastructure.notification;

import Fall2026.domain.account.Admin;
import Fall2026.domain.account.Role;
import Fall2026.domain.account.User;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("NotificationService")
class NotificationServiceTest {

    private User user;
    private Admin admin;
    private TestNotificationService notificationService;

    @BeforeEach
    void setUp() {
        user = new User(1, "patientUser", "secret1", "user@example.com");
        admin = new Admin(2, "adminUser", "secret2", "admin@example.com");
        notificationService = new TestNotificationService();
    }

    @Nested
    @DisplayName("notify with User")
    class NotifyUserTests {

        @Test
        @DisplayName("dispatches email to correct user address with message content")
        void dispatchesToValidUser() {
            notificationService.notify(user, "Your appointment is confirmed.");

            assertEquals(1, notificationService.capturedMessages.size());
            Message sent = notificationService.capturedMessages.get(0);

            try {
                InternetAddress recipient = (InternetAddress) sent.getRecipients(Message.RecipientType.TO)[0];
                String html = sent.getContent().toString();

                assertEquals("user@example.com", recipient.getAddress());
                assertTrue(html.contains("Your appointment is confirmed."));
                assertTrue(html.contains("patientUser"));
                assertTrue(sent.getSubject().contains("Appointment"));
            } catch (Exception e) {
                throw new RuntimeException("Failed to inspect captured message", e);
            }
        }

        @Test
        @DisplayName("message content includes HTML with styling")
        void messageContentIsHtml() {
            notificationService.notify(user, "Test message");

            Message sent = notificationService.capturedMessages.get(0);
            try {
                String html = sent.getContent().toString();
                assertTrue(html.contains("<!DOCTYPE html>"));
                assertTrue(html.contains("Appointment System"));
                assertTrue(html.contains("message-box"));
            } catch (Exception e) {
                throw new RuntimeException("Failed to inspect HTML content", e);
            }
        }
    }

    @Nested
    @DisplayName("notify with Admin")
    class NotifyAdminTests {

        @Test
        @DisplayName("dispatches email to correct admin address with message content")
        void dispatchesToValidAdmin() {
            notificationService.notify(admin, "New booking created.");

            assertEquals(1, notificationService.capturedMessages.size());
            Message sent = notificationService.capturedMessages.get(0);

            try {
                InternetAddress recipient = (InternetAddress) sent.getRecipients(Message.RecipientType.TO)[0];
                String html = sent.getContent().toString();

                assertEquals("admin@example.com", recipient.getAddress());
                assertTrue(html.contains("New booking created."));
                assertTrue(html.contains("adminUser"));
            } catch (Exception e) {
                throw new RuntimeException("Failed to inspect captured message", e);
            }
        }
    }

    @Nested
    @DisplayName("notify with edge cases")
    class NotifyEdgeCasesTests {

        @Test
        @DisplayName("throws NullPointerException when role is null")
        void throwsWhenRoleIsNull() {
            assertThrows(NullPointerException.class, () -> notificationService.notify(null, "message"));
        }

        @Test
        @DisplayName("handles null message by sending rendered null string")
        void handlesNullMessage() {
            notificationService.notify(user, null);

            assertEquals(1, notificationService.capturedMessages.size());
            Message sent = notificationService.capturedMessages.get(0);

            try {
                String html = sent.getContent().toString();
                assertTrue(html.contains("null"));
            } catch (Exception e) {
                throw new RuntimeException("Failed to inspect message with null content", e);
            }
        }

        @Test
        @DisplayName("handles empty message by sending email with empty message box")
        void handlesEmptyMessage() {
            notificationService.notify(user, "");

            assertEquals(1, notificationService.capturedMessages.size());
            Message sent = notificationService.capturedMessages.get(0);

            try {
                String html = sent.getContent().toString();
                assertTrue(html.contains("<div class='message-box'></div>"));
            } catch (Exception e) {
                throw new RuntimeException("Failed to inspect message with empty content", e);
            }
        }
    }

    @Nested
    @DisplayName("Observer contract")
    class ObserverContractTests {

        @Test
        @DisplayName("implements Observer interface")
        void implementsObserver() {
            assertTrue(notificationService instanceof Observer);
        }

        @Test
        @DisplayName("notify method builds correct email structure")
        void notifyBuildsCorrectStructure() {
            notificationService.notify(user, "Test notification body");

            assertEquals(1, notificationService.capturedMessages.size());
            Message sent = notificationService.capturedMessages.get(0);

            try {
                assertNotNull(sent.getFrom());
                assertNotNull(sent.getRecipients(Message.RecipientType.TO));
                assertNotNull(sent.getSubject());
                assertEquals("text/html; charset=utf-8", sent.getContentType());
            } catch (Exception e) {
                throw new RuntimeException("Failed to verify email structure", e);
            }
        }

        @Test
        @DisplayName("handles send failures gracefully without throwing")
        void handlesExceptionGracefully() {
            TestFailingNotificationService failingService = new TestFailingNotificationService();
            failingService.notify(user, "This should not throw");
            assertTrue(failingService.capturedMessages.isEmpty());
        }
    }

    static class TestNotificationService extends NotificationService {
        List<Message> capturedMessages = new ArrayList<>();

        @Override
        protected void sendEmail(Message email) {
            capturedMessages.add(email);
        }
    }

    static class TestFailingNotificationService extends NotificationService {
        List<Message> capturedMessages = new ArrayList<>();

        @Override
        protected void sendEmail(Message email) throws RuntimeException {
            throw new RuntimeException("Simulated transport failure");
        }
    }
}
