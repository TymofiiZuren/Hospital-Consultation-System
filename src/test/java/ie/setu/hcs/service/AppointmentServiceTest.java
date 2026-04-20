package ie.setu.hcs.service;

import org.junit.jupiter.api.Test;

import javax.swing.table.DefaultTableModel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentServiceTest {

    private final AppointmentService service = new AppointmentService();

    @Test
    void pendingGroupingKeepsOnlyTodayAndFuturePendingAppointments() throws Exception {
        DefaultTableModel filtered = filter(sampleAppointments(), "PENDING");

        assertEquals(1, filtered.getRowCount());
        assertEquals("Future Pending", filtered.getValueAt(0, filtered.findColumn("doctor")));
    }

    @Test
    void acceptedGroupingKeepsAcceptedAppointmentsRegardlessOfDateAndSortsAscending() throws Exception {
        DefaultTableModel filtered = filter(sampleAppointments(), "ACCEPTED");

        assertEquals(2, filtered.getRowCount());
        assertEquals("Past Accepted", filtered.getValueAt(0, filtered.findColumn("doctor")));
        assertEquals("Future Accepted", filtered.getValueAt(1, filtered.findColumn("doctor")));
    }

    @Test
    void cancelledGroupingIncludesCancelledAndRejectedAppointments() throws Exception {
        DefaultTableModel filtered = filter(sampleAppointments(), "CANCELLED");

        assertEquals(2, filtered.getRowCount());
        assertEquals("Cancelled", filtered.getValueAt(0, filtered.findColumn("status")));
        assertEquals("Rejected", filtered.getValueAt(1, filtered.findColumn("status")));
    }

    @Test
    void pastGroupingIncludesCompletedAndPastNonActiveAppointmentsInDescendingOrder() throws Exception {
        DefaultTableModel filtered = filter(sampleAppointments(), "PAST");

        assertEquals(2, filtered.getRowCount());
        assertEquals("Past Pending", filtered.getValueAt(0, filtered.findColumn("doctor")));
        assertEquals("Completed Visit", filtered.getValueAt(1, filtered.findColumn("doctor")));
    }

    private DefaultTableModel sampleAppointments() {
        DefaultTableModel model = new DefaultTableModel(new Object[]{
                "doctor", "appointment_datetime", "consultation_room", "status", "medical_need"
        }, 0);

        model.addRow(new Object[]{"Future Pending", LocalDate.now().plusDays(1).atTime(9, 0), "", "Pending", "Review"});
        model.addRow(new Object[]{"Past Pending", LocalDate.now().minusDays(1).atTime(9, 0), "", "Pending", "Check"});
        model.addRow(new Object[]{"Past Accepted", LocalDate.now().minusDays(2).atTime(11, 0), "", "Accepted", "Follow-up"});
        model.addRow(new Object[]{"Future Accepted", LocalDate.now().plusDays(2).atTime(11, 0), "", "Accepted", "Review"});
        model.addRow(new Object[]{"Cancelled Visit", LocalDate.now().plusDays(3).atTime(10, 0), "", "Cancelled", "Cancelled need"});
        model.addRow(new Object[]{"Rejected Visit", LocalDate.now().plusDays(4).atTime(10, 0), "", "Rejected", "Rejected need"});
        model.addRow(new Object[]{"Completed Visit", LocalDate.now().minusDays(3).atTime(8, 30), "", "Completed", "Complete"});

        return model;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private DefaultTableModel filter(DefaultTableModel source, String groupName) throws Exception {
        Class<?> enumClass = Class.forName("ie.setu.hcs.service.AppointmentService$PatientAppointmentGroup");
        Object enumValue = Enum.valueOf((Class<? extends Enum>) enumClass.asSubclass(Enum.class), groupName);
        Method method = AppointmentService.class.getDeclaredMethod("filterPatientAppointments", DefaultTableModel.class, enumClass);
        method.setAccessible(true);
        return (DefaultTableModel) method.invoke(service, source, enumValue);
    }
}
