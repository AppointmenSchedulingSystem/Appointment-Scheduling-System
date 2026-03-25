# Appointment-Scheduling-System
Code structure for an appointment scheduling system.
```aiignore
Fall2026
                ├── App.java
                ├── application
                │ ├── rules
                │ └── services
                │     ├── AppointmentService.java
                │     └── AuthService.java
                ├── domain
                │ ├── account
                │ │ ├── Admin.java
                │ │ ├── Role.java
                │ │ └── User.java
                │ ├── appointment
                │ │ ├── Appointment.java
                │ │ ├── Schedule.java
                │ │ └── TimeSlot.java
                │ └── exceptions
                │    └── ValidationException.java
                ├── infrastructure
                │ ├── notification
                │ └── persistence
                │     ├── AdminFileManager.java
                │     └── CredentialStorage.java
                └── util
                    └── Validators.java
```
