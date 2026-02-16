// defining package ie.setu.hcs.model
package ie.setu.hcs.model;

// importing stuff
import java.time.LocalDateTime;

// implementing Invoice class
public class Invoice {
    // defining attributes of the class
    private Integer invoiceId;
    private Integer patientId;
    private Integer consultationId;
    private Float amount;
    private String invoiceStatus;
    private LocalDateTime issuedAt;
    private LocalDateTime paidAt;

    // creating empty Invoice constructor
    public Invoice() {}

    // creating Invoice constructor with defining arguments
    public Invoice(Integer patientId, Integer consultationId,
                   Float amount, String invoiceStatus, LocalDateTime issuedAt,
                   LocalDateTime paidAt) {
        // implementing attributes
        this.patientId = patientId;
        this.consultationId = consultationId;
        this.amount = amount;
        this.invoiceStatus = invoiceStatus;
        this.issuedAt = issuedAt;
        this.paidAt = paidAt;
    }

    // creating Invoice constructor with defining arguments
    public Invoice(Integer invoiceId, Integer patientId, Integer consultationId,
                   Float amount, String invoiceStatus, LocalDateTime issuedAt,
                   LocalDateTime paidAt) {
        // implementing attributes
        this.invoiceId = invoiceId;
        this.patientId = patientId;
        this.consultationId = consultationId;
        this.amount = amount;
        this.invoiceStatus = invoiceStatus;
        this.issuedAt = issuedAt;
        this.paidAt = paidAt;
    }

    // creating getter for invoiceId
    public Integer getInvoiceId() {
        return invoiceId;
    }

    // creating setter for invoiceId
    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    // creating getter for patientId
    public Integer getPatientId() {
        return patientId;
    }

    // creating setting for patientId
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

    // creating getter for amount
    public Float getAmount() {
        return amount;
    }

    // creating setter for amount
    public void setAmount(Float amount) {
        this.amount = amount;
    }

    // creating getter for invoiceStatus
    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    // creating setter for invoiceStatus
    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    // creating getter for issuedAt
    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    // creating setter for issuedAt
    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    // creating getter for paidAt
    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    // creating setter for paidAt
    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    // implementing toString method
    @Override
    public String toString() {
        return "Invoice Id: " + invoiceId +
                "Patient Id: " + patientId +
                "Consultation Id: " + consultationId +
                "Amount: " + amount +
                "Issued At: " + issuedAt;
    }
}
