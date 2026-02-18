package ie.setu.hcs.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConfig {
    private static final Properties properties = new Properties();

    public void loadProperties() {

        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("app.properties")
        ) {
            if (input == null) {
                throw new RuntimeException("Properties file not found!");
            }

            properties.load(input);

        } catch (NullPointerException err) {
            System.err.println("File is null " + err.getMessage());
        } catch (IOException err) {
            System.err.println("Input/Output problem " + err.getMessage());
        } catch (RuntimeException err) {
            System.err.println("Runtime error " + err.getMessage());
        } catch (Exception err) {
            System.err.println("Exception " + err.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");
        String driver = properties.getProperty("db.driver");

        return DriverManager.getConnection(url, user, password);
    }
}
