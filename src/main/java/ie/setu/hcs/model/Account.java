// defining package ie.setu.hcs.model
package ie.setu.hcs.model;

// importing stuff
import java.time.LocalDateTime;

// implementing Account class
public class Account {
    // declaring attributes of the class
    private Integer accountId;
    private String email;
    private String passwordHash;
    private Integer roleId;
    private String lastName;
    private String firstName;
    private String ppsn;
    private String phone;
    private String gender;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // creating empty Account constructor
    public Account() {}

    // creating Account constructor and defining arguments
    public Account(String email, String passwordHash, Integer roleId,
                   String lastName, String firstName, String ppsn, String phone,
                   String gender, Boolean isActive, LocalDateTime createdAt) {
        // implementing attributes
        this.email = email;
        this.passwordHash = passwordHash;
        this.roleId = roleId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.ppsn = ppsn;
        this.phone = phone;
        this.gender = gender;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // creating Account constructor and defining arguments
    public Account(Integer accountId, String email, String passwordHash, Integer roleId,
                   String lastName, String firstName, String ppsn, String phone,
                   String gender, Boolean isActive, LocalDateTime createdAt) {
        // implementing attributes
        this.accountId = accountId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roleId = roleId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.ppsn = ppsn;
        this.phone = phone;
        this.gender = gender;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // creating getter for accountId
    public Integer getAccountId() {
        return this.accountId;
    }

    // creating getter for email
    public String getEmail() {
        return this.email;
    }

    // creating getter for passwordHash
    public String getPasswordHash() {
        return this.passwordHash;
    }

    // creating getter for roleId
    public Integer getRoleId() {
        return roleId;
    }

    // creating getter for lastName
    public String getLastName() {
        return lastName;
    }

    // creating getter for firstName
    public String getFirstName() {
        return firstName;
    }

    // creating getter for ppsn
    public String getPpsn() {
        return ppsn;
    }

    // creating getter for phone
    public String getPhone() {
        return phone;
    }

    // creating getter for gender
    public String getGender() {
        return gender;
    }

    // creating getter for isActive
    public Boolean isActive() {
        return isActive;
    }

    // creating getter for createdAt
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // creating setter for accountId
    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    // creating setter for email
    public void setEmail(String email) {
        this.email = email;
    }

    // creating setter for passwordHash
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // creating setter for roleId
    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    // creating setter for lastName
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // creating setter for firstName
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // creating setter for ppsn
    public void setPpsn(String ppsn) {
        this.ppsn = ppsn;
    }

    // creating setter for phone
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // creating setter for gender
    public void setGender(String gender) {
        this.gender = gender;
    }

    // creating setter for active
    public void setActive(Boolean active) {
        isActive = active;
    }

    // creating setter for createdAt
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // implementing the toString method
    @Override
    public String toString() {
        return "\nAccount Id: " + accountId +
                "\nFirst name: " + firstName +
                "\nLast name: " + lastName +
                "\nEmail: " + email +
                "\nRole Id: " + roleId +
                "\nActive: " + isActive;
    }
}