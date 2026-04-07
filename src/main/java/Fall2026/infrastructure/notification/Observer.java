package Fall2026.infrastructure.notification;
import Fall2026.domain.account.Role;
import Fall2026.domain.account.User;


public interface Observer {
        void notify(Role role, String message);
}
