package ie.setu.hcs.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AnimatedContentPanel extends JPanel {
    private static final int FRAME_DELAY_MS = 14;
    private static final int ANIMATION_DURATION_MS = 240;

    private BufferedImage previousSnapshot;
    private BufferedImage nextSnapshot;
    private JComponent pendingComponent;
    private Timer timer;
    private float progress = 1f;
    private boolean animating;

    public AnimatedContentPanel() {
        super(new BorderLayout());
        setOpaque(false);
    }

    public void showContent(JComponent component) {
        if (component == null) {
            return;
        }

        if (getWidth() <= 0 || getHeight() <= 0 || getComponentCount() == 0) {
            swapImmediately(component);
            return;
        }

        Component current = getComponent(0);
        if (!(current instanceof JComponent currentComponent)) {
            swapImmediately(component);
            return;
        }

        BufferedImage oldSnapshot = snapshot(currentComponent);
        BufferedImage newSnapshot = snapshot(component);
        if (oldSnapshot == null || newSnapshot == null) {
            swapImmediately(component);
            return;
        }

        previousSnapshot = oldSnapshot;
        nextSnapshot = newSnapshot;
        pendingComponent = component;
        progress = 0f;
        animating = true;

        removeAll();
        revalidate();
        repaint();

        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        final long startedAt = System.currentTimeMillis();
        timer = new Timer(FRAME_DELAY_MS, e -> {
            long elapsed = System.currentTimeMillis() - startedAt;
            progress = Math.min(1f, elapsed / (float) ANIMATION_DURATION_MS);
            repaint();

            if (progress >= 1f) {
                timer.stop();
                animating = false;
                previousSnapshot = null;
                nextSnapshot = null;
                swapImmediately(pendingComponent);
                pendingComponent = null;
            }
        });
        timer.start();
    }

    private void swapImmediately(JComponent component) {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        animating = false;
        previousSnapshot = null;
        nextSnapshot = null;
        pendingComponent = null;

        removeAll();
        add(component, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private BufferedImage snapshot(JComponent component) {
        int width = Math.max(1, getWidth());
        int height = Math.max(1, getHeight());

        component.setSize(width, height);
        component.doLayout();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 0));
        g2.fillRect(0, 0, width, height);
        component.printAll(g2);
        g2.dispose();
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!animating || previousSnapshot == null || nextSnapshot == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int width = getWidth();
        int offset = Math.round(width * 0.09f * (1f - progress));

        Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - (progress * 0.35f)));
        g2.drawImage(previousSnapshot, -offset, 0, null);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, 0.2f + (progress * 0.8f))));
        g2.drawImage(nextSnapshot, Math.round(width * 0.09f) - offset, 0, null);
        g2.setComposite(originalComposite);
        g2.dispose();
    }
}
