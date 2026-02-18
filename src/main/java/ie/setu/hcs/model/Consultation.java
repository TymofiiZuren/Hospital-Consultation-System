// defining package ie.setu.hcs.model
package ie.setu.hcs.model;

// importing stuff
import java.time.LocalDateTime;

// creating Consultation class
public class Consultation {
    // defining attributes
    private Integer consultationId;
    private Integer appointmentId;
    private String diagnosis;
    private String notes;
    private LocalDateTime createdAt;
    
    // creating empty Consultation constructor
    public Consultation() {}

    // creating Consultation constructor with defining arguments
    public Consultation(Integer appointmentId, String diagnosis,
                        String notes, LocalDateTime createdAt) {
        // implementing attributes
        this.appointmentId = appointmentId;
        this.diagnosis = diagnosis;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // creating Consultation constructor with defining arguments
    public Consultation(Integer consultationId, Integer appointmentId, String diagnosis,
                        String notes, LocalDateTime createdAt) {
        // implementing attributes
        this.consultationId = consultationId;
        this.appointmentId = appointmentId;
        this.diagnosis = diagnosis;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // creating getter for consultationId
    public Integer getConsultationId() {
        return consultationId;
    }

    // creating setter for consultationId
    public void setConsultationId(Integer consultationId) {
        this.consultationId = consultationId;
    }

    // creating getter for appointmentId
    public Integer getAppointmentId() {
        return appointmentId;
    }

    // creating setter for appointmentId
    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    // creating getter for diagnosis
    public String getDiagnosis() {
        return diagnosis;
    }

    // creating setter for diagnosis
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    // creating getter for notes
    public String getNotes() {
        return notes;
    }

    // creating setter for notes
    public void setNotes(String notes) {
        this.notes = notes;
    }

    // creating getter for createdAt
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // creating setter for createdAt
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // implementing the toString method
    @Override
    public String toString() {
        return "Consultation Id: " + consultationId +
                "Appointment Id: " + appointmentId +
                "Diagnosis: " + diagnosis +
                "Created At: " + createdAt;
    }
}
