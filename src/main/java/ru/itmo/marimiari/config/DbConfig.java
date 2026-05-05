package ru.itmo.marimiari.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConfig {
    private static final String PROPERTIES_FILE = "db.properties";
    private static Properties props = new Properties();

    static {
        try (InputStream input = DbConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)){
            if (input == null){
                throw new RuntimeException("db.propertiesnot found");
            }
            props.load(input);
            Class.forName("org.postgresql.Driver");
        } catch (Exception e){
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
        );
    }
}
