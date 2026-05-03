package org.example.sgbd_proiect_bun_muzica.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;

public class ConnectionLeakDemo {

    private static HikariDataSource createPool(int maxSize) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/musicdb");
        config.setUsername("postgres");
        config.setPassword("root");
        config.setMaximumPoolSize(maxSize);
        config.setConnectionTimeout(3000);
        return new HikariDataSource(config);
    }

    /** SCENARIUL 1 - conexiuni nedeschise, pool epuizat */
    public static void demonstrateLeak() {
        System.out.println("\nSCENARIUL 1: LEAK - conexiuni neînchise ");
        HikariDataSource ds = createPool(5);

        try {
            for (int i = 1; i <= 6; i++) {
                Connection conn = ds.getConnection();
                System.out.println("Conexiunea #" + i + " obtinuta - dar NEINCHISA!");
            }
        } catch (Exception e) {
            System.out.println("EROARE - Pool epuizat: " + e.getMessage());
            System.out.println("Pool-ul are doar 5 conexiuni si toate sunt blocate (leak)!");
        } finally {
            ds.close();
        }
    }

    /** SCENARIUL 2 - fix corect cu try-with-resources */
    public static void demonstrateFix() {
        System.out.println("\n=== SCENARIUL 2: FIX - try-with-resources ===");
        HikariDataSource ds = createPool(5);

        try {
            for (int i = 1; i <= 10; i++) {
                try (Connection conn = ds.getConnection()) {
                    System.out.println("Conexiunea #" + i + " obtinuta si inchisa corect!");
                }
            }
            System.out.println("Toate 10 conexiunile au functionat cu un pool de 5!");
        } catch (Exception e) {
            System.out.println("Eroare: " + e.getMessage());
        } finally {
            ds.close();
        }
    }

    public static void runDemo() {
        demonstrateLeak();
        demonstrateFix();
    }
}