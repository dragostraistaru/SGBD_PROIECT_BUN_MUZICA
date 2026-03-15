package org.example.sgbd_proiect_bun_muzica.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Citeste configuratia bazei de date din db.properties.
 */
public class DatabaseConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (input == null)
                throw new RuntimeException("db.properties nu a fost gasit!");
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Eroare la citirea db.properties", e);
        }
    }

    public static String getUrl()      { return props.getProperty("db.url"); }
    public static String getUsername() { return props.getProperty("db.username"); }
    public static String getPassword() { return props.getProperty("db.password"); }
}