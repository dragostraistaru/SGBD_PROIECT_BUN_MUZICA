package org.example.sgbd_proiect_bun_muzica.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Factory pentru crearea conexiunilor JDBC.
 * Foloseste DatabaseConfig pentru parametrii de conexiune.
 */
public class ConnectionFactory {

    private ConnectionFactory() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUsername(),
                DatabaseConfig.getPassword()
        );
    }
}