// defining package ie.setu.hcs.model
package ie.setu.hcs.model;

// importing stuff
import java.time.LocalDateTime;

// implementing Appointment class
public class Appointment {
    // defining attributes of the class
    private Integer appointmentId;
    private Integer patientId;
    private Integer doctorId;
    private LocalDateTime date;
    private String status;
    
    // creating empty Appointment constructor
    public Appointment() {}

    // creating Appointment constructor with defining arguments
    public Appointment(Integer patientId, Integer doctorId,
                       LocalDateTime date, String status) {
        // implementing appointmentId
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.status = status;
    }

    // creating Appointment constructor with defining arguments
    public Appointment(Integer appointmentId, Integer patientId, Integer doctorId,
                   LocalDateTime date, String status) {
        // implementing appointmentId
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.status = status;
    }

    // creating getter for appointmentId
    public Integer getAppointmentId() {
        return appointmentId;
    }

    // creating setter for appointment Id
    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    // creating getter for patientId
    public Integer getPatientId() {
        return patientId;
    }

    // creating setter for patientId
    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    // creating getter for doctorId
    public Integer getDoctorId() {
        return doctorId;
    }

    // creating setter for doctorId
    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    // creating getter for date
    public LocalDateTime getDate() {
        return date;
    }

    // creating setter for date
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    // creating getter for status
    public String getStatus() {
        return status;
    }

    // creating setter for status
    public void setStatus(String status) {
        this.status = status;
    }

    // implementing the toString method
    @Override
    public String toString() {
        return "\nAppointment Id: " + appointmentId +
                "\nPatient Id: " + patientId +
                "\nDoctor Id: " + doctorId +
                "\nAppointment date: " + date +
                "\nAppointment status: " + status;
    }
}
