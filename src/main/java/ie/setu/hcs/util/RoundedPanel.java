package ie.setu.hcs.util;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private final int radius;
    private final Color fillColor;
    private final Color borderColor;
    private final boolean elevated;

    public RoundedPanel(LayoutManager layout, Color fillColor, Color borderColor, int radius) {
        this(layout, fillColor, borderColor, radius, true);
    }

    public RoundedPanel(LayoutManager layout, Color fillColor, Color borderColor, int radius, boolean elevated) {
        super(layout);
        this.fillColor = fillColor;
        this.borderColor = borderColor;
        this.radius = radius;
        this.elevated = elevated;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (elevated) {
            g2.setColor(HCS_Colors.SHADOW);
            g2.fillRoundRect(4, 8, Math.max(0, getWidth() - 8), Math.max(0, getHeight() - 10), radius, radius);
        }

        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2.dispose();
    }
}
