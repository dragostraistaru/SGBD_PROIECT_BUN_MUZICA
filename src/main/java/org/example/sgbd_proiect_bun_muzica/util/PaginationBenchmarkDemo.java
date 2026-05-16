package org.example.sgbd_proiect_bun_muzica.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.example.sgbd_proiect_bun_muzica.domain.Album;
import org.example.sgbd_proiect_bun_muzica.repository.AlbumRepositoryORM;
import org.example.sgbd_proiect_bun_muzica.util.paging.Page;
import org.example.sgbd_proiect_bun_muzica.util.paging.Pageable;

/**
 * CERINTA 3: Paginare - Comparație Offset vs Cursor (Keyset)
 * 
 * Test cu 10.000 albume:
 * - Pagina 1 (OFFSET: 0, LIMIT 100)
 * - Pagina 50 (OFFSET: 4900, LIMIT 100)
 * - Pagina 100 (OFFSET: 9900, LIMIT 100)
 */
public class PaginationBenchmarkDemo {

    private final EntityManagerFactory emf;
    private final AlbumRepositoryORM albumRepo;

    public PaginationBenchmarkDemo(EntityManagerFactory emf) {
        this.emf = emf;
        this.albumRepo = new AlbumRepositoryORM();
    }

    /**
     * Populeaza baza cu 10.000 albume pentru demo
     */
    public void seedLargeData() {
        IndexBenchmarkDemo indexDemo = new IndexBenchmarkDemo(emf);
        indexDemo.seedLargeData();
    }

    public void run() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CERINTA 3: PAGINARE - OFFSET vs CURSOR");
        System.out.println("=".repeat(80));

        IndexBenchmarkDemo indexDemo = new IndexBenchmarkDemo(emf);
        indexDemo.seedLargeData();

        int pageSize = 100;

        System.out.println("\n--- STRATEGIE A: OFFSET/LIMIT ---");
        benchmarkOffset(pageSize);

        System.out.println("\n--- STRATEGIE B: CURSOR (KEYSET) ---");
        benchmarkCursor(pageSize);

        printComparison();
    }

    private void benchmarkOffset(int pageSize) {
        Pageable pageable;
        Page<Album> page;
        long time;

        System.out.println("\nPage 1 (OFFSET=0):");
        pageable = new Pageable(0, pageSize);
        time = System.currentTimeMillis();
        page = albumRepo.findAllOffset(pageable);
        time = System.currentTimeMillis() - time;
        System.out.println("  Time: " + time + " ms");
        System.out.println("  Result: " + page);

        System.out.println("\nPage 50 (OFFSET=4900):");
        pageable = new Pageable(49, pageSize);
        time = System.currentTimeMillis();
        page = albumRepo.findAllOffset(pageable);
        time = System.currentTimeMillis() - time;
        System.out.println("  Time: " + time + " ms");
        System.out.println("  Result: " + page);

        System.out.println("\nPage 100 (OFFSET=9900):");
        pageable = new Pageable(99, pageSize);
        time = System.currentTimeMillis();
        page = albumRepo.findAllOffset(pageable);
        time = System.currentTimeMillis() - time;
        System.out.println("  Time: " + time + " ms");
        System.out.println("  Result: " + page);
    }

    private void benchmarkCursor(int pageSize) {
        Pageable pageable;
        Page<Album> page;
        long time;
        Long lastId = null;

        System.out.println("\nPage 1 (Cursor=null):");
        pageable = new Pageable(0, pageSize);
        time = System.currentTimeMillis();
        page = albumRepo.findAllCursor(pageable, null);
        time = System.currentTimeMillis() - time;
        System.out.println("  Time: " + time + " ms");
        System.out.println("  Result: " + page);
        if (!page.getContent().isEmpty()) {
            lastId = page.getContent().get(page.getContent().size() - 1).getId();
        }

        System.out.println("\nPage 50 (Simulated - jumping to ID ~4900):");
        lastId = 4900L;
        pageable = new Pageable(49, pageSize);
        time = System.currentTimeMillis();
        page = albumRepo.findAllCursor(pageable, lastId);
        time = System.currentTimeMillis() - time;
        System.out.println("  Time: " + time + " ms");
        System.out.println("  Result: " + page);
        if (!page.getContent().isEmpty()) {
            lastId = page.getContent().get(page.getContent().size() - 1).getId();
        }


        System.out.println("\nPage 100 (Simulated - jumping to ID ~9900):");
        lastId = 9900L; // Approximate ID at page 100
        pageable = new Pageable(99, pageSize);
        time = System.currentTimeMillis();
        page = albumRepo.findAllCursor(pageable, lastId);
        time = System.currentTimeMillis() - time;
        System.out.println("  Time: " + time + " ms");
        System.out.println("  Result: " + page);
    }

    private void printComparison() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ANALIZA PERFORMANTA");
        System.out.println("=".repeat(80));
        System.out.println("""
                OFFSET/LIMIT:
                ✓ Simplu de implementat
                ✓ Random access ușor (sari la pagina X)
                ✗ Scumpă pe pagini mari (trebuie Skip N rânduri)
                ✗ Probleme cu date care se schimbă între querys
                
                CURSOR (KEYSET):
                ✓ Performanță constantă indiferent de pagină
                ✓ Eficient pentru tabele mari
                ✓ Consistent chiar cu date care se schimbă
                ✗ Nu poți sări la pagina X direct
                ✗ Trebuie să pastrezi ID-ul ultimului element
                
                RECOMANDARE: 
                - OFFSET pt UI simplu cu +/- pagini si ≤ 1M rânduri
                - CURSOR pt tabele MARI si scroll infinit
                """);

        explainAnalyze();
    }

    private void explainAnalyze() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EXPLAIN ANALYZE - Query Plans");
        System.out.println("=".repeat(80));

        try (EntityManager em = emf.createEntityManager()) {
            System.out.println("\n1. OFFSET Query Plan:");
            var offsetPlan = em.createNativeQuery(
                    "EXPLAIN ANALYZE SELECT * FROM albums ORDER BY id OFFSET 4900 LIMIT 100"
            ).getResultList();
            offsetPlan.forEach(row -> System.out.println("  " + row));

            System.out.println("\n2. CURSOR Query Plan:");
            var cursorPlan = em.createNativeQuery(
                    "EXPLAIN ANALYZE SELECT * FROM albums WHERE id > 4900 ORDER BY id LIMIT 100"
            ).getResultList();
            cursorPlan.forEach(row -> System.out.println("  " + row));
        }
    }
}


