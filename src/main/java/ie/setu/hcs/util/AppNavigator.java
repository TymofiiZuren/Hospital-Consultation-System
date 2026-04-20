package ie.setu.hcs.util;

import ie.setu.hcs.ui.MainForm;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class AppNavigator {
    private static final Map<JFrame, JFrame> EMBEDDED_HOSTS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private AppNavigator() {}

    public static void show(JFrame frame) {
        frame.setVisible(true);
        frame.toFront();
    }

    public static void registerEmbeddedHost(JFrame embedded, JFrame host) {
        if (embedded == null || host == null) {
            return;
        }
        EMBEDDED_HOSTS.put(embedded, host);
    }

    public static void replace(JFrame current, JFrame next) {
        if (next == null) {
            return;
        }

        JFrame embeddedHost = current == null ? null : EMBEDDED_HOSTS.get(current);
        if (embeddedHost instanceof MainForm reusableHost && next instanceof MainForm) {
            EMBEDDED_HOSTS.remove(current);
            next.dispose();
            reusableHost.returnToLanding();
            return;
        }

        JFrame actualCurrent = resolveCurrent(current);

        if (actualCurrent == null) {
            show(next);
            return;
        }

        next.setBounds(actualCurrent.getBounds());
        actualCurrent.dispose();
        show(next);
    }

    public static void backOrClose(JFrame current, JFrame next) {
        JFrame actualCurrent = resolveCurrent(current);

        if (next == null) {
            if (actualCurrent != null) {
                actualCurrent.dispose();
            }
            return;
        }
        replace(actualCurrent, next);
    }

    private static JFrame resolveCurrent(JFrame current) {
        if (current == null) {
            return null;
        }

        JFrame host = EMBEDDED_HOSTS.get(current);
        if (host != null && host.isDisplayable()) {
            return host;
        }

        return current;
    }
}
