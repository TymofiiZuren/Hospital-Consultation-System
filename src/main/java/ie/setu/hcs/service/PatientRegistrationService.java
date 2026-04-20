// defining package ie.setu.hcs.service
package ie.setu.hcs.service;

// importing stuff
import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.PatientDAOImpl;
import ie.setu.hcs.dao.interfaces.AccountDAO;
import ie.setu.hcs.dao.interfaces.PatientDAO;
import ie.setu.hcs.exception.ConflictException;
import ie.setu.hcs.exception.OperationFailedException;
import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Patient;
import ie.setu.hcs.util.PasswordUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

// implementing PatientRegistrationService class
public class PatientRegistrationService {

    private final AccountDAO accountDAO;
    private final PatientDAO patientDAO;

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
        if (firstName.isBlank() || lastName.isBlank() ||
                email.isBlank() || password.isBlank()) {
            throw new ValidationException("Required fields cannot be empty.");
        }

        // check if the email already exists
        if (accountDAO.existsByEmail(email)) {
            throw new ConflictException("Email already registered.");
        }

        // hash the password
        String hashedPassword = PasswordUtil.hash(password);

        // create Account
        Account account = new Account(
                email,
                hashedPassword,
                1, // roleId = PATIENT
                lastName,
                firstName,
                ppsn,
                phone,
                gender,
                true,
                LocalDateTime.now()
        );
        account.setAdmin(false);

        // get the accountId
        accountDAO.save(account);
        Integer accountId = account.getAccountId();

        try {
            // create Patient
            Patient patient = new Patient(
                    accountId,
                    dob,
                    address,
                    eircode,
                    bloodType,
                    generateMedicalRecordNumber()
            );

            // save the patient
            patientDAO.save(patient);
        } catch (Exception ex) {
            // ROLLBACK: Delete the account if patient creation fails
            try {
                accountDAO.delete(accountId);
            } catch (Exception deleteEx) {
                // Log the error but don't suppress the original exception
                System.err.println("Failed to rollback account creation: " + deleteEx.getMessage());
            }
            // Re-throw the original exception
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
