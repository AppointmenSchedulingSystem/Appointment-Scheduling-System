package Fall2026.domain.appointment;

public enum AppointmentType {
    URGENT,//skip the queue
    FOLLOW_UP,//for patients who have already had an initial consultation and need a follow-up appointment
    ASSESSMENT,//for patients who need an initial assessment to determine the appropriate course of treatment
    VIRTUAL,//for appointments that can be conducted remotely via video conferencing
    IN_PERSON,//for appointments that require the patient to be physically present
    INDIVIDUAL,//for one-on-one appointments
    GROUP//for appointments that involve multiple personnel
}
