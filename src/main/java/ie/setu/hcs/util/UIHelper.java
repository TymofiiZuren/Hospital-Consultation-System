package ie.setu.hcs.util;

import ie.setu.hcs.exception.ValidationException;

import javax.imageio.ImageIO;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelListener;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class UIHelper {
    private static final int FORM_CONTROL_WIDTH = 420;
    private static final int FORM_SECTION_MAX_WIDTH = 920;
    private static final int TABLE_SECTION_MAX_WIDTH = 1280;
    private static final int UI_FONT_SIZE_BOOST = 2;
    private static final String UI_FONT_FAMILY = resolveFontFamily(
            "SF Pro Display",
            "SF Pro Text",
            "SF Pro",
            "Helvetica Neue",
            "Helvetica",
            ".AppleSystemUIFont",
            "SansSerif"
    );

    static {
        installGlobalTypography();
    }

    private UIHelper() {}

    public static Font font(int style, int size) {
        return new Font(UI_FONT_FAMILY, style, Math.max(11, size + UI_FONT_SIZE_BOOST));
    }

    public static AnimatedContentPanel animatedContentHost() {
        AnimatedContentPanel panel = new AnimatedContentPanel();
        panel.setBorder(new EmptyBorder(0, 24, 24, 24));
        return panel;
    }

    private static void installGlobalTypography() {
        UIDefaults defaults = UIManager.getDefaults();
        Enumeration<Object> keys = defaults.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = defaults.get(key);
            if (value instanceof FontUIResource existing) {
                defaults.put(key, new FontUIResource(font(existing.getStyle(), existing.getSize())));
            }
        }
    }

    private static String resolveFontFamily(String... candidates) {
        Set<String> installedFamilies = new HashSet<>(
                Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
        );
        for (String candidate : candidates) {
            if (installedFamilies.contains(candidate)) {
                return candidate;
            }
        }
        return "SansSerif";
    }

    public static JPanel appBackground(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint background = new GradientPaint(
                        0, 0, HCS_Colors.LIGHT_BG,
                        getWidth(), getHeight(), HCS_Colors.LIGHT_BG_ALT
                );
                g2.setPaint(background);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(new Color(18, 176, 149, 22));
                g2.fillOval(-80, -30, 280, 280);
                g2.setColor(new Color(37, 99, 235, 18));
                g2.fillOval(getWidth() - 240, 80, 260, 260);
                g2.dispose();
            }
        };
        panel.setOpaque(true);
        return panel;
    }

    public static JButton actionButton(String text, Color color) {
        RoundedButton button = new RoundedButton(text, 22);
        button.setPreferredSize(new Dimension(126, 36));
        button.setFont(font(Font.BOLD, 13));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = actionButton(text, HCS_Colors.SURFACE);
        button.putClientProperty("buttonStyle", "ghost");
        button.setForeground(HCS_Colors.TEXT_DARK);
        return button;
    }

    public static JButton detailsButton(Component parent, JTable table, String title) {
        return detailsButton(parent, table, title, "View Details");
    }

    public static JButton detailsButton(Component parent, JTable table, String title, String label) {
        JButton button = actionButton("View Details", HCS_Colors.ACCENT_SKY);
        button.setText(label);
        button.addActionListener(e -> {
            try {
                showSelectedRowDetails(parent, table, title);
            } catch (Exception ex) {
                showError(parent, ex);
            }
        });
        return button;
    }

    public static JLabel roundedLogoLabel(String resourcePath, int size) {
        JLabel label = new JLabel(roundedLogoIcon(resourcePath, size));
        label.setPreferredSize(new Dimension(size, size));
        label.setMinimumSize(new Dimension(size, size));
        label.setMaximumSize(new Dimension(size, size));
        return label;
    }

    public static ImageIcon roundedLogoIcon(String resourcePath, int size) {
        int renderSize = Math.max(size * 4, size + 96);
        BufferedImage image = new BufferedImage(renderSize, renderSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        float outerInset = 0.5f;
        Ellipse2D.Float outerCircle = new Ellipse2D.Float(outerInset, outerInset, renderSize - 1f, renderSize - 1f);
        float haloInset = Math.max(2f, renderSize * 0.02f);
        Ellipse2D.Float haloCircle = new Ellipse2D.Float(haloInset, haloInset, renderSize - (haloInset * 2f), renderSize - (haloInset * 2f));
        float ringStroke = Math.max(2f, renderSize * 0.014f);
        float ringInset = Math.max(6f, renderSize * 0.055f);
        float ringDiameter = renderSize - (ringInset * 2f);
        Ellipse2D.Float ringCircle = new Ellipse2D.Float(ringInset, ringInset, ringDiameter, ringDiameter);
        float contentInset = ringInset + ringStroke + Math.max(4f, renderSize * 0.016f);
        float contentDiameter = renderSize - (contentInset * 2f);
        Ellipse2D.Float contentCircle = new Ellipse2D.Float(contentInset, contentInset, contentDiameter, contentDiameter);

        g2.setColor(new Color(255, 255, 255, 46));
        g2.fill(outerCircle);
        g2.setColor(new Color(15, 23, 42, 18));
        g2.fill(haloCircle);
        g2.setColor(new Color(255, 255, 255, 236));
        g2.fill(ringCircle);
        g2.setClip(contentCircle);

        URL resource = UIHelper.class.getResource(resourcePath);
        if (resource != null) {
            try {
                BufferedImage source = ImageIO.read(resource);
                if (source != null) {
                    BufferedImage cropped = cropLogoWhitespace(source);
                    int sourceWidth = cropped.getWidth();
                    int sourceHeight = cropped.getHeight();
                    double usableWidth = contentDiameter * 0.91;
                    double usableHeight = contentDiameter * 0.91;
                    double scale = Math.min(usableWidth / sourceWidth, usableHeight / sourceHeight);
                    int drawWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
                    int drawHeight = Math.max(1, (int) Math.round(sourceHeight * scale));
                    int drawX = (renderSize - drawWidth) / 2;
                    int drawY = (renderSize - drawHeight) / 2;
                    g2.drawImage(
                            cropped,
                            drawX, drawY, drawX + drawWidth, drawY + drawHeight,
                            0, 0, sourceWidth, sourceHeight,
                            null
                        );
                }
            } catch (Exception ignored) {
                Image source = new ImageIcon(resource).getImage();
                int usableWidth = Math.round(contentDiameter * 0.91f);
                int usableHeight = Math.round(contentDiameter * 0.91f);
                int drawX = (renderSize - usableWidth) / 2;
                int drawY = (renderSize - usableHeight) / 2;
                g2.drawImage(source, drawX, drawY, usableWidth, usableHeight, null);
            }
        }

        g2.setClip(null);
        g2.setColor(new Color(180, 196, 214, 210));
        g2.setStroke(new BasicStroke(ringStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(ringCircle);
        g2.dispose();

        return new ImageIcon(scaleImageHighQuality(image, size, size));
    }

    private static BufferedImage cropLogoWhitespace(BufferedImage source) {
        int minX = source.getWidth();
        int minY = source.getHeight();
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getRGB(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                int red = (argb >>> 16) & 0xFF;
                int green = (argb >>> 8) & 0xFF;
                int blue = argb & 0xFF;

                boolean transparent = alpha < 18;
                boolean nearWhite = red > 245 && green > 245 && blue > 245;
                if (!transparent && !nearWhite) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX < minX || maxY < minY) {
            return source;
        }

        int padX = Math.max(8, (maxX - minX) / 18);
        int padY = Math.max(8, (maxY - minY) / 18);
        int cropX = Math.max(0, minX - padX);
        int cropY = Math.max(0, minY - padY);
        int cropW = Math.min(source.getWidth() - cropX, (maxX - minX + 1) + (padX * 2));
        int cropH = Math.min(source.getHeight() - cropY, (maxY - minY + 1) + (padY * 2));

        BufferedImage cropped = new BufferedImage(cropW, cropH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = cropped.createGraphics();
        g2.drawImage(source, 0, 0, cropW, cropH, cropX, cropY, cropX + cropW, cropY + cropH, null);
        g2.dispose();
        return cropped;
    }

    private static BufferedImage scaleImageHighQuality(BufferedImage source, int targetWidth, int targetHeight) {
        BufferedImage current = source;
        int width = source.getWidth();
        int height = source.getHeight();

        while (width / 2 >= targetWidth && height / 2 >= targetHeight) {
            width = Math.max(targetWidth, width / 2);
            height = Math.max(targetHeight, height / 2);
            BufferedImage step = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = step.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2.drawImage(current, 0, 0, width, height, null);
            g2.dispose();
            current = step;
        }

        BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D out = output.createGraphics();
        out.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        out.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        out.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        out.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        out.drawImage(current, 0, 0, targetWidth, targetHeight, null);
        out.dispose();
        return output;
    }

    public static JButton sidebarButton(String text, Color color) {
        RoundedButton button = new RoundedButton(text, 22);
        button.setMaximumSize(new Dimension(236, 42));
        button.setPreferredSize(new Dimension(236, 42));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(font(Font.BOLD, 13));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        return button;
    }

    public static JButton navButton(String text, boolean active) {
        RoundedButton button = new RoundedButton(text, 22);
        button.putClientProperty("buttonStyle", "nav");
        button.putClientProperty("buttonSelected", active);
        button.setForeground(active ? HCS_Colors.NAV_TEXT_ACTIVE : Color.WHITE);
        button.setPreferredSize(new Dimension(132, 40));
        button.setFont(font(Font.BOLD, 13));
        button.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        return button;
    }

    public static void setNavActive(AbstractButton button, boolean active) {
        button.putClientProperty("buttonStyle", "nav");
        button.putClientProperty("buttonSelected", active);
        button.setForeground(active ? HCS_Colors.NAV_TEXT_ACTIVE : Color.WHITE);
        button.repaint();
    }

    public static JButton cardButton(String title, String desc) {
        JButton card = new RoundedButton("", 28);
        card.putClientProperty("buttonStyle", "card");
        card.setLayout(new BorderLayout(0, 10));
        card.setBackground(HCS_Colors.SURFACE);
        card.setBorder(new EmptyBorder(22, 22, 22, 22));
        card.setFocusPainted(false);
        card.setContentAreaFilled(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setHorizontalAlignment(SwingConstants.LEFT);
        card.setVerticalAlignment(SwingConstants.TOP);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel cardTitle = new JLabel(title);
        cardTitle.setFont(font(Font.BOLD, 18));
        cardTitle.setForeground(HCS_Colors.TEXT_DARK);
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel cardDesc = new JLabel("<html><div style='width:220px'>" + desc + "</div></html>");
        cardDesc.setFont(font(Font.PLAIN, 13));
        cardDesc.setForeground(HCS_Colors.TEXT_MUTED);
        cardDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(cardTitle);
        body.add(Box.createVerticalStrut(8));
        body.add(cardDesc);
        card.add(body, BorderLayout.CENTER);
        return card;
    }

    public static JPanel metricCard(String value, String title, String detail, Color accent) {
        JPanel card = roundedPanel(new BorderLayout(0, 16), HCS_Colors.SURFACE);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel accentBar = new JPanel();
        accentBar.setPreferredSize(new Dimension(52, 8));
        accentBar.setBackground(accent);
        accentBar.setOpaque(true);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(font(Font.BOLD, 28));
        valueLabel.setForeground(HCS_Colors.TEXT_DARK);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(font(Font.BOLD, 13));
        titleLabel.setForeground(HCS_Colors.TEXT_MUTED);

        JLabel detailLabel = new JLabel("<html><div style='width:180px'>" + detail + "</div></html>");
        detailLabel.setFont(font(Font.PLAIN, 12));
        detailLabel.setForeground(HCS_Colors.TEXT_SOFT);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(valueLabel);
        text.add(Box.createVerticalStrut(8));
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(6));
        text.add(detailLabel);

        card.add(accentBar, BorderLayout.NORTH);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    public static JPanel spotlightCard(String eyebrow, String title, String description, Color tint) {
        JPanel card = roundedPanel(new BorderLayout(0, 14), tint);
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel eyebrowLabel = new JLabel(eyebrow.toUpperCase());
        eyebrowLabel.setFont(font(Font.BOLD, 11));
        eyebrowLabel.setForeground(HCS_Colors.PRIMARY_TEAL_DARK);

        JLabel titleLabel = new JLabel("<html><div style='width:300px'>" + title + "</div></html>");
        titleLabel.setFont(font(Font.BOLD, 20));
        titleLabel.setForeground(HCS_Colors.TEXT_DARK);

        JLabel descLabel = new JLabel("<html><div style='width:320px'>" + description + "</div></html>");
        descLabel.setFont(font(Font.PLAIN, 13));
        descLabel.setForeground(HCS_Colors.TEXT_MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(eyebrowLabel);
        text.add(Box.createVerticalStrut(10));
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(10));
        text.add(descLabel);

        card.add(text, BorderLayout.CENTER);
        return card;
    }

    public static JPanel pill(String text, Color background, Color foreground) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);

        RoundedPanel pill = new RoundedPanel(new FlowLayout(FlowLayout.CENTER, 12, 8), background, background, 24, false);
        JLabel label = new JLabel(text);
        label.setFont(font(Font.BOLD, 11));
        label.setForeground(foreground);
        pill.add(label);
        wrapper.add(pill);
        return wrapper;
    }

    public static JScrollPane scrollablePage(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(new Color(0, 0, 0, 0));
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    public static void styleField(JTextField field) {
        field.setPreferredSize(new Dimension(FORM_CONTROL_WIDTH, 42));
        field.setFont(font(Font.PLAIN, 13));
        field.setForeground(HCS_Colors.TEXT_DARK);
        field.setCaretColor(HCS_Colors.PRIMARY_TEAL_DARK);
        field.setBackground(HCS_Colors.SURFACE);
        field.setBorder(new CompoundBorder(
                new RoundedBorder(HCS_Colors.BORDER_COLOR, 18, 1),
                new EmptyBorder(9, 14, 9, 14)
        ));
    }

    public static void stylePassword(JPasswordField field) {
        styleField(field);
        field.setFont(font(Font.PLAIN, 9));
    }

    public static void styleTextArea(JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(font(Font.PLAIN, 13));
        area.setForeground(HCS_Colors.TEXT_DARK);
        area.setBackground(HCS_Colors.SURFACE);
        area.setBorder(new EmptyBorder(10, 12, 10, 12));
        area.setCaretColor(HCS_Colors.PRIMARY_TEAL_DARK);
    }

    public static void styleCombo(JComboBox<?> comboBox) {
        comboBox.setPreferredSize(new Dimension(FORM_CONTROL_WIDTH, 42));
        comboBox.setFont(font(Font.PLAIN, 13));
        comboBox.setBackground(HCS_Colors.SURFACE);
        comboBox.setForeground(HCS_Colors.TEXT_DARK);
        comboBox.setBorder(new CompoundBorder(
                new RoundedBorder(HCS_Colors.BORDER_COLOR, 18, 1),
                new EmptyBorder(0, 10, 0, 10)
        ));
        comboBox.setFocusable(true);
        comboBox.setOpaque(false);
        comboBox.setUI(new ModernComboBoxUI());
        comboBox.setRenderer(new ModernComboBoxRenderer());
    }

    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.BOLD, 12));
        label.setForeground(HCS_Colors.LABEL_COLOR);
        return label;
    }

    public static JPanel formPanel() {
        JPanel panel = roundedPanel(new GridBagLayout(), HCS_Colors.SURFACE);
        panel.setBorder(new EmptyBorder(22, 26, 22, 26));
        panel.setMaximumSize(new Dimension(FORM_SECTION_MAX_WIDTH, Integer.MAX_VALUE));
        return panel;
    }

    public static JPanel roundedPanel(LayoutManager layout, Color color) {
        return new RoundedPanel(layout, color, HCS_Colors.BORDER_COLOR, 28);
    }

    public static JPanel pageBody(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(26, 26, 26, 26));
        return panel;
    }

    public static JPanel actionBar() {
        JPanel panel = roundedPanel(new FlowLayout(FlowLayout.LEFT, 10, 10), HCS_Colors.SURFACE);
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));
        return panel;
    }

    public static JPanel tableSearchBar(JTable table, String labelText) {
        JPanel panel = roundedPanel(new BorderLayout(12, 0), HCS_Colors.SURFACE);
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel label = new JLabel(labelText);
        label.setFont(font(Font.BOLD, 12));
        label.setForeground(HCS_Colors.LABEL_COLOR);

        JTextField searchField = new JTextField();
        styleField(searchField);
        searchField.setPreferredSize(new Dimension(Math.min(420, TABLE_SECTION_MAX_WIDTH), 40));
        installTableSearch(table, searchField);

        panel.add(label, BorderLayout.WEST);
        panel.add(searchField, BorderLayout.CENTER);
        return panel;
    }

    public static void installTableSearch(JTable table, JTextField searchField) {
        if (table == null || searchField == null) {
            return;
        }

        Runnable updater = () -> {
            RowSorter<? extends TableModel> sorter = table.getRowSorter();
            if (!(sorter instanceof TableRowSorter<?> tableSorter)) {
                return;
            }

            String query = searchField.getText();
            if (query == null || query.isBlank()) {
                tableSorter.setRowFilter(null);
            } else {
                tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(query.trim())));
            }
        };

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updater.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updater.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updater.run();
            }
        });
    }

    public static JPanel navbarWrapper(JComponent component) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 24, 0, 24));
        wrapper.putClientProperty("trimEmbeddedBorder", Boolean.TRUE);
        wrapper.add(component, BorderLayout.CENTER);
        return wrapper;
    }

    public static JPanel compactSection(JComponent component) {
        return new WidthConstrainedPanel(component, FORM_SECTION_MAX_WIDTH, false);
    }

    public static JPanel tableAlignedSection(JComponent component) {
        return new WidthConstrainedPanel(component, TABLE_SECTION_MAX_WIDTH, false);
    }

    public static void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.insets = new Insets(8, 8, 8, 14);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(label(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }

    public static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    component.setBackground(HCS_Colors.TABLE_SELECTION_BG);
                    component.setForeground(HCS_Colors.TABLE_SELECTION_FG);
                } else {
                    component.setBackground(row % 2 == 0 ? HCS_Colors.SURFACE : HCS_Colors.TABLE_ROW_ALT);
                    component.setForeground(HCS_Colors.TEXT_DARK);
                }
                return component;
            }
        };
        table.setRowHeight(36);
        table.setFont(font(Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);
        table.setOpaque(true);
        table.setBackground(HCS_Colors.SURFACE);
        table.setGridColor(HCS_Colors.BORDER_COLOR);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(HCS_Colors.TABLE_SELECTION_BG);
        table.setSelectionForeground(HCS_Colors.TABLE_SELECTION_FG);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setDefaultRenderer(Object.class, new ModernTableCellRenderer());
        table.addPropertyChangeListener("model", e -> SwingUtilities.invokeLater(() -> {
            installSingleColumnSorter(table);
            configureColumnWidths(table);
        }));
        registerSortHeaderRefresh(table);
        table.setTableHeader(new SortableTableHeader(table));

        JTableHeader header = table.getTableHeader();
        header.setFont(font(Font.BOLD, 12));
        header.setBackground(HCS_Colors.TABLE_HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new ModernTableHeaderRenderer());
        installSingleColumnSorter(table);
        configureColumnWidths(table);
        return table;
    }

    private static void registerSortHeaderRefresh(JTable table) {
        table.addPropertyChangeListener("rowSorter", e -> attachSortHeaderRefresh(table));
        attachSortHeaderRefresh(table);
    }

    private static void attachSortHeaderRefresh(JTable table) {
        RowSorter<? extends TableModel> sorter = table.getRowSorter();
        if (sorter == null) {
            return;
        }

        Object attachedSorter = table.getClientProperty("sortHeaderRefreshSorter");
        if (attachedSorter == sorter) {
            return;
        }

        sorter.addRowSorterListener(e -> {
            table.getTableHeader().repaint();
            table.revalidate();
            table.repaint();
            Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, table);
            if (scrollPane != null) {
                scrollPane.revalidate();
                scrollPane.repaint();
            }
        });
        table.putClientProperty("sortHeaderRefreshSorter", sorter);
    }

    private static void installSingleColumnSorter(JTable table) {
        TableModel model = table.getModel();
        if (model == null) {
            return;
        }

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model) {
            @Override
            public void toggleSortOrder(int column) {
                SortOrder nextOrder = SortOrder.ASCENDING;
                List<? extends SortKey> currentKeys = getSortKeys();
                if (!currentKeys.isEmpty()) {
                    SortKey current = currentKeys.get(0);
                    if (current.getColumn() == column && current.getSortOrder() == SortOrder.ASCENDING) {
                        nextOrder = SortOrder.DESCENDING;
                    } else if (current.getColumn() == column && current.getSortOrder() == SortOrder.DESCENDING) {
                        nextOrder = SortOrder.ASCENDING;
                    }
                }
                setSortKeys(List.of(new SortKey(column, nextOrder)));
            }
        };
        sorter.setMaxSortKeys(1);
        table.setRowSorter(sorter);
    }

    private static final class SortableTableHeader extends JTableHeader {
        private final JTable table;
        private final MouseAdapter sortHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (!SwingUtilities.isLeftMouseButton(event)) {
                    return;
                }
                int viewColumn = columnAtPoint(event.getPoint());
                if (viewColumn >= 0) {
                    toggleSort(viewColumn);
                    event.consume();
                }
            }
        };

        private SortableTableHeader(JTable table) {
            super(table.getColumnModel());
            this.table = table;
            installSingleClickHandler();
        }

        @Override
        public void updateUI() {
            super.updateUI();
            installSingleClickHandler();
        }

        private void toggleSort(int viewColumn) {
            RowSorter<? extends TableModel> sorter = table.getRowSorter();
            if (sorter == null) {
                return;
            }

            int modelColumn = table.convertColumnIndexToModel(viewColumn);
            SortOrder nextOrder = SortOrder.ASCENDING;
            List<? extends RowSorter.SortKey> sortKeys = sorter.getSortKeys();
            if (!sortKeys.isEmpty()) {
                RowSorter.SortKey current = sortKeys.get(0);
                if (current.getColumn() == modelColumn && current.getSortOrder() == SortOrder.ASCENDING) {
                    nextOrder = SortOrder.DESCENDING;
                } else if (current.getColumn() == modelColumn && current.getSortOrder() == SortOrder.DESCENDING) {
                    nextOrder = SortOrder.ASCENDING;
                }
            }

            sorter.setSortKeys(List.of(new RowSorter.SortKey(modelColumn, nextOrder)));
            repaint();
        }

        private void installSingleClickHandler() {
            for (var listener : getMouseListeners()) {
                if (listener != sortHandler) {
                    removeMouseListener(listener);
                }
            }
            for (var listener : getMouseMotionListeners()) {
                removeMouseMotionListener(listener);
            }
            addMouseListener(sortHandler);
        }
    }

    public static JComponent tableScrollPane(JTable table) {
        return tableScrollPane(table, -1);
    }

    public static JComponent tableScrollPane(JTable table, int preferredHeight) {
        JScrollPane scrollPane = new RoundedScrollPane(table, 26, HCS_Colors.SURFACE, HCS_Colors.BORDER_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        if (preferredHeight <= 0) {
            bindAutoTableHeight(table, scrollPane);
        } else {
            scrollPane.setPreferredSize(new Dimension(TABLE_SECTION_MAX_WIDTH, preferredHeight));
        }
        scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> stretchColumnsToViewport(table));
            }
        });
        return new WidthConstrainedPanel(scrollPane, TABLE_SECTION_MAX_WIDTH, true, preferredHeight);
    }

    public static JScrollPane textAreaScrollPane(JTextArea textArea) {
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(FORM_CONTROL_WIDTH, 92));
        scrollPane.setOpaque(true);
        scrollPane.setBackground(HCS_Colors.SURFACE);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(HCS_Colors.SURFACE);
        scrollPane.setBorder(BorderFactory.createLineBorder(HCS_Colors.BORDER_COLOR));
        scrollPane.setViewportBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        return scrollPane;
    }

    private static void bindAutoTableHeight(JTable table, JScrollPane scrollPane) {
        Runnable updateHeight = () -> SwingUtilities.invokeLater(() -> {
            scrollPane.setPreferredSize(new Dimension(TABLE_SECTION_MAX_WIDTH, computeAutoTableHeight(table)));
            scrollPane.revalidate();
        });

        TableModelListener listener = e -> updateHeight.run();
        table.getModel().addTableModelListener(listener);
        table.addPropertyChangeListener("model", e -> {
            if (e.getOldValue() instanceof TableModel oldModel) {
                oldModel.removeTableModelListener(listener);
            }
            if (e.getNewValue() instanceof TableModel newModel) {
                newModel.addTableModelListener(listener);
            }
            updateHeight.run();
        });
        updateHeight.run();
    }

    private static int computeAutoTableHeight(JTable table) {
        int headerHeight = table.getTableHeader() == null ? 44 : table.getTableHeader().getPreferredSize().height;
        int rows = Math.max(1, table.getRowCount());
        int visibleRows = Math.min(rows, 4);
        int borderPadding = 4;
        return headerHeight + (visibleRows * table.getRowHeight()) + borderPadding;
    }

    private static final class RoundedScrollPane extends JScrollPane {
        private final int radius;
        private final Color fillColor;
        private final Color borderColor;

        private RoundedScrollPane(Component view, int radius, Color fillColor, Color borderColor) {
            super(view);
            this.radius = radius;
            this.fillColor = fillColor;
            this.borderColor = borderColor;
            setOpaque(false);
            setBackground(fillColor);
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            getViewport().setOpaque(true);
            getViewport().setBackground(fillColor);
            getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
            getHorizontalScrollBar().setOpaque(false);
            getVerticalScrollBar().setOpaque(false);
            setCorner(LOWER_LEFT_CORNER, transparentCorner());
            setCorner(LOWER_RIGHT_CORNER, transparentCorner());
            setCorner(UPPER_LEFT_CORNER, transparentCorner());
            setCorner(UPPER_RIGHT_CORNER, transparentCorner());
            if (getColumnHeader() != null) {
                getColumnHeader().setOpaque(false);
            }
        }

        @Override
        public void setColumnHeaderView(Component view) {
            super.setColumnHeaderView(view);
            if (getColumnHeader() != null) {
                getColumnHeader().setOpaque(false);
            }
        }

        @Override
        public void setViewportView(Component view) {
            super.setViewportView(view);
            getViewport().setOpaque(true);
            getViewport().setBackground(fillColor);
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape shape = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, radius, radius);
            g2.setColor(fillColor);
            g2.fill(shape);
            g2.setClip(shape);
            super.paint(g2);
            g2.dispose();

            Graphics2D borderG = (Graphics2D) g.create();
            borderG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            borderG.setColor(borderColor);
            borderG.draw(shape);
            borderG.dispose();
        }

        private JComponent transparentCorner() {
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            return panel;
        }
    }

    public static JPanel pageHeader(String title, String subtitle) {
        JPanel panel = new GradientHeaderPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(font(Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("<html><div style='width:480px'>" + subtitle + "</div></html>");
        subtitleLabel.setFont(font(Font.PLAIN, 14));
        subtitleLabel.setForeground(HCS_Colors.TEXT_LIGHT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dateLabel.setFont(font(Font.PLAIN, 12));
        dateLabel.setForeground(new Color(226, 232, 240, 210));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(subtitleLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(dateLabel);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    public static void showContent(JPanel host, JComponent component) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> showContent(host, component));
            return;
        }

        host.removeAll();
        host.add(component, BorderLayout.CENTER);
        host.revalidate();
        host.repaint();
    }

    public static void showError(Component parent, Exception ex) {
        JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showSelectedRowDetails(Component parent, JTable table, String title) throws Exception {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            throw new ValidationException("Please select a row first.");
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        showRowDetails(parent, table, modelRow, title);
    }

    public static JComponent embeddedView(JFrame frame) {
        return embeddedView(frame, false);
    }

    public static JComponent embeddedView(JFrame frame, boolean trimOuterPadding) {
        Container content = frame.getContentPane();
        frame.setContentPane(new JPanel());
        frame.dispose();
        hideStandaloneNavigation(content);
        if (trimOuterPadding) {
            trimOuterPadding(content);
        }

        if (content instanceof JComponent component) {
            return component;
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    private static void trimOuterPadding(Component component) {
        if (component instanceof JComponent jc && jc.getBorder() instanceof EmptyBorder) {
            jc.setBorder(new EmptyBorder(0, 0, 0, 0));
        }
        trimEmbeddedContentPadding(component);
    }

    private static void trimEmbeddedContentPadding(Component component) {
        if (component instanceof JComponent jc
                && Boolean.TRUE.equals(jc.getClientProperty("trimEmbeddedBorder"))
                && jc.getBorder() instanceof EmptyBorder) {
            jc.setBorder(new EmptyBorder(0, 0, 0, 0));
        }

        if (component instanceof AnimatedContentPanel panel) {
            panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                trimEmbeddedContentPadding(child);
            }
        }
    }

    private static void hideStandaloneNavigation(Component component) {
        if (component instanceof JButton button) {
            String text = button.getText();
            if ("Back".equalsIgnoreCase(text) || "Close".equalsIgnoreCase(text)) {
                button.setVisible(false);
            }
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                hideStandaloneNavigation(child);
            }
        }
    }

    public static Integer selectedId(JTable table, String columnName) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int column = table.getModel() instanceof DefaultTableModel
                ? ((DefaultTableModel) table.getModel()).findColumn(columnName)
                : -1;

        if (column < 0) {
            column = 0;
        }

        Object value = table.getModel().getValueAt(modelRow, column);
        return value == null ? null : Integer.parseInt(value.toString());
    }

    public static void hideColumns(JTable table, String... columnNames) {
        if (!(table.getModel() instanceof DefaultTableModel model) || columnNames == null) {
            return;
        }

        TableColumnModel columnModel = table.getColumnModel();
        for (String columnName : columnNames) {
            if (columnName == null || columnName.isBlank()) {
                continue;
            }

            int modelIndex = model.findColumn(columnName);
            if (modelIndex < 0) {
                continue;
            }

            int viewIndex = table.convertColumnIndexToView(modelIndex);
            if (viewIndex >= 0 && viewIndex < columnModel.getColumnCount()) {
                columnModel.removeColumn(columnModel.getColumn(viewIndex));
            }
        }

        configureColumnWidths(table);
    }

    private static void showRowDetails(Component parent, JTable table, int row, String title) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(760, 620);
        dialog.setMinimumSize(new Dimension(620, 480));
        dialog.setLocationRelativeTo(parent);

        JPanel root = appBackground(new BorderLayout(0, 18));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.add(pageHeader(title, "Detailed information for the selected row"), BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        TableModel model = table.getModel();
        int displayedFields = 0;

        for (int column = 0; column < table.getColumnCount(); column++) {
            int modelIndex = table.convertColumnIndexToModel(column);
            String columnName = model.getColumnName(modelIndex);
            if (!showInDetails(columnName)) {
                continue;
            }

            if (displayedFields > 0) {
                list.add(Box.createVerticalStrut(12));
            }
            list.add(detailRow(
                    prettyColumnName(columnName),
                    valueToDisplay(model.getValueAt(row, modelIndex))
            ));
            displayedFields++;
        }

        if (displayedFields == 0) {
            list.add(detailRow("Details", "No additional information available."));
        }

        JPanel body = pageBody(new BorderLayout());
        body.add(scrollablePage(list), BorderLayout.CENTER);

        JPanel footer = actionBar();
        JButton close = secondaryButton("Close");
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);

        root.add(body, BorderLayout.CENTER);
        root.add(tableAlignedSection(footer), BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private static JPanel detailRow(String labelText, String valueText) {
        JPanel card = roundedPanel(new BorderLayout(0, 10), HCS_Colors.SURFACE);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel label = new JLabel(labelText);
        label.setFont(font(Font.BOLD, 12));
        label.setForeground(HCS_Colors.TEXT_MUTED);

        JTextArea value = new JTextArea(valueText);
        value.setEditable(false);
        value.setLineWrap(true);
        value.setWrapStyleWord(true);
        value.setOpaque(false);
        value.setBorder(null);
        value.setFocusable(false);
        value.setFont(font(Font.PLAIN, 14));
        value.setForeground(HCS_Colors.TEXT_DARK);

        card.add(label, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private static String prettyColumnName(String name) {
        if (name == null || name.isBlank()) {
            return "Field";
        }

        String normalized = name.replace('_', ' ').trim();
        String[] parts = normalized.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase());
            }
        }
        return builder.toString();
    }

    private static String valueToDisplay(Object value) {
        if (value == null) {
            return "N/A";
        }
        String text = value.toString().trim();
        return text.isEmpty() ? "N/A" : text;
    }

    private static boolean showInDetails(String columnName) {
        if (columnName == null || columnName.isBlank()) {
            return false;
        }

        String normalized = columnName.trim().toLowerCase();
        return !normalized.endsWith("_id") && !normalized.equals("id");
    }

    private static void configureColumnWidths(JTable table) {
        if (table.getColumnCount() == 0) {
            return;
        }

        TableColumnModel columns = table.getColumnModel();
        FontMetrics cellMetrics = table.getFontMetrics(table.getFont());
        FontMetrics headerMetrics = table.getTableHeader().getFontMetrics(table.getTableHeader().getFont());
        int sampleRows = Math.min(table.getRowCount(), 50);
        int[] baseWidths = new int[table.getColumnCount()];

        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
            TableColumn column = columns.getColumn(columnIndex);
            Object headerValue = column.getHeaderValue();
            int preferred = headerMetrics.stringWidth(headerValue == null ? "" : headerValue.toString()) + 32;

            for (int row = 0; row < sampleRows; row++) {
                Object value = table.getValueAt(row, columnIndex);
                preferred = Math.max(preferred, cellMetrics.stringWidth(value == null ? "" : value.toString()) + 28);
            }

            int width = Math.max(88, Math.min(320, preferred));
            column.setMinWidth(Math.min(width, 100));
            column.setPreferredWidth(width);
            column.setWidth(width);
            baseWidths[columnIndex] = width;
        }

        table.putClientProperty("baseColumnWidths", baseWidths);
        stretchColumnsToViewport(table);
    }

    private static void stretchColumnsToViewport(JTable table) {
        Object widthsProperty = table.getClientProperty("baseColumnWidths");
        if (!(widthsProperty instanceof int[] baseWidths) || baseWidths.length == 0) {
            return;
        }

        Container parent = table.getParent();
        if (!(parent instanceof JViewport viewport)) {
            return;
        }

        TableColumnModel columns = table.getColumnModel();
        if (columns.getColumnCount() != baseWidths.length) {
            return;
        }

        int viewportWidth = viewport.getWidth();
        if (viewportWidth <= 0) {
            return;
        }

        int totalBaseWidth = 0;
        for (int width : baseWidths) {
            totalBaseWidth += width;
        }

        int targetWidth = Math.max(totalBaseWidth, viewportWidth);
        int usedWidth = 0;

        for (int columnIndex = 0; columnIndex < columns.getColumnCount(); columnIndex++) {
            TableColumn column = columns.getColumn(columnIndex);
            int width;

            if (columnIndex == columns.getColumnCount() - 1) {
                width = targetWidth - usedWidth;
            } else {
                width = Math.round((float) baseWidths[columnIndex] * targetWidth / totalBaseWidth);
            }

            width = Math.max(column.getMinWidth(), width);
            column.setPreferredWidth(width);
            column.setWidth(width);
            usedWidth += width;
        }

        Dimension preferred = table.getPreferredSize();
        table.setPreferredScrollableViewportSize(new Dimension(targetWidth, preferred.height));
        table.revalidate();
    }

    private static final class WidthConstrainedPanel extends JPanel {
        private final JComponent content;
        private final int maxWidth;
        private final boolean fillHeight;
        private final int preferredHeight;

        private WidthConstrainedPanel(JComponent content, int maxWidth, boolean fillHeight) {
            this(content, maxWidth, fillHeight, -1);
        }

        private WidthConstrainedPanel(JComponent content, int maxWidth, boolean fillHeight, int preferredHeight) {
            super(null);
            this.content = content;
            this.maxWidth = maxWidth;
            this.fillHeight = fillHeight;
            this.preferredHeight = preferredHeight;
            setOpaque(false);
            add(content);
        }

        @Override
        public void doLayout() {
            Insets insets = getInsets();
            int availableWidth = Math.max(0, getWidth() - insets.left - insets.right);
            int availableHeight = Math.max(0, getHeight() - insets.top - insets.bottom);
            int width = Math.min(maxWidth, availableWidth);
            int x = insets.left + Math.max(0, (availableWidth - width) / 2);

            Dimension preferred = content.getPreferredSize();
            int height = fillHeight ? availableHeight : Math.min(preferred.height, availableHeight);
            int y = insets.top;
            content.setBounds(x, y, width, Math.max(0, height));
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension preferred = content.getPreferredSize();
            Insets insets = getInsets();
            int height = preferredHeight > 0 ? preferredHeight : preferred.height;
            return new Dimension(
                    Math.min(maxWidth, preferred.width) + insets.left + insets.right,
                    height + insets.top + insets.bottom
            );
        }
    }

    private static final class GradientHeaderPanel extends JPanel {
        private GradientHeaderPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gradient = new GradientPaint(
                    0, 0, HCS_Colors.HEADER_GRADIENT_START,
                    getWidth(), getHeight(), HCS_Colors.HEADER_GRADIENT_END
            );
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), 34, 34);

            g2.setColor(new Color(255, 255, 255, 28));
            g2.fillOval(getWidth() - 160, -30, 220, 220);
            g2.setColor(new Color(110, 231, 217, 34));
            g2.fillOval(getWidth() - 220, 40, 180, 180);
            g2.setColor(new Color(255, 255, 255, 48));
            g2.drawRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), 34, 34);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final class ModernTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setBorder(new EmptyBorder(0, 16, 0, 16));
            setFont(table.getFont());
            return this;
        }
    }

    private static final class ModernTableHeaderRenderer extends DefaultTableCellRenderer {
        private static final Icon SORT_ASC_ICON = new SortArrowIcon(true);
        private static final Icon SORT_DESC_ICON = new SortArrowIcon(false);
        private static final Icon SORT_NEUTRAL_ICON = new SortNeutralIcon();

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table,
                    value == null ? "" : value.toString().replace('_', ' '),
                    isSelected,
                    hasFocus,
                    row,
                    column
            );
            label.setOpaque(true);
            label.setBackground(HCS_Colors.TABLE_HEADER_BG);
            label.setForeground(Color.WHITE);
            label.setFont(font(Font.BOLD, 12));
            label.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 1, 0, HCS_Colors.BORDER_COLOR),
                    new EmptyBorder(0, 16, 0, 16)
            ));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setHorizontalTextPosition(SwingConstants.LEFT);
            label.setIcon(sortIcon(table, column));
            label.setIconTextGap(label.getIcon() == null ? 0 : 8);
            return label;
        }

        private Icon sortIcon(JTable table, int viewColumn) {
            RowSorter<? extends TableModel> sorter = table.getRowSorter();
            if (sorter == null || sorter.getSortKeys().isEmpty()) {
                return SORT_NEUTRAL_ICON;
            }

            int modelColumn = table.convertColumnIndexToModel(viewColumn);
            for (RowSorter.SortKey sortKey : sorter.getSortKeys()) {
                if (sortKey.getColumn() != modelColumn) {
                    continue;
                }
                if (sortKey.getSortOrder() == SortOrder.ASCENDING) {
                    return SORT_ASC_ICON;
                }
                if (sortKey.getSortOrder() == SortOrder.DESCENDING) {
                    return SORT_DESC_ICON;
                }
                return SORT_NEUTRAL_ICON;
            }
            return SORT_NEUTRAL_ICON;
        }
    }

    private static final class SortArrowIcon implements Icon {
        private final boolean ascending;

        private SortArrowIcon(boolean ascending) {
            this.ascending = ascending;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 230));

            Path2D triangle = new Path2D.Float();
            if (ascending) {
                triangle.moveTo(x + 4, y + 8);
                triangle.lineTo(x + 8, y + 3);
                triangle.lineTo(x + 12, y + 8);
            } else {
                triangle.moveTo(x + 4, y + 4);
                triangle.lineTo(x + 8, y + 9);
                triangle.lineTo(x + 12, y + 4);
            }
            triangle.closePath();
            g2.fill(triangle);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 12;
        }
    }

    private static final class SortNeutralIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(255, 255, 255, 150));
            Path2D up = new Path2D.Float();
            up.moveTo(x + 4, y + 7);
            up.lineTo(x + 8, y + 3);
            up.lineTo(x + 12, y + 7);
            up.closePath();
            g2.fill(up);

            g2.setColor(new Color(255, 255, 255, 105));
            Path2D down = new Path2D.Float();
            down.moveTo(x + 4, y + 6);
            down.lineTo(x + 8, y + 10);
            down.lineTo(x + 12, y + 6);
            down.closePath();
            g2.fill(down);

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }
    }

    private static final class ModernComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            return new ComboArrowButton();
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(HCS_Colors.SURFACE);
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 18, 18);
            g2.dispose();
        }
    }

    private static final class ModernComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(8, 12, 8, 12));
            label.setFont(font(Font.PLAIN, 13));
            label.setForeground(HCS_Colors.TEXT_DARK);
            label.setBackground(isSelected ? HCS_Colors.PRIMARY_TEAL_SOFT : HCS_Colors.SURFACE);
            return label;
        }
    }

    private static final class ComboArrowButton extends JButton {
        private ComboArrowButton() {
            setPreferredSize(new Dimension(38, 42));
            setBorder(BorderFactory.createEmptyBorder());
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int height = getHeight();
            g2.setColor(HCS_Colors.BORDER_COLOR);
            g2.drawLine(0, 9, 0, Math.max(9, height - 10));

            Color chevron = getModel().isPressed()
                    ? HCS_Colors.PRIMARY_TEAL_DARK
                    : getModel().isRollover() ? HCS_Colors.TEXT_DARK : HCS_Colors.TEXT_MUTED;
            g2.setColor(chevron);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2 + 1;
            Path2D chevronPath = new Path2D.Float();
            chevronPath.moveTo(centerX - 5, centerY - 3);
            chevronPath.lineTo(centerX, centerY + 2);
            chevronPath.lineTo(centerX + 5, centerY - 3);
            g2.draw(chevronPath);
            g2.dispose();
        }
    }
}
