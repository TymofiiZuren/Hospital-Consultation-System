package ie.setu.hcs.integration;

import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.util.PasswordUtil;
import ie.setu.hcs.util.TransactionRunner;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionRunnerIT extends DatabaseIntegrationSupport {

    @Test
    void transactionRunnerRollsBackInsertedAccountWhenCallbackFails() throws Exception {
        assumeDatabaseAvailable();

        String email = uniqueEmail("txn");
        AccountDAOImpl accountDAO = new AccountDAOImpl();

        try {
            assertThrows(IllegalStateException.class, () -> TransactionRunner.inTransaction(conn -> {
                Account account = new Account(
                        email,
                        PasswordUtil.hash("secret123"),
                        1,
                        "Rollback",
                        "Transaction",
                        uniquePpsn("TX"),
                        "0870000001",
                        "Other",
                        true,
                        LocalDateTime.now()
                );
                accountDAO.save(conn, account);
                throw new IllegalStateException("Force rollback");
            }));

            assertNull(accountDAO.findByEmail(email));
        } finally {
            deleteAccountByEmail(email);
        }
    }
}
