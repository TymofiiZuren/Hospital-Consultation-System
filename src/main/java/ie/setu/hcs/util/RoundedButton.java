package ie.setu.hcs.util;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;

public class RoundedButton extends JButton {
    private final int radius;
    private boolean hovered;
    private boolean pressed;

    public RoundedButton(String text) {
        this(text, 8);
    }

    public RoundedButton(String text, int radius) {
        super(text);
        this.radius = radius;
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                pressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        String variant = String.valueOf(getClientProperty("buttonStyle"));
        boolean selected = Boolean.TRUE.equals(getClientProperty("buttonSelected"));

        Color fill = getBackground() == null ? HCS_Colors.BUTTON_BLUE : getBackground();
        Color border = blend(fill, Color.BLACK, 0.12f);
        Color textColor = getForeground() == null ? Color.WHITE : getForeground();
        boolean elevated = true;

        if ("nav".equals(variant)) {
            fill = selected ? HCS_Colors.NAV_PILL_ACTIVE : HCS_Colors.NAV_PILL;
            border = selected ? new Color(255, 255, 255, 102) : new Color(255, 255, 255, 44);
            textColor = selected ? HCS_Colors.NAV_TEXT_ACTIVE : Color.WHITE;
            elevated = false;
        } else if ("ghost".equals(variant)) {
            fill = hovered ? HCS_Colors.SURFACE_ALT : HCS_Colors.SURFACE;
            border = hovered ? HCS_Colors.BORDER_STRONG : HCS_Colors.BORDER_COLOR;
            textColor = HCS_Colors.TEXT_DARK;
            elevated = false;
        } else if ("card".equals(variant)) {
            fill = hovered ? HCS_Colors.SURFACE_ALT : HCS_Colors.SURFACE;
            border = hovered ? new Color(18, 176, 149, 102) : HCS_Colors.BORDER_COLOR;
            textColor = HCS_Colors.TEXT_DARK;
        } else if ("subtle".equals(variant)) {
            fill = hovered ? HCS_Colors.SURFACE_ALT : HCS_Colors.SURFACE;
            border = hovered ? HCS_Colors.BORDER_STRONG : HCS_Colors.BORDER_COLOR;
            textColor = HCS_Colors.PRIMARY_TEAL_DARK;
            elevated = false;
        }

        if (!isEnabled()) {
            fill = new Color(203, 213, 225);
            border = new Color(203, 213, 225);
            textColor = Color.WHITE;
        } else if ("nav".equals(variant)) {
            if (hovered) {
                fill = selected ? new Color(255, 255, 255, 232) : new Color(255, 255, 255, 56);
                border = selected ? new Color(255, 255, 255, 118) : new Color(255, 255, 255, 68);
            }
            if (pressed) {
                fill = selected ? new Color(255, 255, 255, 220) : new Color(255, 255, 255, 48);
            }
        } else {
            if (hovered) {
                fill = blend(fill, Color.BLACK, 0.04f);
                border = blend(border, Color.BLACK, 0.06f);
            }
            if (pressed) {
                fill = blend(fill, Color.BLACK, 0.10f);
                border = blend(border, Color.BLACK, 0.10f);
            }
        }

        int arc = Math.max(radius, 18);

        if (elevated) {
            int shadowAlpha = "card".equals(variant) ? 14 : 18;
            if (hovered) {
                shadowAlpha += 6;
            }
            g2.setColor(new Color(15, 23, 42, shadowAlpha));
            g2.fillRoundRect(1, 2, Math.max(0, getWidth() - 3), Math.max(0, getHeight() - 3), arc, arc);
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), arc, arc);

        g2.setColor(border);
        g2.drawRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), arc, arc);

        setForeground(textColor);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        // Border is rendered inside paintComponent to keep the elevated effect in sync.
    }

    private Color blend(Color base, Color overlay, float amount) {
        float keep = 1f - amount;
        return new Color(
                Math.min(255, Math.round(base.getRed() * keep + overlay.getRed() * amount)),
                Math.min(255, Math.round(base.getGreen() * keep + overlay.getGreen() * amount)),
                Math.min(255, Math.round(base.getBlue() * keep + overlay.getBlue() * amount)),
                base.getAlpha()
        );
    }
}
