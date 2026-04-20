package ie.setu.hcs.ui;

import ie.setu.hcs.model.Account;
import ie.setu.hcs.service.ConsultationService;
import ie.setu.hcs.util.AppNavigator;
import ie.setu.hcs.util.HCS_Colors;
import ie.setu.hcs.util.RoundedPanel;
import ie.setu.hcs.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LabTechnicianDashboard extends JFrame {
    private final Account account;
    private JPanel contentHost;

    private JButton btnHome;
    private JButton btnLabs;
    private JButton btnConsultations;
    private JButton btnProfile;

    public LabTechnicianDashboard(Account account) {
        this.account = account;
        initUI();
        showHome();
    }

    private void initUI() {
        setTitle("Lab Technician Dashboard");
        setSize(1180, 760);
        setMinimumSize(new Dimension(980, 680));
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

        JLabel title = new JLabel("Laboratory Workspace");
        title.setFont(UIHelper.font(Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Lab results, consult lookup, and profile");
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

        JPanel navGrid = new JPanel(new GridLayout(1, 4, 10, 10));
        navGrid.setOpaque(false);
        navGrid.setBorder(new EmptyBorder(18, 0, 0, 0));

        btnHome = UIHelper.navButton("Overview", true);
        btnLabs = UIHelper.navButton("Lab Results", false);
        btnConsultations = UIHelper.navButton("Consults", false);
        btnProfile = UIHelper.navButton("Profile", false);

        btnHome.addActionListener(e -> showHome());
        btnLabs.addActionListener(e -> showFrame(new LabResultsFrame(account, LabResultsFrame.Mode.TECHNICIAN), btnLabs));
        btnConsultations.addActionListener(e -> showFrame(new DataTableFrame(
                "Consultations",
                "Completed consultation records for lab work",
                "consultation_id",
                () -> new ConsultationService().getAllConsultations()
        ).withBack(() -> new LabTechnicianDashboard(account)), btnConsultations));
        btnProfile.addActionListener(e -> showFrame(new ProfileFrame(account), btnProfile));

        navGrid.add(btnHome);
        navGrid.add(btnLabs);
        navGrid.add(btnConsultations);
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
                "Lab Overview",
                "Open consultations, upload results, and check your profile."
        ), BorderLayout.NORTH);

        JPanel body = UIHelper.pageBody(new BorderLayout(0, 24));

        JPanel metrics = new JPanel(new GridLayout(1, 3, 18, 18));
        metrics.setOpaque(false);
        metrics.add(UIHelper.metricCard("Results", "Uploads", "Save and review lab results.", HCS_Colors.PRIMARY_TEAL));
        metrics.add(UIHelper.metricCard("Consults", "Lookup", "Find the right consultation first.", HCS_Colors.BUTTON_BLUE));
        metrics.add(UIHelper.metricCard("Profile", "Account", "Open your details quickly.", HCS_Colors.ACCENT_AMBER));

        JPanel lower = new JPanel(new GridLayout(1, 2, 18, 18));
        lower.setOpaque(false);
        lower.add(UIHelper.spotlightCard(
                "Lab",
                "Focused on the main tasks",
                "Use this dashboard to find consultations and upload results quickly.",
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
        list.add(infoLine("Open consultations before uploading results."));
        list.add(Box.createVerticalStrut(10));
        list.add(infoLine("Move between lookup and upload in one place."));
        list.add(Box.createVerticalStrut(10));
        list.add(infoLine("Your profile stays easy to reach."));

        summary.add(title, BorderLayout.NORTH);
        summary.add(list, BorderLayout.CENTER);
        lower.add(summary);

        JPanel cards = new JPanel(new GridLayout(1, 3, 18, 18));
        cards.setOpaque(false);
        cards.add(card("Upload Results", "Add lab results.", () -> showFrame(new LabResultsFrame(account, LabResultsFrame.Mode.TECHNICIAN), btnLabs)));
        cards.add(card("Consultations", "Find consultation details.", () -> showFrame(new DataTableFrame(
                "Consultations",
                "Completed consultation records for lab work",
                "consultation_id",
                () -> new ConsultationService().getAllConsultations()
        ).withBack(() -> new LabTechnicianDashboard(account)), btnConsultations)));
        cards.add(card("Profile", "View your account.", () -> showFrame(new ProfileFrame(account), btnProfile)));

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
        UIHelper.setNavActive(btnLabs, btnLabs == activeButton);
        UIHelper.setNavActive(btnConsultations, btnConsultations == activeButton);
        UIHelper.setNavActive(btnProfile, btnProfile == activeButton);
    }
}
