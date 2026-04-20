package ie.setu.hcs.integration;

import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.PatientDAOImpl;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Patient;
import ie.setu.hcs.service.PatientRegistrationService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatientRegistrationServiceIT extends DatabaseIntegrationSupport {

    @Test
    void registerPatientCreatesAccountAndPatientWithGeneratedMedicalRecordNumber() throws Exception {
        assumeDatabaseAvailable();

        String email = uniqueEmail("patient");
        PatientRegistrationService service = new PatientRegistrationService();
        AccountDAOImpl accountDAO = new AccountDAOImpl();
        PatientDAOImpl patientDAO = new PatientDAOImpl();

        try {
            service.registerPatient(
                    "Integration",
                    "Patient",
                    email,
                    "secret123",
                    uniquePpsn("PT"),
                    "0871231231",
                    "Female",
                    LocalDate.of(1999, 5, 12),
                    "Dublin",
                    "D01 TEST",
                    "O+"
            );

            Account account = accountDAO.findByEmail(email);
            assertNotNull(account);
            assertNotNull(account.getAccountId());
            assertNotEquals("secret123", account.getPasswordHash());

            Patient patient = patientDAO.findByAccountId(account.getAccountId());
            assertNotNull(patient);
            assertNotNull(patient.getPatientId());
            assertNotNull(patient.getMedicalRecordNum());
            assertFalse(patient.getMedicalRecordNum().isBlank());
            assertTrue(patient.getMedicalRecordNum().chars().allMatch(Character::isDigit));
        } finally {
            deleteAccountByEmail(email);
        }
    }
}
