package ie.setu.hcs.service;

import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.AdministratorDAOImpl;
import ie.setu.hcs.dao.impl.DepartmentDAOImpl;
import ie.setu.hcs.dao.interfaces.AccountDAO;
import ie.setu.hcs.dao.interfaces.AdministratorDAO;
import ie.setu.hcs.dao.interfaces.DepartmentDAO;
import ie.setu.hcs.exception.ConflictException;
import ie.setu.hcs.exception.OperationFailedException;
import ie.setu.hcs.exception.ValidationException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Administrator;
import ie.setu.hcs.util.PasswordUtil;

import java.time.LocalDateTime;

public class AdminRegistrationService {

    private final AccountDAO accountDAO;
    private final AdministratorDAO administratorDAO;
    private final DepartmentDAO departmentDAO;

    public AdminRegistrationService() {
        this.accountDAO = new AccountDAOImpl();
        this.administratorDAO = new AdministratorDAOImpl();
        this.departmentDAO = new DepartmentDAOImpl();
    }

    public void registerAdmin(String firstName,
                              String lastName,
                              String email,
                              String password,
                              String ppsn,
                              String phone,
                              String gender,
                              String employeeNum,
                              String department,
                              String jobTitle) throws Exception {

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
                4, // roleId = Administrator
                lastName,
                firstName,
                ppsn,
                phone,
                gender,
                true,
                LocalDateTime.now()
        );
        account.setAdmin(true);

        accountDAO.save(account);
        Integer accountId = account.getAccountId();

        try {
            Integer depId = departmentDAO.findByName(department);

            Administrator administrator = new Administrator(
                    accountId,
                    jobTitle,
                    employeeNum,
                    depId
            );
            administratorDAO.save(administrator);
        } catch (Exception ex) {
            // ROLLBACK: Delete the account if administrator creation fails
            try {
                accountDAO.delete(accountId);
            } catch (Exception deleteEx) {
                // Log the error but don't suppress the original exception
                System.err.println("Failed to rollback account creation: " + deleteEx.getMessage());
            }
            // Re-throw the original exception
            throw new OperationFailedException("Administrator registration failed: " + ex.getMessage(), ex);
        }
    }
}
