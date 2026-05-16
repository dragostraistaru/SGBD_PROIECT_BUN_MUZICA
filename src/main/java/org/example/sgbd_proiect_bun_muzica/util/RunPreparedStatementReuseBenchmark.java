package org.example.sgbd_proiect_bun_muzica.util;

import jakarta.persistence.EntityManagerFactory;

public class RunPreparedStatementReuseBenchmark {
    public static void main(String[] args) {
        try (EntityManagerFactory emf = JPAUtil.getEntityManagerFactory()) {
            new PreparedStatementReuseBenchmarkDemo(emf).run();
        }
    }
}

