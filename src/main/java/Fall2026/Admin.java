package Fall2026;
/*
US1.1 – Administrator login
As an administrator, I want to log into the system using my credentials so that I can manage
schedules and reservations.
Acceptance: - Valid credentials → login success - Invalid credentials → error message
*/
/*
US1.2 – Administrator logout
As an administrator, I want to log out so that my session is closed securely.
Acceptance: - After logout, admin actions require re-login
 */


import java.util.Date;

public class Admin {
   private String AdminName;
   private String AdminPassword;
   private Date lastLoginTime;
   private boolean IsLoggedIn;

    // Constructor
   public Admin(String adminName, String adminPassword) {
        this.AdminName = adminName;
        this.AdminPassword = adminPassword;
        this.lastLoginTime = new Date();
        this.IsLoggedIn = false;
   }
   public boolean Login(String AdminName, String AdminPassword) {
       if(this.AdminName.equals(AdminName) && this.AdminPassword.equals(AdminPassword)) {
              this.IsLoggedIn = true;
              this.lastLoginTime = new Date();
              return true; // login success
         } else {
              return false; // login failed
       }
   }
   public void Logout() {
         this.IsLoggedIn = false;
   }
   public boolean IsLoggedIn() {
            return this.IsLoggedIn;
   }
    // Getters
    public String getAdminName() {
        return AdminName;
    }

    public String getAdminPassword() {
        return AdminPassword;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }


}
