package Fall2026.domain.account;


import Fall2026.domain.exceptions.ValidationException;
import Fall2026.util.Validators;

//role won't have types it's added in child classes
public abstract class Role {
    protected final int ID; // can't change ID after creation, so it's final
    protected String username;
    protected String password;//TODO: have hash password instead of plain text(no need in DB)
    protected String email;
    protected boolean isLoggedIn;

    //we will take ID premaid
    public Role(int ID, String username, String password, String email) {
        this.ID = ID;
        this.username = username;
        this.password = password;
        this.email = email;
        this.isLoggedIn = false;
    }
    public int getID() {
        return ID;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        if (!Validators.isValidEmail(email)) {
            throw new ValidationException("Email must be in valid format (e.g., user@example.com).");
        }
        this.email = email;
    }

    public void setPassword(String password) {
        if (!Validators.isValidPassword(password)) {
            throw new ValidationException("Password must be at least 6 characters.");
        }
        this.password = password;
    }

    public void setUsername(String username) {
        if (!Validators.isValidUsername(username)) {
            throw new ValidationException("Username cannot be null or empty.");
        }
        this.username = username;
    }


}
