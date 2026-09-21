package com.skillswap.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Opens a connection from Java to MySQL using JDBC.
 *
 * JDBC (Java Database Connectivity) is a standard Java API for talking to
 * relational databases. You write normal Java method calls; underneath,
 * a "driver" (a jar file specific to MySQL) translates those calls into
 * the MySQL wire protocol. Swap the driver and, in theory, the same JDBC
 * code could talk to a different database.
 *
 * The pieces involved:
 *   - Driver:            the MySQL Connector/J jar. It registers itself
 *                         with DriverManager automatically when the class
 *                         com.mysql.cj.jdbc.Driver is loaded.
 *   - DriverManager:      a factory class that hands you a live Connection
 *                         when you give it a URL, username, and password.
 *   - Connection:         one open line to the database. Expensive to
 *                         create, so we open one per request and close it
 *                         when we're done (see try-with-resources in DAO).
 *   - PreparedStatement:  a precompiled SQL statement with placeholders
 *                         ("?") that we fill in safely — see UserDAO.
 *   - ResultSet:          the table of rows MySQL sends back after a query.
 */
public class DBConnection {

    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    // Static block: runs ONCE when this class is first loaded, before
    // any other code in this class runs. We use it here to read the
    // db.properties file a single time instead of on every request.
    static {
        Properties props = new Properties();

        // db.properties is NOT bundled in a .java file and NOT committed
        // with real credentials — see the setup note below. We load it
        // from the classpath, i.e. from WEB-INF/classes/db.properties
        // once the app is deployed to Tomcat.
        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException(
                        "db.properties not found on classpath. " +
                        "Place it in WEB-INF/classes/db.properties.");
            }
            props.load(input);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }

        DB_URL = props.getProperty("db.url");
        DB_USER = props.getProperty("db.user");
        DB_PASSWORD = props.getProperty("db.password");
    }

    // Private constructor: this class only has static members, so there's
    // no reason to ever create a "new DBConnection()". Making the
    // constructor private prevents that by mistake.
    private DBConnection() {
    }

    /**
     * Returns a brand-new open Connection. Callers are responsible for
     * closing it (always do this in a try-with-resources block).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
