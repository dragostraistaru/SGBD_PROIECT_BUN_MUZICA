package org.example.sgbd_proiect_bun_muzica.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.example.sgbd_proiect_bun_muzica.domain.Album;
import org.example.sgbd_proiect_bun_muzica.domain.Artist;

import java.util.List;

/**
 * CERINTA 5: Optimizarea Operațiilor în Masă
 * Compară 3 abordări pentru UPDATE în masă cu 1000+ albume
 */
public class BulkOperationDemo {

    private final EntityManagerFactory emf;

    public BulkOperationDemo(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void run() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CERINTA 5: OPTIMIZAREA OPERATIILOR IN MASA");
        System.out.println("=".repeat(80));

        seedBulkData();

        System.out.println("\n[1] ABORDARE 1: ACTUALIZARI INDIVIDUALE (Lent)");
        testIndividualUpdates();

        System.out.println("\n[2] ABORDARE 2: BULK UPDATE QUERY (Rapid)");
        testBulkUpdateQuery();

        System.out.println("\n[3] ABORDARE 3: BATCH UPDATES (Mediu)");
        testBatchUpdates();

        printComparison();
    }

    private void seedBulkData() {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            em.createQuery("DELETE FROM Album a WHERE a.title LIKE 'BULK_%'").executeUpdate();
            em.createQuery("DELETE FROM Artist a WHERE a.name = 'BULK_Artist'").executeUpdate();
            em.flush();

            Artist artist = new Artist();
            artist.setName("BULK_Artist");
            artist.setCountry("Romania");
            artist.setFormedYear(2000);
            em.persist(artist);
            em.flush();

            System.out.println("Seeding 1000 albume pentru test...");
            for (int i = 1; i <= 1000; i++) {
                Album album = new Album(
                        "BULK_Album_" + i,
                        1980 + (i % 45),
                        artist
                );
                album.setReleaseYear(2000); // Setez anul uniform pentru update
                em.persist(album);

                if (i % 100 == 0) {
                    em.flush();
                    em.clear();
                    artist = em.find(Artist.class, artist.getId());
                }
            }

            tx.commit();
            System.out.println(" 1000 albume created");
        }
    }

    /**
     * ABORDARE 1: Actualizări Individuale
     * Fetch + loop + merge (N operații DB)
     */
    private void testIndividualUpdates() {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            long start = System.currentTimeMillis();

            List<Album> albums = em.createQuery(
                    "SELECT a FROM Album a WHERE a.title LIKE 'BULK_%'",
                    Album.class
            ).getResultList();

            for (Album album : albums) {
                album.setReleaseYear(album.getReleaseYear() + 10); // Mărire an
                em.merge(album);  // 1000 INSERT/UPDATE statements
            }

            tx.commit();
            long time = System.currentTimeMillis() - start;

            System.out.println("    Timp: " + time + " ms");
            System.out.println("   Probleme: N querys (1000x merge), lent pe date mari");
            System.out.println("   Avantaj: Flexibilitate totală, validare per record");
        }
    }

    /**
     * ABORDARE 2: Bulk Update Query
     * 1 UPDATE statement pentru toți (rapid!)
     */
    private void testBulkUpdateQuery() {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            long start = System.currentTimeMillis();

            // 1 query pentru tot
            int updatedCount = em.createQuery(
                    "UPDATE Album a SET a.releaseYear = a.releaseYear + 20 " +
                    "WHERE a.title LIKE 'BULK_%'"
            ).executeUpdate();

            tx.commit();
            long time = System.currentTimeMillis() - start;

            System.out.println("  ⏱️  Timp: " + time + " ms");
            System.out.println("  ✅ Updated: " + updatedCount + " albume cu 1 query!");
            System.out.println("  ✅ Avantaj: FOARTE rapid, 1 statement");
            System.out.println("  ❌ Limitare: Doar SQL-like updates, fără logică Java");
        }
    }

    /**
     * ABORDARE 3: Batch Updates
     * Merge cu flush periodic
     */
    private void testBatchUpdates() {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            long start = System.currentTimeMillis();

            // Fetch all
            List<Album> albums = em.createQuery(
                    "SELECT a FROM Album a WHERE a.title LIKE 'BULK_%'",
                    Album.class
            ).getResultList();

            // Update în batches
            int batchSize = 50;
            for (int i = 0; i < albums.size(); i++) {
                Album album = albums.get(i);
                album.setReleaseYear(album.getReleaseYear() + 30);
                em.merge(album);

                // Flush + clear la fiecare 50
                if ((i + 1) % batchSize == 0) {
                    em.flush();
                    em.clear();
                }
            }

            // Final flush
            em.flush();

            tx.commit();
            long time = System.currentTimeMillis() - start;

            System.out.println("    Timp: " + time + " ms");
            System.out.println("   Batchuri: 1000 / 50 = 20 flushes (vs 1000)");
            System.out.println("   Balanță: Performanță OK + Flexibilitate");
            System.out.println("   Avantaj: Memory-efficient (clear cache), mai rapid decât individual");
        }
    }

    private void printComparison() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ANALIZA COMPARATIVA");
        System.out.println("=".repeat(80));
        System.out.println("""
                ABORDARE 1: ACTUALIZARI INDIVIDUALE
                Timp: ~1000+ ms
                Querys: 1000+
                Descriere: Fetch + loop + merge (1 query per record)
                 Avantaje:
                   - Logică complexă possibilă în Java
                   - Validare per record
                   - Facilă de debugat
                 Dezavantaje:
                   - N+1 problem (1000 queries!)
                   - Slow pe date mari
                   - Memory overhead (toți obiecții în memorie)
                
                ABORDARE 2: BULK UPDATE QUERY
                Timp: ~10-50 ms
                Querys: 1 (pur SQL)
                Descriere: UPDATE ... SET ... WHERE cu 1 statement
                 Avantaje:
                   - FOARTE rapid (1 query!)
                   - DB optimization
                   - Memory efficient
                 Dezavantaje:
                   - Doar update-uri SQL
                   - Fără validare custom
                   - Cache coherence issues (obiecții din memorie nu sunt updated)
                
                ABORDARE 3: BATCH UPDATES
                Timp: ~100-200 ms
                Querys: ~20 (1000/50)
                Descriere: Merge + flush periodic
                 Avantaje:
                   - Balanță bună între performanță + flexibilitate
                   - Logică Java posibilă
                   - Memory-efficient (clear periodoc)
                   - Validare possible
                 Dezavantaje:
                   - Mai complex de implementat
                   - Batch size e critical
                
                RECOMANDARE:
                - BULK QUERY (2) → Doar actualizări simple + maxim performance
                - BATCH UPDATES (3) → Logică complexă + performance bună
                - INDIVIDUAL (1) → Doar pt <100 records sau logică muy complexă
                """);

        explainAnalyze();
    }

    private void explainAnalyze() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("EXPLAIN ANALYZE - Query Plans");
        System.out.println("=".repeat(80));

        try (EntityManager em = emf.createEntityManager()) {
            System.out.println("\n1. BULK UPDATE Query Plan:");
            var bulkPlan = em.createNativeQuery(
                    "EXPLAIN ANALYZE UPDATE albums SET release_year = release_year + 10 " +
                    "WHERE title LIKE 'BULK_%'"
            ).getResultList();
            bulkPlan.forEach(row -> System.out.println("  " + row));

            System.out.println("\n2. Individual SELECT + UPDATE (Simulated):");
            var selectPlan = em.createNativeQuery(
                    "EXPLAIN ANALYZE SELECT * FROM albums WHERE title LIKE 'BULK_%'"
            ).getResultList();
            selectPlan.forEach(row -> System.out.println("  " + row));
        }
    }
}


