package Fall2026;


import java.util.Date;
//need update Admin must extend User
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
