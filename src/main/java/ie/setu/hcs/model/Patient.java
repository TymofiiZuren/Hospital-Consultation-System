// defining package ie.setu.hcs.model
package ie.setu.hcs.model;

// importing from java api
import java.time.LocalDate;

// implementing Patient class
public class Patient {
    // defining attributes of the class
    private Integer patientId;
    private Integer accountId;
    private LocalDate dateOfBirth;
    private String address;
    private String eircode;
    private String bloodType;
    private String medicalRecordNum;

    // creating empty Patient constructor
    public Patient() {}

    // creating Patient constructor with defining arguments
    public Patient(Integer accountId, LocalDate dateOfBirth,
                   String address, String eircode, String bloodType,
                   String medicalRecordNum) {
        // implement attributes
        this.accountId = accountId;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.eircode = eircode;
        this.bloodType = bloodType;
        this.medicalRecordNum = medicalRecordNum;
    }
    
    // creating Patient constructor with defining arguments
    public Patient(Integer patientId, Integer accountId, LocalDate dateOfBirth,
                   String address, String eircode, String bloodType,
                   String medicalRecordNum) {
        // implement attributes
        this.patientId = patientId;
        this.accountId = accountId;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.eircode = eircode;
        this.bloodType = bloodType;
        this.medicalRecordNum = medicalRecordNum;
    }

    // creating getter for patientId
    public Integer getPatientId() {
        return patientId;
    }

    // creating getter for accountId
    public Integer getAccountId() {
        return accountId;
    }

    // creating getter for dateOfBirth
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    // creating getter for address
    public String getAddress() {
        return address;
    }

    // creating getter for eircode
    public String getEircode() {
        return eircode;
    }

    // creating getter for bloodType
    public String getBloodType() {
        return bloodType;
    }

    // creating getter for medicalRecordNum
    public String getMedicalRecordNum() {
        return medicalRecordNum;
    }

    // creating setter for patientId
    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    // creating setter for accountId
    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    // creating setter for dateOfBirth
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    // creating setter for address
    public void setAddress(String address) {
        this.address = address;
    }

    // creating setter for eircode
    public void setEircode(String eircode) {
        this.eircode = eircode;
    }

    // creating setter for bloodType
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    // creating setter for recordNum
    public void setMedicalRecordNum(String medicalRecordNum) {
        this.medicalRecordNum = medicalRecordNum;
    }

    // implementing the toString method
    @Override
    public String toString() {
        return "\nPatient Id: " + patientId +
                "\nAccount Id: " + accountId +
                "\nDOB: " + dateOfBirth +
                "\nMedical record number: " + medicalRecordNum;
    }
}
