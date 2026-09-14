package com.healthfirst.pims.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Loads connection settings from db.properties and supplies JDBC connections to DAO classes. */
public final class Database {
    private static final Properties PROPERTIES = new Properties();

    static {
        Path externalConfiguration = Path.of("db.properties");
        try (InputStream input = Files.isRegularFile(externalConfiguration)
                ? Files.newInputStream(externalConfiguration)
                : Database.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) throw new IllegalStateException("db.properties is missing from application resources.");
            PROPERTIES.load(input);
            Class.forName(PROPERTIES.getProperty("db.driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Unable to load database configuration: " + e.getMessage());
        }
    }

    private Database() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(PROPERTIES.getProperty("db.url"),
                PROPERTIES.getProperty("db.username"), PROPERTIES.getProperty("db.password"));
    }

    /** Used at start-up to provide a clear, actionable MySQL connection error. */
    public static void verifyConnection() throws SQLException {
        try (Connection connection = getConnection()) {
            if (!connection.isValid(3)) throw new SQLException("The database connection is not valid.");
        }
    }
}
