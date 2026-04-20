package ie.setu.hcs.ui;

import ie.setu.hcs.model.Account;
import ie.setu.hcs.service.AppointmentService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.RoundedPanel;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DoctorDashboard extends JFrame {
    private final Account account;
    private JPanel contentHost;

    private JButton btnHome;
    private JButton btnAppointments;
    private JButton btnPatients;
    private JButton btnConsultations;
    private JButton btnLabs;
    private JButton btnRecords;
    private JButton btnProfile;

    public DoctorDashboard(Account account) {
        this.account = account;
        initUI();
        showHome();
    }

    private void initUI() {
        setTitle("Doctor Dashboard");
        setSize(1240, 780);
        setMinimumSize(new Dimension(1080, 720));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = UIHelper.appBackground(new BorderLayout(0, 18));
        root.setBorder(new EmptyBorder(20, 24, 24, 24));

        contentHost = UIHelper.animatedContentHost();
        root.add(createNavbar(), BorderLayout.NORTH);
        root.add(contentHost, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createNavbar() {
        RoundedPanel shell = new RoundedPanel(new BorderLayout(), HCS_Colors.DARK_SIDEBAR, HCS_Colors.SIDEBAR_BORDER, 34);
        shell.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        JPanel topRow = new JPanel(new BorderLayout(18, 0));
        topRow.setOpaque(false);

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        brand.setOpaque(false);

        JLabel logoLabel = UIHelper.roundedLogoLabel("/images/hospital_logo.png", 42);

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Clinical Workspace");
        title.setFont(UIHelper.font(Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Appointments, patients, labs, and records");
        subtitle.setFont(UIHelper.font(Font.PLAIN, 12));
        subtitle.setForeground(HCS_Colors.TEXT_LIGHT);

        brandText.add(title);
        brandText.add(Box.createVerticalStrut(4));
        brandText.add(subtitle);
        brand.add(logoLabel);
        brand.add(brandText);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JLabel userLabel = new JLabel("Dr. " + account.getLastName());
        userLabel.setFont(UIHelper.font(Font.PLAIN, 13));
        userLabel.setForeground(Color.WHITE);
        actions.add(userLabel);

        JButton logout = UIHelper.secondaryButton("Logout");
        logout.addActionListener(e -> AppNavigator.replace(this, new MainForm()));
        actions.add(logout);

        topRow.add(brand, BorderLayout.WEST);
        topRow.add(actions, BorderLayout.EAST);

        JPanel navGrid = new JPanel(new GridLayout(1, 7, 10, 10));
        navGrid.setOpaque(false);
        navGrid.setBorder(new EmptyBorder(18, 0, 0, 0));

        btnHome = UIHelper.navButton("Overview", true);
        btnAppointments = UIHelper.navButton("Appointments", false);
        btnPatients = UIHelper.navButton("Patients", false);
        btnConsultations = UIHelper.navButton("Consults", false);
        btnLabs = UIHelper.navButton("Labs", false);
        btnRecords = UIHelper.navButton("Records", false);
        btnProfile = UIHelper.navButton("Profile", false);

        btnHome.addActionListener(e -> showHome());
        btnAppointments.addActionListener(e -> showFrame(new AppointmentFrame(account, AppointmentFrame.Mode.DOCTOR), btnAppointments));
        btnPatients.addActionListener(e -> showFrame(new DataTableFrame(
                "My Patients",
                "Patients linked to your appointments",
                "patient_id",
                () -> new AppointmentService().getPatientsForDoctor(account)
        ).withBack(() -> new DoctorDashboard(account)), btnPatients));
        btnConsultations.addActionListener(e -> showFrame(new ConsultationFrame(account, ConsultationFrame.Mode.DOCTOR), btnConsultations));
        btnLabs.addActionListener(e -> showFrame(new LabResultsFrame(account, LabResultsFrame.Mode.DOCTOR), btnLabs));
        btnRecords.addActionListener(e -> showFrame(new MedicalRecordsFrame(account, MedicalRecordsFrame.Mode.DOCTOR), btnRecords));
        btnProfile.addActionListener(e -> showFrame(new ProfileFrame(account), btnProfile));

        navGrid.add(btnHome);
        navGrid.add(btnAppointments);
        navGrid.add(btnPatients);
        navGrid.add(btnConsultations);
        navGrid.add(btnLabs);
        navGrid.add(btnRecords);
        navGrid.add(btnProfile);

        stack.add(topRow);
        stack.add(navGrid);
        shell.add(stack, BorderLayout.CENTER);
        return UIHelper.navbarWrapper(shell);
    }

    private JComponent createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 24));
        content.setOpaque(false);
        content.add(UIHelper.pageHeader(
                "Doctor Overview",
                "Open your schedule, patients, consults, and records."
        ), BorderLayout.NORTH);

        JPanel body = UIHelper.pageBody(new BorderLayout(0, 24));

        JPanel metrics = new JPanel(new GridLayout(1, 3, 18, 18));
        metrics.setOpaque(false);
        metrics.add(UIHelper.metricCard("Today", "Appointments", "See and manage today’s visits.", HCS_Colors.PRIMARY_TEAL));
        metrics.add(UIHelper.metricCard("Patients", "People", "Open patients linked to your visits.", HCS_Colors.BUTTON_BLUE));
        metrics.add(UIHelper.metricCard("Notes", "Consults", "Save notes and follow-ups quickly.", HCS_Colors.ACCENT_AMBER));

        JPanel lower = new JPanel(new GridLayout(1, 2, 18, 18));
        lower.setOpaque(false);
        lower.add(UIHelper.spotlightCard(
                "Doctor",
                "Built for daily work",
                "Use one simple workspace for visits, consults, labs, and records.",
                HCS_Colors.PRIMARY_TEAL_SOFT
        ));

        JPanel guidance = UIHelper.roundedPanel(new BorderLayout(0, 14), HCS_Colors.SURFACE);
        guidance.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel title = new JLabel("Quick Notes");
        title.setFont(UIHelper.font(Font.BOLD, 20));
        title.setForeground(HCS_Colors.TEXT_DARK);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(infoLine("Open appointments to manage visits."));
        list.add(Box.createVerticalStrut(10));
        list.add(infoLine("Open consultations to save notes."));
        list.add(Box.createVerticalStrut(10));
        list.add(infoLine("Check lab results and records in the same place."));

        guidance.add(title, BorderLayout.NORTH);
        guidance.add(list, BorderLayout.CENTER);
        lower.add(guidance);

        JPanel cards = new JPanel(new GridLayout(0, 3, 18, 18));
        cards.setOpaque(false);
        cards.add(card("Today's Appointments", "View your visits.", () -> showFrame(new AppointmentFrame(account, AppointmentFrame.Mode.DOCTOR), btnAppointments)));
        cards.add(card("My Patients", "Open linked patients.", () -> showFrame(new DataTableFrame(
                "My Patients",
                "Patients linked to your appointments",
                "patient_id",
                () -> new AppointmentService().getPatientsForDoctor(account)
        ).withBack(() -> new DoctorDashboard(account)), btnPatients)));
        cards.add(card("Consultations", "Write consult notes.", () -> showFrame(new ConsultationFrame(account, ConsultationFrame.Mode.DOCTOR), btnConsultations)));
        cards.add(card("Lab Results", "Review test results.", () -> showFrame(new LabResultsFrame(account, LabResultsFrame.Mode.DOCTOR), btnLabs)));
        cards.add(card("Medical Records", "Open patient records.", () -> showFrame(new MedicalRecordsFrame(account, MedicalRecordsFrame.Mode.DOCTOR), btnRecords)));

        body.add(metrics, BorderLayout.NORTH);
        body.add(lower, BorderLayout.CENTER);
        body.add(cards, BorderLayout.SOUTH);
        content.add(UIHelper.scrollablePage(body), BorderLayout.CENTER);
        return content;
    }

    private JPanel infoLine(String text) {
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        line.setOpaque(false);

        JPanel accent = new JPanel();
        accent.setBackground(HCS_Colors.PRIMARY_TEAL);
        accent.setPreferredSize(new Dimension(10, 10));

        JLabel label = new JLabel(text);
        label.setFont(UIHelper.font(Font.PLAIN, 13));
        label.setForeground(HCS_Colors.TEXT_MUTED);

        line.add(accent);
        line.add(label);
        return line;
    }

    private JButton card(String title, String desc, Runnable action) {
        JButton card = UIHelper.cardButton(title, desc);
        card.addActionListener(e -> action.run());
        return card;
    }

    private void showHome() {
        setActiveButton(btnHome);
        showPanel(createContent());
    }

    private void showFrame(JFrame frame, JButton activeButton) {
        setActiveButton(activeButton);
        showPanel(UIHelper.embeddedView(frame));
    }

    private void showPanel(JComponent component) {
        UIHelper.showContent(contentHost, component);
    }

    private void setActiveButton(JButton activeButton) {
        UIHelper.setNavActive(btnHome, btnHome == activeButton);
        UIHelper.setNavActive(btnAppointments, btnAppointments == activeButton);
        UIHelper.setNavActive(btnPatients, btnPatients == activeButton);
        UIHelper.setNavActive(btnConsultations, btnConsultations == activeButton);
        UIHelper.setNavActive(btnLabs, btnLabs == activeButton);
        UIHelper.setNavActive(btnRecords, btnRecords == activeButton);
        UIHelper.setNavActive(btnProfile, btnProfile == activeButton);
    }
}
