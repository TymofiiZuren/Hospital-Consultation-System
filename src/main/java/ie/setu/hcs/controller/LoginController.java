// package ie.setu.hcs.controller
package ie.setu.hcs.controller;

import ie.setu.hcs.ui.LoginForm;
import ie.setu.hcs.ui.DoctorDashboard;
import ie.setu.hcs.ui.PatientDashboard;
import ie.setu.hcs.ui.AdminDashboard;
import ie.setu.hcs.ui.LabTechnicianDashboard;
import ie.setu.hcs.service.AuthenticationService;
import ie.setu.hcs.model.Account;

import javax.swing.*;

public class LoginController {

    private final LoginForm view;
    private final AuthenticationService authService;

    public LoginController(LoginForm view) {
        this.view = view;
        this.authService = new AuthenticationService();
    }

    public void handleLogin() throws Exception {

        String email = view.getEmail();
        String password = view.getPassword();

        if (email.isEmpty() || password.isEmpty()) {
            view.showError("Email and password are required.");
            return;
        }

        System.out.println("Login triggered");

        Account account = authService.authenticate(email, password);

        if (account == null) {
            view.showError("Invalid credentials.");
            return;
        }

        JFrame dashboard;
        if (Boolean.TRUE.equals(account.isAdmin()) || Integer.valueOf(4).equals(account.getRoleId())) {
            dashboard = new AdminDashboard(account);
        } else {
            dashboard = switch (account.getRoleId()) {
                case 1 -> new PatientDashboard(account);
                case 2 -> new DoctorDashboard(account);
                case 3 -> new LabTechnicianDashboard(account);
                default -> null;
            };
        }

        if (dashboard == null) {
            view.showError("Unknown role.");
            return;
        }

        view.navigateToDashboard(dashboard);
    }
}
