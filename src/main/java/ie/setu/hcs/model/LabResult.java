// defining package ie.setu.hcs.model
package ie.setu.hcs.model;

// importing stuff
import java.time.LocalDateTime;

// implementing LabResult class
public class LabResult {
    // defining attributes of the class
    private Integer labResultId;
    private Integer consultationId;
    private Integer appointmentId;
    private Integer technicianId;
    private String testType;
    private String result;
    private LocalDateTime uploadedAt;

    // creating empty LabResult constructor
    public LabResult() {}
    
    // creating LabResult constructor with defining arguments
    public LabResult(Integer consultationId, Integer technicianId,
                     String testType, String result, LocalDateTime uploadedAt) {
        this(consultationId, null, technicianId, testType, result, uploadedAt);
    }

    public LabResult(Integer consultationId, Integer appointmentId, Integer technicianId,
                     String testType, String result, LocalDateTime uploadedAt) {
        // implementing attributes
        this.consultationId = consultationId;
        this.appointmentId = appointmentId;
        this.technicianId = technicianId;
        this.testType = testType;
        this.result = result;
        this.uploadedAt = uploadedAt;
    }

    public LabResult(Integer labResultId, Integer consultationId, Integer appointmentId, Integer technicianId,
                     String testType, String result, LocalDateTime uploadedAt) {
        // implementing attributes
        this.labResultId = labResultId;
        this.consultationId = consultationId;
        this.appointmentId = appointmentId;
        this.technicianId = technicianId;
        this.testType = testType;
        this.result = result;
        this.uploadedAt = uploadedAt;
    }

    // creating getter for labResultId
    public Integer getLabResultId() {
        return labResultId;
    }

    // creating setter for labResultId
    public void setLabResultId(Integer labResultId) {
        this.labResultId = labResultId;
    }

    // creating getter for consultationId
    public Integer getConsultationId() {
        return consultationId;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    // creating setter for consultationId
    public void setConsultationId(Integer consultationId) {
        this.consultationId = consultationId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    // creating getter for technicianId
    public Integer getTechnicianId() {
        return technicianId;
    }

    // creating setter for technicianId
    public void setTechnicianId(Integer technicianId) {
        this.technicianId = technicianId;
    }

    // creating getter for testType
    public String getTestType() {
        return testType;
    }

    // creating setter for testType
    public void setTestType(String testType) {
        this.testType = testType;
    }

    // creating getter for result
    public String getResult() {
        return result;
    }

    // creating setter for result
    public void setResult(String result) {
        this.result = result;
    }

    // creating getter for uploadedAt
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    // creating setter for uploadedAt
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    // implementing the toString method
    @Override
    public String toString() {
        return "\nTechnician Id: " + technicianId +
                "\nConsultation Id: " + consultationId +
                "\nAppointment Id: " + appointmentId +
                "\nTest Type: " + testType +
                "\nUploaded At: " + uploadedAt;
    }
}
