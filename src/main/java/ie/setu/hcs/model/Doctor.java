// defining package ie.setu.hcs.model
package ie.setu.hcs.model;

// implementing Doctor class
public class Doctor {
    // defining attributes of the class
    private Integer doctorId;
    private Integer accountId;
    private String specialization;
    private Integer licenseNum;
    private Integer yearsOfExperience;
    private Integer consultationFee;
    private Integer depId;
    
    // creating empty Doctor constructor
    public Doctor() {}

    // creating Doctor constructor with defining arguments
    public Doctor(Integer accountId, String specialization,
                  Integer licenseNum, Integer yearsOfExperience, Integer consultationFee,
                  Integer depId) {
        // implementing attributes
        this.accountId = accountId;
        this.specialization = specialization;
        this.licenseNum = licenseNum;
        this.yearsOfExperience = yearsOfExperience;
        this.consultationFee = consultationFee;
        this.depId = depId;
    }

    // creating Doctor constructor with defining arguments
    public Doctor(Integer doctorId, Integer accountId, String specialization,
                  Integer licenseNum, Integer yearsOfExperience, Integer consultationFee,
                  Integer depId) {
        // implementing attributes
        this.doctorId = doctorId;
        this.accountId = accountId;
        this.specialization = specialization;
        this.licenseNum = licenseNum;
        this.yearsOfExperience = yearsOfExperience;
        this.consultationFee = consultationFee;
        this.depId = depId;
    }

    // creating getter for doctorId
    public Integer getDoctorId() {
        return doctorId;
    }

    // creating getter for accountId
    public Integer getAccountId() {
        return accountId;
    }

    // creating getter for specialization
    public String getSpecialization() {
        return specialization;
    }

    // creating getter for licenseNum
    public Integer getLicenseNum() {
        return licenseNum;
    }

    // creating getter for yearsOfExperience
    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    // creating getter for consultationFee
    public Integer getConsultationFee() {
        return consultationFee;
    }

    // creating getter for depId
    public Integer getDepId() {
        return depId;
    }

    // creating setter for doctorId
    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    // creating setter for accountId
    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    // creating setter for specialization
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    // creating setter for licenseNum
    public void setLicenseNum(Integer licenseNum) {
        this.licenseNum = licenseNum;
    }

    // creating setter for yearsOfExperience
    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    // creating setter for consultationFee
    public void setConsultationFee(Integer consultationFee) {
        this.consultationFee = consultationFee;
    }

    // creating setter for depId
    public void setDepId(Integer depId) {
        this.depId = depId;
    }

    // implementing the toString method
    @Override
    public String toString() {
        return "\nDoctor Id: " + doctorId +
                "\nAccount Id: " + accountId +
                "\nDep Id: " + depId +
                "\nLicense Number: " + licenseNum;
    }
}
