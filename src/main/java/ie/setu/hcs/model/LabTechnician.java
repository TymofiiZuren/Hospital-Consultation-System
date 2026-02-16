// defining package ie.setu.hcs.model
package ie.setu.hcs.model;

// implementing LabTechnician class
public class LabTechnician {
    // defining attributes of the class
    private Integer technicianId;
    private Integer accountId;
    private String qualification;
    private String employeeNum;
    private String labName;
    private String shift;
    
    // creating empty LabTechnician constructor
    public LabTechnician() {}

    // creating LabTechnician construction with defining arguments
    public LabTechnician(Integer accountId, String qualification,
                         String employeeNum, String labName, String shift) {
        // implementing attributes
        this.accountId = accountId;
        this.qualification = qualification;
        this.employeeNum = employeeNum;
        this.labName = labName;
        this.shift = shift;
    }

    // creating LabTechnician construction with defining arguments
    public LabTechnician(Integer technicianId, Integer accountId, String qualification,
                         String employeeNum, String labName, String shift) {
        // implementing attributes
        this.technicianId = technicianId;
        this.accountId = accountId;
        this.qualification = qualification;
        this.employeeNum = employeeNum;
        this.labName = labName;
        this.shift = shift;
    }

    // creating getter for technicianId
    public Integer getTechnicianId() {
        return technicianId;
    }

    // creating getter for accountId
    public Integer getAccountId() {
        return accountId;
    }

    // creating getter for qualification
    public String getQualification() {
        return qualification;
    }

    // creating getter for employeeNum
    public String getEmployeeNum() {
        return employeeNum;
    }

    // creating getter for labName
    public String getLabName() {
        return labName;
    }

    // creating getter for shift
    public String getShift() {
        return shift;
    }

    // creating setter for technicianId
    public void setTechnicianId(Integer technicianId) {
        this.technicianId = technicianId;
    }

    // creating setter for accountId
    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    // creating setter for qualification
    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    // creating setter for employeeNum
    public void setEmployeeNum(String employeeNum) {
        this.employeeNum = employeeNum;
    }

    // creating setter for labName
    public void setLabName(String labName) {
        this.labName = labName;
    }

    // creating setter for shift
    public void setShift(String shift) {
        this.shift = shift;
    }

    // implementing the toString method
    @Override
    public String toString() {
        return "\nTechnician Id: " + technicianId +
                "\nAccount Id: " + accountId +
                "\nQualification: " + qualification +
                "\nEmployee Number: " + employeeNum;
    }
}
