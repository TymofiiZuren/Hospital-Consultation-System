package ie.setu.hcs.integration;

import ie.setu.hcs.dao.impl.AppointmentDAOImpl;
import ie.setu.hcs.model.Appointment;
import ie.setu.hcs.service.AppointmentService;
import ie.setu.hcs.service.ConsultationService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConsultationServiceIT extends DatabaseIntegrationSupport {

    @Test
    void saveAndDeleteConsultationKeepsConsultationMedicalRecordAndInvoiceInSync() throws Exception {
        assumeDatabaseAvailable();

        AppointmentService appointmentService = new AppointmentService();
        ConsultationService consultationService = new ConsultationService();
        AppointmentDAOImpl appointmentDAO = new AppointmentDAOImpl();
        Integer appointmentId = null;

        try {
            appointmentId = appointmentService.createAppointment(
                    3,
                    1,
                    LocalDateTime.now().plusDays(30).withHour(10).withMinute(0).withSecond(0).withNano(0),
                    "Accepted",
                    "Integration consultation need",
                    "IT-ROOM"
            );

            Integer consultationId = consultationService.saveConsultation(
                    appointmentId,
                    "Integration diagnosis",
                    "Integration notes",
                    "Integration prescription",
                    55f
            );

            assertNotNull(consultationId);
            assertEquals(consultationId, findConsultationIdByAppointmentId(appointmentId));
            assertEquals("Integration prescription", consultationService.getPrescriptionForConsultation(consultationId));
            assertEquals(55f, consultationService.getInvoiceAmountForConsultation(consultationId));

            Appointment updatedAppointment = appointmentDAO.findById(appointmentId);
            assertNotNull(updatedAppointment);
            assertEquals("Completed", updatedAppointment.getStatus());

            assertEquals(1, countRows("SELECT COUNT(*) FROM medical_records WHERE consultation_id = ?", consultationId));
            assertEquals(1, countRows("SELECT COUNT(*) FROM invoices WHERE consultation_id = ?", consultationId));

            consultationService.deleteConsultation(consultationId);

            assertNull(findConsultationIdByAppointmentId(appointmentId));
            assertEquals(0, countRows("SELECT COUNT(*) FROM medical_records WHERE consultation_id = ?", consultationId));
            assertEquals(0, countRows("SELECT COUNT(*) FROM invoices WHERE consultation_id = ?", consultationId));
        } finally {
            deleteAppointmentTree(appointmentId);
        }
    }
}
