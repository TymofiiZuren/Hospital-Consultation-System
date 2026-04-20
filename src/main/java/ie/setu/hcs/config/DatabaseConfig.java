package ie.setu.hcs.config;

import ie.setu.hcs.exception.ConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {
    private static final Properties PROPERTIES = new Properties();
    private static final String[] PROPERTY_FILES = {"application.properties", "app.properties"};
    private static volatile boolean loaded;

    static {
        ensureLoaded();
    }

    private DatabaseConfig() {
    }

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }

        loadProperties();
        loadDriver();
        loaded = true;
    }

    private static void loadProperties() {
        for (String fileName : PROPERTY_FILES) {
            try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream(fileName)) {
                if (input == null) {
                    continue;
                }
                PROPERTIES.load(input);
                return;
            } catch (IOException ex) {
                throw new ConfigurationException("Failed to load database configuration from " + fileName + ".", ex);
            }
        }

        throw new ConfigurationException(
                "No database configuration file was found. Expected application.properties or app.properties on the classpath."
        );
    }

    private static void loadDriver() {
        String driver = trimToNull(PROPERTIES.getProperty("db.driver"));
        if (driver == null) {
            return;
        }

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            throw new ConfigurationException("Database driver could not be loaded: " + driver, ex);
        }
    }

    public static Connection getConnection() throws SQLException {
        ensureLoaded();

        String url = normalizeJdbcUrl(trimToNull(PROPERTIES.getProperty("db.url")));
        String user = trimToNull(PROPERTIES.getProperty("db.user"));
        String password = PROPERTIES.getProperty("db.password", "");

        if (url == null) {
            throw new SQLException("Database URL is missing in application.properties.");
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizeJdbcUrl(String url) {
        if (url == null) {
            return null;
        }

        int queryIndex = url.indexOf('?');
        String base = queryIndex >= 0 ? url.substring(0, queryIndex) : url;
        String suffix = queryIndex >= 0 ? url.substring(queryIndex) : "";

        if (base.endsWith("/") && base.startsWith("jdbc:mysql://")) {
            base = base.substring(0, base.length() - 1);
        }

        return base + suffix;
    }
}
