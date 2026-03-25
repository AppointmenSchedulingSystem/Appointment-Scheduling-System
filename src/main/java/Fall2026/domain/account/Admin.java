package Fall2026.domain.account;

//change this so admin extends User and remove the password field since it is already in User class
import java.time.Instant;
import java.util.Date;
//need update Admin must extend User
public class Admin extends Role {
    private Instant lastLoginTime;
    public Admin(int id, String username, String password, String email) {
        super(id, username, password, email);
    }

    public Instant getLastLoginTime() {
        return lastLoginTime;
    }

    /** Should be called by AuthService after a successful login (optional). */
    public void markLoginNow() {
        this.lastLoginTime = Instant.now();
    }




}
