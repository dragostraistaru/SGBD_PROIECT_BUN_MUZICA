package org.example.sgbd_proiect_bun_muzica.util;

import jakarta.persistence.EntityManagerFactory;

public class RunBenchmark {
    public static void main(String[] args) {
        EntityManagerFactory emf = JPAUtil.getEntityManagerFactory();

        // --- CERINTA 1: N+1 QUERY ---
//         NPlusOneDemo nPlusOneDemo = new NPlusOneDemo(emf);
//         nPlusOneDemo.run();

        // --- CERINTA 2A: BENCHMARK FARA INDEX-URI ---
//         IndexBenchmarkDemo indexDemo = new IndexBenchmarkDemo(emf);
//         indexDemo.runTaskA();

        // --- CERINTA 2B: ADAUGARE INDEX-URI ---
         //indexDemo.runTaskB();

        // --- CERINTA 2C: BENCHMARK CU INDEX-URI ---
         //indexDemo.runTaskC();

        // --- CERINTA 3: PAGINARE ---
        // PaginationBenchmarkDemo paginationDemo = new PaginationBenchmarkDemo(emf);
        // paginationDemo.run();

        // --- CERINTA 5: BULK OPERATIONS ---
        BulkOperationDemo bulkDemo = new BulkOperationDemo(emf);
        bulkDemo.run();

        // --- LAB VECHI ---
        // ConnectionPoolBenchmark.runBenchmark();
        // ConnectionLeakDemo.runDemo();

        emf.close();
    }
}