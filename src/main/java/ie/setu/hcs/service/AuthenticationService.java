package ie.setu.hcs.service;

import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.interfaces.AccountDAO;
import ie.setu.hcs.exception.DataAccessException;
import ie.setu.hcs.exception.InactiveAccountException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.util.PasswordUtil;

public class AuthenticationService {

    private final AccountDAO accountDAO;

    public AuthenticationService() {
        this(new AccountDAOImpl());
    }

    public AuthenticationService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public Account authenticate(String email, String password) throws Exception {
        Account account;
        try {
            account = accountDAO.findByEmail(email);
        } catch (Exception ex) {
            throw new DataAccessException("Authentication could not reach the account store.", ex);
        }

        if (account == null) return null;

        if (!account.getPasswordHash().equals(PasswordUtil.hash(password))) {
            return null;
        }

        if (!Boolean.TRUE.equals(account.isActive())) {
            throw new InactiveAccountException("This account is inactive. Please contact an administrator.");
        }

        return account;
    }
}
