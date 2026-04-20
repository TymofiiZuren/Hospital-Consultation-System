package ie.setu.hcs.service;

import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.LabTechnicianDAOImpl;
import ie.setu.hcs.dao.interfaces.AccountDAO;
import ie.setu.hcs.dao.interfaces.LabTechnicianDAO;
import ie.setu.hcs.exception.ConflictException;
import ie.setu.hcs.exception.OperationFailedException;
import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.LabTechnician;
import ie.setu.hcs.util.PasswordUtil;

import java.time.LocalDateTime;

public class LabTechnicianRegistrationService {

    private final AccountDAO accountDAO;
    private final LabTechnicianDAO technicianDAO;

    public LabTechnicianRegistrationService() {
        this.accountDAO = new AccountDAOImpl();
        this.technicianDAO = new LabTechnicianDAOImpl();
    }

    public void registerTechnician(String firstName,
                                   String lastName,
                                   String email,
                                   String password,
                                   String ppsn,
                                   String phone,
                                   String gender,
                                   String employeeNum,
                                   String qualification,
                                   String labName,
                                   String shift) throws Exception {

        if (firstName.isBlank() || lastName.isBlank() ||
                email.isBlank() || password.isBlank()) {
            throw new ValidationException("Required fields cannot be empty.");
        }

        if (accountDAO.existsByEmail(email)) {
            throw new ConflictException("Email already registered.");
        }

        String hashedPassword = PasswordUtil.hash(password);

        Account account = new Account(
                email,
                hashedPassword,
                3, // roleId = Lab technician
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
            LabTechnician technician = new LabTechnician(
                    accountId,
                    qualification,
                    employeeNum,
                    labName,
                    shift
            );
            technicianDAO.save(technician);
        } catch (Exception ex) {
            // ROLLBACK: Delete the account if technician creation fails
            try {
                accountDAO.delete(accountId);
            } catch (Exception deleteEx) {
                // Log the error but don't suppress the original exception
                System.err.println("Failed to rollback account creation: " + deleteEx.getMessage());
            }
            // Re-throw the original exception
            throw new OperationFailedException("Lab Technician registration failed: " + ex.getMessage(), ex);
        }
    }
}
