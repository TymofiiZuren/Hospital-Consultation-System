// defining package ie.setu.hcs.model
package ie.setu.hcs.model;

// importing stuff
import java.time.LocalDate;

// implementing Insurance class
public class Insurance {
    // defining attributes
    private Integer insuranceId;
    private Integer patientId;
    private String providerName;
    private String policyNum;
    private String status;
    private LocalDate expirationDate;
    
    // creating empty Insurance constructor
    public Insurance() {}

    // creating Insurance constructor with defining arguments
    public Insurance(Integer patientId, String providerName,
                     String policyNum, String status, LocalDate expirationDate) {
        // implementing attributes
        this.patientId = patientId;
        this.providerName = providerName;
        this.policyNum = policyNum;
        this.status = status;
        this.expirationDate = expirationDate;
    }

    // creating Insurance constructor with defining arguments
    public Insurance(Integer insuranceId, Integer patientId, String providerName,
                     String policyNum, String status, LocalDate expirationDate) {
        // implementing attributes
        this.insuranceId = insuranceId;
        this.patientId = patientId;
        this.providerName = providerName;
        this.policyNum = policyNum;
        this.status = status;
        this.expirationDate = expirationDate;
    }

    // creating getter for insuranceId
    public Integer getInsuranceId() {
        return insuranceId;
    }

    // creating setter for insuranceId
    public void setInsuranceId(Integer insuranceId) {
        this.insuranceId = insuranceId;
    }

    // creating getter for patientId
    public Integer getPatientId() {
        return patientId;
    }

    // creating setter for patientId
    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    // creating getter for providerName
    public String getProviderName() {
        return providerName;
    }

    // creating setter for providerName
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    // creating getter for policyNum
    public String getPolicyNum() {
        return policyNum;
    }

    // creating setter for policyNum
    public void setPolicyNum(String policyNum) {
        this.policyNum = policyNum;
    }

    // creating getter for status
    public String getStatus() {
        return status;
    }

    // creating setter for status
    public void setStatus(String status) {
        this.status = status;
    }

    // creating getter for expirationDate
    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    // creating setter for expirationDate
    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    // implementing the toString method
    @Override
    public String toString() {
        return "Insurance Id: " + insuranceId +
                "Patient Id: " + patientId +
                "Provider Name: " + providerName +
                "Policy Num: " +  policyNum +
                "Expiration Date: " + expirationDate;
    }

}
