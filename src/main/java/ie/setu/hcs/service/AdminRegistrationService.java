package ie.setu.hcs.service;

import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.AdministratorDAOImpl;
import ie.setu.hcs.dao.impl.DepartmentDAOImpl;
import ie.setu.hcs.exception.ConflictException;
import ie.setu.hcs.exception.OperationFailedException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Administrator;
import ie.setu.hcs.util.InputValidationUtil;
import ie.setu.hcs.util.PasswordUtil;
import ie.setu.hcs.util.TransactionRunner;

import java.time.LocalDateTime;

public class AdminRegistrationService {

    private final AccountDAOImpl accountDAO;
    private final AdministratorDAOImpl administratorDAO;
    private final DepartmentDAOImpl departmentDAO;

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
                4, // roleId = Administrator
                normalizedLastName,
                normalizedFirstName,
                ppsn,
                phone,
                gender,
                true,
                LocalDateTime.now()
        );
        account.setAdmin(true);

        try {
            Integer depId = departmentDAO.findByName(department);

            Administrator administrator = new Administrator(
                    null,
                    jobTitle,
                    employeeNum,
                    depId
            );
            TransactionRunner.inTransaction(conn -> {
                accountDAO.save(conn, account);
                administrator.setAccountId(account.getAccountId());
                administratorDAO.save(conn, administrator);
                return null;
            });
        } catch (Exception ex) {
            throw new OperationFailedException("Administrator registration failed: " + ex.getMessage(), ex);
        }
    }
}
