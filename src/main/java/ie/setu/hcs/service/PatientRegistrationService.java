// defining package ie.setu.hcs.service
package ie.setu.hcs.service;

// importing stuff
import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.PatientDAOImpl;
import ie.setu.hcs.exception.ConflictException;
import ie.setu.hcs.exception.OperationFailedException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Patient;
import ie.setu.hcs.util.InputValidationUtil;
import ie.setu.hcs.util.PasswordUtil;
import ie.setu.hcs.util.TransactionRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

// implementing PatientRegistrationService class
public class PatientRegistrationService {

    private final AccountDAOImpl accountDAO;
    private final PatientDAOImpl patientDAO;

    // creating PatientRegistration constructor to initialize DAOs
    public PatientRegistrationService() {
        this.accountDAO = new AccountDAOImpl();
        this.patientDAO = new PatientDAOImpl();
    }

    // implementing registerPatient method
    public void registerPatient(String firstName,
                                String lastName,
                                String email,
                                String password,
                                String ppsn,
                                String phone,
                                String gender,
                                LocalDate dob,
                                String address,
                                String eircode,
                                String bloodType) throws Exception {

        // check the required fields
        String normalizedFirstName = InputValidationUtil.requireNonBlank(firstName, "First name");
        String normalizedLastName = InputValidationUtil.requireNonBlank(lastName, "Last name");
        String normalizedEmail = InputValidationUtil.requireEmail(email);
        String normalizedPassword = InputValidationUtil.requireNonBlank(password, "Password");

        // check if the email already exists
        if (accountDAO.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email already registered.");
        }

        // hash the password
        String hashedPassword = PasswordUtil.hash(normalizedPassword);

        // create Account
        Account account = new Account(
                normalizedEmail,
                hashedPassword,
                1, // roleId = PATIENT
                normalizedLastName,
                normalizedFirstName,
                ppsn,
                phone,
                gender,
                true,
                LocalDateTime.now()
        );

        try {
            Patient patient = new Patient(
                    null,
                    dob,
                    address,
                    eircode,
                    bloodType,
                    generateMedicalRecordNumber()
            );

            TransactionRunner.inTransaction(conn -> {
                accountDAO.save(conn, account);
                patient.setAccountId(account.getAccountId());
                patientDAO.save(conn, patient);
                return null;
            });
        } catch (Exception ex) {
            throw new OperationFailedException("Patient registration failed: " + ex.getMessage(), ex);
        }
    }

    private String generateMedicalRecordNumber() throws Exception {
        for (int attempt = 0; attempt < 100; attempt++) {
            int candidate = ThreadLocalRandom.current().nextInt(100_000_000, 1_000_000_000);
            if (patientDAO.findByMedRecordNum(candidate) == null) {
                return String.valueOf(candidate);
            }
        }

        throw new OperationFailedException("Could not generate a unique medical record number.");
    }
}
