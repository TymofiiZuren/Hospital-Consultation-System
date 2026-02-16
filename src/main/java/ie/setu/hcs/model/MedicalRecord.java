// defining package ie.setu.hcs.model
package ie.setu.hcs.model;

// importing stuff
import java.time.LocalDateTime;

// implementing MedicalRecord class
public class MedicalRecord {
    // defining attributes of the class
    private Integer recordId;
    private Integer patientId;
    private Integer consultationId;
    private String prescription;
    private LocalDateTime createdAt;

    // creating empty MedicalRecord constructor
    public MedicalRecord() {}
    
    // creating MedicalRecord constructor with defining arguments
    public MedicalRecord(Integer patientId, Integer consultationId,
                         String prescription, LocalDateTime createdAt) {
        // implementing attributes
        this.patientId = patientId;
        this.consultationId = consultationId;
        this.prescription = prescription;
        this.createdAt = createdAt;
    }
    
    // creating MedicalRecord constructor with defining arguments
    public MedicalRecord(Integer recordId, Integer patientId, Integer consultationId,
                         String prescription, LocalDateTime createdAt) {
        // implementing attributes
        this.recordId = recordId;
        this.patientId = patientId;
        this.consultationId = consultationId;
        this.prescription = prescription;
        this.createdAt = createdAt;
    }

    // creating getter for recordId
    public Integer getRecordId() {
        return recordId;
    }

    // creating setter for recordId
    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    // creating getter for patientId
    public Integer getPatientId() {
        return patientId;
    }

    // creating setter for patientId
    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    // creating getter for consultationId
    public Integer getConsultationId() {
        return consultationId;
    }

    // creating setter for consultationId
    public void setConsultationId(Integer consultationId) {
        this.consultationId = consultationId;
    }

    // creating getter for prescription
    public String getPrescription() {
        return prescription;
    }

    // creating setter for prescription
    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    // creating getter for createdAt
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // creating setter for createdAt
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // implementing toString method
    @Override
    public String toString() {
        return "\nRecord Id: " + recordId +
                "\nPatient Id: " + patientId +
                "\nConsultation Id: " + consultationId +
                "\nPrescription: " + prescription +
                "\nCreated at: " + createdAt;
    }
}
