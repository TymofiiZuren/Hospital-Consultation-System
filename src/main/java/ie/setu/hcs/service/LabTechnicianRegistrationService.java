package ie.setu.hcs.service;

import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.LabTechnicianDAOImpl;
import ie.setu.hcs.exception.ConflictException;
import ie.setu.hcs.exception.OperationFailedException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.LabTechnician;
import ie.setu.hcs.util.InputValidationUtil;
import ie.setu.hcs.util.PasswordUtil;
import ie.setu.hcs.util.TransactionRunner;

import java.time.LocalDateTime;

public class LabTechnicianRegistrationService {

    private final AccountDAOImpl accountDAO;
    private final LabTechnicianDAOImpl technicianDAO;

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

        String normalizedFirstName = InputValidationUtil.requireNonBlank(firstName, "First name");
        String normalizedLastName = InputValidationUtil.requireNonBlank(lastName, "Last name");
        String normalizedEmail = InputValidationUtil.requireEmail(email);
        String normalizedPassword = InputValidationUtil.requireNonBlank(password, "Password");

        if (accountDAO.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email already registered.");
        }

        String hashedPassword = PasswordUtil.hash(normalizedPassword);

        Account account = new Account(
                normalizedEmail,
                hashedPassword,
                3, // roleId = Lab technician
                normalizedLastName,
                normalizedFirstName,
                ppsn,
                phone,
                gender,
                true,
                LocalDateTime.now()
        );
        account.setAdmin(false);

        try {
            LabTechnician technician = new LabTechnician(
                    null,
                    qualification,
                    employeeNum,
                    labName,
                    shift
            );
            TransactionRunner.inTransaction(conn -> {
                accountDAO.save(conn, account);
                technician.setAccountId(account.getAccountId());
                technicianDAO.save(conn, technician);
                return null;
            });
        } catch (Exception ex) {
            throw new OperationFailedException("Lab Technician registration failed: " + ex.getMessage(), ex);
        }
    }
}
