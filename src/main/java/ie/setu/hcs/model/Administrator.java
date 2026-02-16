// defining package ie.setu.hcs.model
package ie.setu.hcs.model;

// implementing Administrator class
public class Administrator {
    // defining attributes of the class
    private Integer adminId;
    private Integer accountId;
    private String jobTitle;
    private String employeeNum;
    private Integer depId;

    // creating empty Administrator constructor
    public Administrator() {}

    // creating Administrator constructor and defining arguments
    public Administrator(Integer accountId, String jobTitle,
                         String employeeNum, Integer depId) {
        // implementing attributes
        this.accountId = accountId;
        this.jobTitle = jobTitle;
        this.employeeNum = employeeNum;
        this.depId = depId;
    }

    // creating Administrator constructor and defining arguments
    public Administrator(Integer adminId, Integer accountId, String jobTitle,
                         String employeeNum, Integer depId) {
        // implementing attributes
        this.adminId = adminId;
        this.accountId = accountId;
        this.jobTitle = jobTitle;
        this.employeeNum = employeeNum;
        this.depId = depId;
    }

    // creating getter for adminId
    public Integer getAdminId() {
        return adminId;
    }

    // creating getter for accountId
    public Integer getAccountId() {
        return accountId;
    }

    // creating getter for jobTitle
    public String getJobTitle() {
        return jobTitle;
    }

    // creating getter for employeeNum
    public String getEmployeeNum() {
        return employeeNum;
    }

    // creating getter for depId
    public Integer getDepId() {
        return depId;
    }

    // creating setter for adminId
    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    // creating setter for accountId
    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    // creating setter for jobTitle
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    // creating setter for employeeNum
    public void setEmployeeNum(String employeeNum) {
        this.employeeNum = employeeNum;
    }

    // creating setter for depId
    public void setDepId(Integer depId) {
        this.depId = depId;
    }

    // implementing the toString method
    @Override
    public String toString() {
        return "\nAdmin Id: " + adminId +
                "\nAccount Id: " + accountId +
                "\nDep Id: " + depId +
                "\nEmployee Num: " + employeeNum;
    }
}

