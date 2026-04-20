package ie.setu.hcs.service;

import ie.setu.hcs.dao.interfaces.AccountDAO;
import ie.setu.hcs.exception.DataAccessException;
import ie.setu.hcs.exception.InactiveAccountException;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {

    @Test
    void authenticateReturnsAccountForMatchingActiveCredentials() throws Exception {
        Account account = account(true);
        AuthenticationService service = new AuthenticationService(new StubAccountDAO(account, null));

        Account result = service.authenticate("jay@icloud.com", "secret123");

        assertNotNull(result);
        assertEquals("jay@icloud.com", result.getEmail());
    }

    @Test
    void authenticateReturnsNullForWrongPassword() throws Exception {
        Account account = account(true);
        AuthenticationService service = new AuthenticationService(new StubAccountDAO(account, null));

        Account result = service.authenticate("jay@icloud.com", "wrong-password");

        assertNull(result);
    }

    @Test
    void authenticateThrowsInactiveAccountExceptionForInactiveAccount() throws Exception {
        Account account = account(false);
        AuthenticationService service = new AuthenticationService(new StubAccountDAO(account, null));

        InactiveAccountException ex = assertThrows(
                InactiveAccountException.class,
                () -> service.authenticate("jay@icloud.com", "secret123")
        );

        assertEquals("This account is inactive. Please contact an administrator.", ex.getMessage());
    }

    @Test
    void authenticateWrapsDaoFailuresInDataAccessException() {
        AuthenticationService service = new AuthenticationService(
                new StubAccountDAO(null, new SQLException("db offline"))
        );

        DataAccessException ex = assertThrows(
                DataAccessException.class,
                () -> service.authenticate("jay@icloud.com", "secret123")
        );

        assertEquals("Authentication could not reach the account store.", ex.getMessage());
        assertInstanceOf(SQLException.class, ex.getCause());
    }

    private Account account(boolean active) throws Exception {
        Account account = new Account(
                9,
                "jay@icloud.com",
                PasswordUtil.hash("secret123"),
                1,
                "Klepetz",
                "Jay",
                "1234567A",
                "0871234567",
                "Male",
                active,
                false,
                LocalDateTime.now()
        );
        account.setActive(active);
        return account;
    }

    private static final class StubAccountDAO implements AccountDAO {
        private final Account account;
        private final SQLException failure;

        private StubAccountDAO(Account account, SQLException failure) {
            this.account = account;
            this.failure = failure;
        }

        @Override
        public Account findByEmail(String email) throws SQLException {
            if (failure != null) {
                throw failure;
            }
            return account;
        }

        @Override
        public Boolean existsByEmail(String email) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DefaultTableModel findByRoleId(Integer roleId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deactivate(Integer accountId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void save(Account entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Account findById(Integer id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DefaultTableModel findAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(Account entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(Integer id) {
            throw new UnsupportedOperationException();
        }
    }
}
