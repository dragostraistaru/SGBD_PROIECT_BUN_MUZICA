package org.example.sgbd_proiect_bun_muzica.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionPoolBenchmark {

    private static final String URL      = "jdbc:postgresql://localhost:5432/musicdb";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "root";
    private static final int    COUNT    = 100;

    /** Test FARA pooling - creeaza conexiune noua de fiecare data */
    public static BenchmarkResult testWithoutPooling() throws Exception {
        long start = System.currentTimeMillis();

        for (int i = 0; i < COUNT; i++) {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            }
        }

        long total = System.currentTimeMillis() - start;
        return new BenchmarkResult(COUNT, total);
    }

    /** Test CU pooling - ia conexiunea din HikariCP pool */
    public static BenchmarkResult testWithPooling() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);

        try (HikariDataSource ds = new HikariDataSource(config)) {
            long start = System.currentTimeMillis();

            for (int i = 0; i < COUNT; i++) {
                try (Connection conn = ds.getConnection()) {
                }
            }

            long total = System.currentTimeMillis() - start;
            return new BenchmarkResult(COUNT, total);
        }
    }

    public static class BenchmarkResult {
        public final int    count;
        public final long   totalMs;
        public final double avgMs;

        public BenchmarkResult(int count, long totalMs) {
            this.count   = count;
            this.totalMs = totalMs;
            this.avgMs   = (double) totalMs / count;
        }

        @Override
        public String toString() {
            return String.format(
                    "Total: %d ms | Medie per conexiune: %.2f ms | Conexiuni: %d",
                    totalMs, avgMs, count
            );
        }
    }


    public static void runBenchmark() {
        try {
            System.out.println(" BENCHMARK CONNECTION POOLING ");
            System.out.println("Fara pooling: " + testWithoutPooling());
            System.out.println("Cu pooling:   " + testWithPooling());
        } catch (Exception e) {
            System.err.println("Eroare benchmark: " + e.getMessage());
        }
    }
}