// defining package ie.setu.hcs.service
package ie.setu.hcs.service;

// importing stuff

import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.DepartmentDAOImpl;
import ie.setu.hcs.dao.impl.DoctorDAOImpl;
import ie.setu.hcs.exception.ConflictException;
import ie.setu.hcs.exception.OperationFailedException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Doctor;
import ie.setu.hcs.util.InputValidationUtil;
import ie.setu.hcs.util.PasswordUtil;
import ie.setu.hcs.util.TransactionRunner;

import java.time.LocalDateTime;

// implementing DoctorRegistrationService class
public class DoctorRegistrationService {

    // defining attributes of the class
    private final AccountDAOImpl accountDAO;
    private final DoctorDAOImpl doctorDAO;
    private final DepartmentDAOImpl departmentDAO;

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
                               String employeeNum,
                               String medicalLicense,
                               int yearsExperience,
                               String department,
                               String specialization) throws Exception {

        // required field validation
        String normalizedFirstName = InputValidationUtil.requireNonBlank(firstName, "First name");
        String normalizedLastName = InputValidationUtil.requireNonBlank(lastName, "Last name");
        String normalizedEmail = InputValidationUtil.requireEmail(email);
        String normalizedPassword = InputValidationUtil.requireNonBlank(password, "Password");

        // email uniqueness
        if (accountDAO.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email already registered.");
        }

        // hashing password
        String hashedPassword = PasswordUtil.hash(normalizedPassword);

        // create Account using YOUR constructor
        Account account = new Account(
                normalizedEmail,
                hashedPassword,
                2, // roleId = DOCTOR
                normalizedLastName,
                normalizedFirstName,
                ppsn,
                phone,
                gender,
                true,
                LocalDateTime.now()
        );
        
        try {
            // getting ids
            Integer depId = departmentDAO.findByName(department);
            // create Doctor
            Doctor doctor = new Doctor(
                    null,
                    InputValidationUtil.requireNonBlank(employeeNum, "Employee number"),
                    specialization,
                    medicalLicense,
                    yearsExperience,
                    0, // fee is default 0
                    depId
            );
            TransactionRunner.inTransaction(conn -> {
                accountDAO.save(conn, account);
                doctor.setAccountId(account.getAccountId());
                doctorDAO.save(conn, doctor);
                return null;
            });
        } catch (Exception ex) {
            throw new OperationFailedException("Doctor registration failed: " + ex.getMessage(), ex);
        }
    }
}
