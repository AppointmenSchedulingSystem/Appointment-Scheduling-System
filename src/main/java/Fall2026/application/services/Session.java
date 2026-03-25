package Fall2026.application.services;
import Fall2026.domain.account.Admin;
import Fall2026.domain.account.Role;
import Fall2026.domain.account.User;

public class Session {
    private Role currentAccount;

    public Role getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(Role currentAccount) {
        this.currentAccount = currentAccount;
    }

    public void clear() {
        this.currentAccount = null;
    }
    public boolean isLoggedIn() {
        return currentAccount != null;
    }

    public boolean isAdmin() {
        return currentAccount instanceof Admin;
    }

    public boolean isUser() {
        return currentAccount instanceof User;
    }

}
