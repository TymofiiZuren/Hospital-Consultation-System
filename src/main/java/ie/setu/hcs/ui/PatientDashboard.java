package ie.setu.hcs.ui;

import ie.setu.hcs.model.Account;
import ie.setu.hcs.service.NotificationService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.RoundedPanel;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientDashboard extends JFrame {
    private static final DateTimeFormatter NOTIFICATION_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final Account account;
    private final NotificationService notificationService = new NotificationService();
    private JPanel contentHost;

    private JButton btnHome;
    private JButton btnAppointments;
    private JButton btnResults;
    private JButton btnRecords;
    private JButton btnInvoices;
    private JButton btnInsurance;
    private JButton btnProfile;

    public PatientDashboard(Account account) {
        this.account = account;
        initUI();
        showHome();
    }

    private void initUI() {
        setTitle("Patient Dashboard");
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

        JLabel title = new JLabel("Patient Care Portal");
        title.setFont(UIHelper.font(Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Appointments, labs, records, billing, and insurance");
        subtitle.setFont(UIHelper.font(Font.PLAIN, 12));
        subtitle.setForeground(HCS_Colors.TEXT_LIGHT);

        brandText.add(title);
        brandText.add(Box.createVerticalStrut(4));
        brandText.add(subtitle);
        brand.add(logoLabel);
        brand.add(brandText);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JLabel userLabel = new JLabel(account.getFirstName());
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
        btnResults = UIHelper.navButton("Labs", false);
        btnRecords = UIHelper.navButton("Records", false);
        btnInvoices = UIHelper.navButton("Billing", false);
        btnInsurance = UIHelper.navButton("Insurance", false);
        btnProfile = UIHelper.navButton("Profile", false);

        btnHome.addActionListener(e -> showHome());
        btnAppointments.addActionListener(e -> showFrame(new AppointmentFrame(account, AppointmentFrame.Mode.PATIENT), btnAppointments));
        btnResults.addActionListener(e -> showFrame(new LabResultsFrame(account, LabResultsFrame.Mode.PATIENT), btnResults));
        btnRecords.addActionListener(e -> showFrame(new MedicalRecordsFrame(account, MedicalRecordsFrame.Mode.PATIENT), btnRecords));
        btnInvoices.addActionListener(e -> showFrame(new InvoiceFrame(account, InvoiceFrame.Mode.PATIENT), btnInvoices));
        btnInsurance.addActionListener(e -> showFrame(new InsuranceFrame(account, InsuranceFrame.Mode.PATIENT), btnInsurance));
        btnProfile.addActionListener(e -> showFrame(new ProfileFrame(account), btnProfile));

        navGrid.add(btnHome);
        navGrid.add(btnAppointments);
        navGrid.add(btnResults);
        navGrid.add(btnRecords);
        navGrid.add(btnInvoices);
        navGrid.add(btnInsurance);
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
                "Patient Overview",
                "Open your appointments, records, labs, billing, and insurance."
        ), BorderLayout.NORTH);

        JPanel body = UIHelper.pageBody(new BorderLayout(0, 24));

        JPanel metrics = new JPanel(new GridLayout(1, 3, 18, 18));
        metrics.setOpaque(false);
        metrics.add(UIHelper.metricCard("Visits", "Appointments", "See your visit status in one place.", HCS_Colors.PRIMARY_TEAL));
        metrics.add(UIHelper.metricCard("Records", "History", "Open medical records and lab results.", HCS_Colors.BUTTON_BLUE));
        metrics.add(UIHelper.metricCard("Billing", "Payments", "Check invoices and insurance.", HCS_Colors.ACCENT_AMBER));

        JPanel lower = new JPanel(new GridLayout(1, 2, 18, 18));
        lower.setOpaque(false);
        lower.add(UIHelper.spotlightCard(
                "Patient",
                "Everything is easy to find",
                "Use one simple dashboard for visits, records, billing, and insurance.",
                HCS_Colors.PRIMARY_TEAL_SOFT
        ));

        JPanel summary = UIHelper.roundedPanel(new BorderLayout(0, 14), HCS_Colors.SURFACE);
        summary.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel title = new JLabel("Quick Notes");
        title.setFont(UIHelper.font(Font.BOLD, 20));
        title.setForeground(HCS_Colors.TEXT_DARK);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(infoLine("Book a new appointment."));
        list.add(Box.createVerticalStrut(10));
        list.add(infoLine("Check records and lab results."));
        list.add(Box.createVerticalStrut(10));
        list.add(infoLine("Open billing and insurance anytime."));

        summary.add(title, BorderLayout.NORTH);
        summary.add(list, BorderLayout.CENTER);
        lower.add(summary);

        JPanel cards = new JPanel(new GridLayout(2, 2, 18, 18));
        cards.setOpaque(false);
        cards.add(card("Book Appointment", "Schedule a visit.", () -> showFrame(new AppointmentFrame(account, AppointmentFrame.Mode.PATIENT), btnAppointments)));
        cards.add(card("My Appointments", "View all appointments.", () -> showFrame(new AppointmentFrame(account, AppointmentFrame.Mode.PATIENT), btnAppointments)));
        cards.add(card("Medical Records", "Read your records.", () -> showFrame(new MedicalRecordsFrame(account, MedicalRecordsFrame.Mode.PATIENT), btnRecords)));
        cards.add(card("Invoices", "View invoices.", () -> showFrame(new InvoiceFrame(account, InvoiceFrame.Mode.PATIENT), btnInvoices)));

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
        SwingUtilities.invokeLater(this::showUnreadNotifications);
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
        UIHelper.setNavActive(btnResults, btnResults == activeButton);
        UIHelper.setNavActive(btnRecords, btnRecords == activeButton);
        UIHelper.setNavActive(btnInvoices, btnInvoices == activeButton);
        UIHelper.setNavActive(btnInsurance, btnInsurance == activeButton);
        UIHelper.setNavActive(btnProfile, btnProfile == activeButton);
    }

    private void showUnreadNotifications() {
        try {
            List<NotificationService.NotificationItem> unread = notificationService.consumeUnreadNotifications(account, 3);
            if (unread.isEmpty()) {
                return;
            }

            StringBuilder message = new StringBuilder("<html><body style='width: 360px;'>");
            message.append("<b>Recent appointment updates</b><br><br>");
            for (int i = 0; i < unread.size(); i++) {
                NotificationService.NotificationItem item = unread.get(i);
                message.append("<b>").append(escapeHtml(item.title())).append("</b><br>");
                message.append(escapeHtml(item.message()));
                if (item.createdAt() != null) {
                    message.append("<br><span style='color:#667085;'>")
                            .append(item.createdAt().format(NOTIFICATION_TIME))
                            .append("</span>");
                }
                if (i < unread.size() - 1) {
                    message.append("<br><br>");
                }
            }
            message.append("</body></html>");

            JOptionPane.showMessageDialog(this, new JLabel(message.toString()), "Recent Updates", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            UIHelper.showError(this, ex);
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
