### Appointment-Scheduling-System
##-Ahmad Daghlas 12218563
##-Mohanad Hamad 122
##-Yazan Salem 122

Code structure for an appointment scheduling system.
```aiignore
.
├── Admins.txt
├── Appointments.txt
├── README.md
├── Slots.txt
├── Users.txt
├── pom.xml
└── src
    └── main
        ├── java
        │ └── Fall2026
        │     ├── App.java
        │     ├── application
        │     │ ├── rules
        │     │ │ ├── AppointmentTypeRuleFactory.java
        │     │ │ ├── AssessmentRuleStrategy.java
        │     │ │ ├── BaseRuleStrategy.java
        │     │ │ ├── BookingRuleStrategy.java
        │     │ │ ├── FollowUpRuleStrategy.java
        │     │ │ ├── GroupRuleStrategy.java
        │     │ │ ├── InPersonRuleStrategy.java
        │     │ │ ├── IndividualRuleStrategy.java
        │     │ │ ├── UrgentRuleStrategy.java
        │     │ │ └── VirtualRuleStrategy.java
        │     │ └── services
        │     │     ├── AdminAppointmentService.java
        │     │     ├── AppointmentService.java
        │     │     ├── AuthService.java
        │     │     └── Session.java
        │     ├── domain
        │     │ ├── account
        │     │ │ ├── Admin.java
        │     │ │ ├── Role.java
        │     │ │ └── User.java
        │     │ ├── appointment
        │     │ │ ├── Appointment.java
        │     │ │ ├── AppointmentType.java
        │     │ │ ├── Schedule.java
        │     │ │ └── TimeSlot.java
        │     │ └── exceptions
        │     │     ├── AuthorizationException.java
        │     │     └── ValidationException.java
        │     ├── infrastructure
        │     │ ├── notification
        │     │ │ ├── NotificationService.java
        │     │ │ └── Observer.java
        │     │ └── persistence
        │     │     ├── AdminFileManager.java
        │     │     ├── AppointmentFileManager.java
        │     │     ├── CredentialStorage.java
        │     │     ├── EmailService.java
        │     │     ├── ScheduleFileManager.java
        │     │     └── UserFileManager.java
        │     ├── shell
        │     │ ├── AdminShell.java
        │     │ ├── GuestShell.java
        │     │ └── UserShell.java
        │     └── util
        │         └── Validators.java
        └── resources
            └── config.properties
```
