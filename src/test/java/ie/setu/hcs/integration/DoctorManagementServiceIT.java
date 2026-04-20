package ie.setu.hcs.integration;

import ie.setu.hcs.dao.impl.AccountDAOImpl;
import ie.setu.hcs.dao.impl.DoctorDAOImpl;
import ie.setu.hcs.model.Account;
import ie.setu.hcs.model.Doctor;
import ie.setu.hcs.service.DoctorManagementService;
import ie.setu.hcs.service.DoctorRegistrationService;
import org.junit.jupiter.api.Test;

import javax.swing.table.DefaultTableModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DoctorManagementServiceIT extends DatabaseIntegrationSupport {

    @Test
    void doctorManagementServiceLoadsAndUpdatesRegisteredDoctors() throws Exception {
        assumeDatabaseAvailable();

        String email = uniqueEmail("doctor");
        DoctorRegistrationService registrationService = new DoctorRegistrationService();
        DoctorManagementService managementService = new DoctorManagementService();
        AccountDAOImpl accountDAO = new AccountDAOImpl();
        DoctorDAOImpl doctorDAO = new DoctorDAOImpl();

        try {
            registrationService.registerDoctor(
                    "Integration",
                    "Doctor",
                    email,
                    "secret123",
                    uniquePpsn("DR"),
                    "0871231232",
                    "Male",
                    "DOC-IT-" + System.currentTimeMillis(),
                    "LIC-" + System.currentTimeMillis(),
                    4,
                    "Neurology",
                    "Neurologist"
            );

            Account account = accountDAO.findByEmail(email);
            assertNotNull(account);

            Doctor doctor = doctorDAO.findByAccountId(account.getAccountId());
            assertNotNull(doctor);
            assertNotNull(managementService.findDoctorById(doctor.getDoctorId()));
            assertNotNull(managementService.findAccountById(account.getAccountId()));

            managementService.updateDoctor(
                    doctor.getDoctorId(),
                    "Neurology Integration",
                    doctor.getLicenseNum(),
                    doctor.getEmployeeNum(),
                    5,
                    0,
                    managementService.getDepartmentName(doctor.getDepId())
            );

            Doctor updated = doctorDAO.findById(doctor.getDoctorId());
            assertEquals("Neurology Integration", updated.getSpecialization());
            assertEquals(5, updated.getYearsOfExperience());
            assertEquals(0, updated.getConsultationFee());

            DefaultTableModel model = managementService.getDoctorsForManagement();
            int emailColumn = model.findColumn("email");
            assertTrue(emailColumn >= 0);

            boolean found = false;
            for (int row = 0; row < model.getRowCount(); row++) {
                Object value = model.getValueAt(row, emailColumn);
                if (email.equals(value)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        } finally {
            deleteAccountByEmail(email);
        }
    }
}
