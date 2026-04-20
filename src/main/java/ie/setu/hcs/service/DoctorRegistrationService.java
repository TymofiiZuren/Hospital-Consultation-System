// defining package ie.setu.hcs.service
package ie.setu.hcs.service;

// importing stuff

import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.DoctorDAOImpl;
import ie.setu.hcs.dao.impl.DepartmentDAOImpl;
import ie.setu.hcs.dao.interfaces.AccountDAO;
import ie.setu.hcs.dao.interfaces.DepartmentDAO;
import ie.setu.hcs.dao.interfaces.DoctorDAO;
import ie.setu.hcs.exception.ConflictException;
import ie.setu.hcs.exception.OperationFailedException;
import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Doctor;
import ie.setu.hcs.util.PasswordUtil;

import java.time.LocalDateTime;

// implementing DoctorRegistrationService class
public class DoctorRegistrationService {

    // defining attributes of the class
    private final AccountDAO accountDAO;
    private final DoctorDAO doctorDAO;
    private final DepartmentDAO departmentDAO;

    // creating constructor for the class
    public DoctorRegistrationService() {
        this.accountDAO = new AccountDAOImpl();
        this.doctorDAO = new DoctorDAOImpl();
        this.departmentDAO = new DepartmentDAOImpl();
    }

    // creating registerDoctor method
    public void registerDoctor(String firstName,
                               String lastName,
                               String email,
                               String password,
                               String ppsn,
                               String phone,
                               String gender,
                               String medicalLicense,
                               int yearsExperience,
                               String department,
                               String specialization) throws Exception {

        // required field validation
        if (firstName.isBlank() || lastName.isBlank() ||
                email.isBlank() || password.isBlank()) {
            throw new ValidationException("Required fields cannot be empty.");
        }

        // email uniqueness
        if (accountDAO.existsByEmail(email)) {
            throw new ConflictException("Email already registered.");
        }

        // hashing password
        String hashedPassword = PasswordUtil.hash(password);

        // create Account using YOUR constructor
        Account account = new Account(
                email,
                hashedPassword,
                2, // roleId = DOCTOR
                lastName,
                firstName,
                ppsn,
                phone,
                gender,
                true,
                LocalDateTime.now()
        );
        account.setAdmin(false);

        accountDAO.save(account);
        Integer accountId = account.getAccountId();
        
        try {
            // getting ids
            Integer depId = departmentDAO.findByName(department);
            // create Doctor
            Doctor doctor = new Doctor(
                    accountId,
                    specialization,
                    medicalLicense,
                    yearsExperience,
                    0, // fee is default 0
                    depId
            );
            doctorDAO.save(doctor);
        } catch (Exception ex) {
            // ROLLBACK: Delete the account if doctor creation fails
            try {
                accountDAO.delete(accountId);
            } catch (Exception deleteEx) {
                // Log the error but don't suppress the original exception
                System.err.println("Failed to rollback account creation: " + deleteEx.getMessage());
            }
            // Re-throw the original exception
            throw new OperationFailedException("Doctor registration failed: " + ex.getMessage(), ex);
        }
    }
}
