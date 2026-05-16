package org.example.sgbd_proiect_bun_muzica.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.example.sgbd_proiect_bun_muzica.domain.Album;
import org.example.sgbd_proiect_bun_muzica.domain.Artist;

import java.util.List;

public class IndexBenchmarkDemo {

    private final EntityManagerFactory emf;

    public IndexBenchmarkDemo(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void runTaskA() {
        System.out.println("\n--- SARCINA A: BENCHMARK FARA INDEX-URI ---");

        seedLargeData();
        dropIndexes();

        benchmarkQuery("Cautare dupa titlu",
                "SELECT * FROM albums WHERE title = 'Test Album 5000'");

        benchmarkQuery("Cautare dupa artist",
                "SELECT * FROM albums WHERE artist_id = 1");

        benchmarkQuery("Interval release year",
                "SELECT * FROM albums WHERE release_year BETWEEN 1990 AND 2010");

        benchmarkQuery("Cautare multi-coloana",
                "SELECT * FROM albums WHERE artist_id = 1 AND release_year > 2000");

        explainAnalyze("EXPLAIN ANALYZE - Cautare dupa titlu",
                "EXPLAIN ANALYZE SELECT * FROM albums WHERE title = 'Test Album 5000'");

        explainAnalyze("EXPLAIN ANALYZE - Cautare dupa artist",
                "EXPLAIN ANALYZE SELECT * FROM albums WHERE artist_id = 1");

        explainAnalyze("EXPLAIN ANALYZE - Interval release year",
                "EXPLAIN ANALYZE SELECT * FROM albums WHERE release_year BETWEEN 1990 AND 2010");

        explainAnalyze("EXPLAIN ANALYZE - Multi-coloana",
                "EXPLAIN ANALYZE SELECT * FROM albums WHERE artist_id = 1 AND release_year > 2000");
    }


    public void runTaskB() {
        System.out.println("\n--- SARCINA B: ADAUGARE INDEX-URI ---");

        createIndexes();

        System.out.println("Index-urile au fost create.");
    }


    public void runTaskC() {
        System.out.println("\n--- SARCINA C: BENCHMARK CU INDEX-URI ---");

        benchmarkQuery("Cautare dupa titlu",
                "SELECT * FROM albums WHERE title = 'Test Album 5000'");

        benchmarkQuery("Cautare dupa artist",
                "SELECT * FROM albums WHERE artist_id = 1");

        benchmarkQuery("Interval release year",
                "SELECT * FROM albums WHERE release_year BETWEEN 1990 AND 2010");

        benchmarkQuery("Cautare multi-coloana",
                "SELECT * FROM albums WHERE artist_id = 1 AND release_year > 2000");

        explainAnalyze("EXPLAIN ANALYZE - Cautare dupa titlu",
                "EXPLAIN ANALYZE SELECT * FROM albums WHERE title = 'Test Album 5000'");

        explainAnalyze("EXPLAIN ANALYZE - Cautare dupa artist",
                "EXPLAIN ANALYZE SELECT * FROM albums WHERE artist_id = 1");

        explainAnalyze("EXPLAIN ANALYZE - Interval release year",
                "EXPLAIN ANALYZE SELECT * FROM albums WHERE release_year BETWEEN 1990 AND 2010");

        explainAnalyze("EXPLAIN ANALYZE - Multi-coloana",
                "EXPLAIN ANALYZE SELECT * FROM albums WHERE artist_id = 1 AND release_year > 2000");
    }


    private void createIndexes() {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            em.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_albums_title ON albums(title)").executeUpdate();
            em.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_albums_artist_id ON albums(artist_id)").executeUpdate();
            em.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_albums_release_year ON albums(release_year)").executeUpdate();
            em.createNativeQuery("CREATE INDEX IF NOT EXISTS idx_albums_artist_year ON albums(artist_id, release_year)").executeUpdate();

            tx.commit();
        }
    }

    /// PT SARCINA A///

    /**
     * Populeaza tabelul cu 10.000 albume pentru UI (FĂRĂ a șterge datele serios)
     */
    public void seedUIData() {
        try (EntityManager em = emf.createEntityManager()) {

            Long totalCount = em.createQuery("SELECT COUNT(a) FROM Album a", Long.class)
                    .getSingleResult();

            System.out.println("Albume existente: " + totalCount);


            if (totalCount >= 10000) {
                System.out.println(" Deja sunt " + totalCount + " albume");
                return;
            }

            EntityTransaction tx = em.getTransaction();
            tx.begin();

            Artist uiArtist = new Artist();
            uiArtist.setName("UI_PaginationArtist");
            uiArtist.setCountry("Romania");
            uiArtist.setFormedYear(2000);
            em.persist(uiArtist);
            em.flush();

            int albumsToAdd = (int) (10000 - totalCount);
            System.out.println("Adăugând " + albumsToAdd + " albume...");

            for (int i = 1; i <= albumsToAdd; i++) {
                Album album = new Album(
                        "Album_UI_" + (totalCount + i),
                        1980 + (i % 45),
                        uiArtist
                );
                em.persist(album);

                if (i % 500 == 0) {
                    em.flush();
                }
            }

            tx.commit();
            System.out.println(" UI Data complete: 10.000+ albume total");
        }
    }

    /**
     * Populeaza tabelul cu 10.000 albume pentru benchmark
     */
    public void seedLargeData() {
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();
            em.createQuery("DELETE FROM Album a").executeUpdate();
            em.createQuery("DELETE FROM Artist a").executeUpdate();
            em.flush();

            Artist artist = new Artist();
            artist.setName("Benchmark Artist");
            artist.setCountry("Romania");
            artist.setFormedYear(2000);
            em.persist(artist);
            em.flush();

            for (int i = 1; i <= 10000; i++) {
                Album album = new Album(
                        "Test Album " + i,
                        1980 + (i % 45),
                        artist
                );

                em.persist(album);

                if (i % 500 == 0) {
                    em.flush();
                    em.clear();
                    artist = em.find(Artist.class, artist.getId());
                }
            }

            em.getTransaction().commit();
            System.out.println(" Benchmark Data: 10.000 albume");
        }
    }

    private void dropIndexes() {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            em.createNativeQuery("DROP INDEX IF EXISTS idx_albums_title").executeUpdate();
            em.createNativeQuery("DROP INDEX IF EXISTS idx_albums_artist_id").executeUpdate();
            em.createNativeQuery("DROP INDEX IF EXISTS idx_albums_release_year").executeUpdate();
            em.createNativeQuery("DROP INDEX IF EXISTS idx_albums_artist_year").executeUpdate();

            tx.commit();
        }
    }

    private void benchmarkQuery(String name, String sql) {
        try (EntityManager em = emf.createEntityManager()) {
            long start = System.currentTimeMillis();

            for (int i = 0; i < 100; i++) {
                em.createNativeQuery(sql).getResultList();
            }

            long total = System.currentTimeMillis() - start;
            double average = total / 100.0;

            System.out.println(name + ": total = " + total + " ms, medie = " + average + " ms");
        }
    }

    private void explainAnalyze(String name, String sql) {
        try (EntityManager em = emf.createEntityManager()) {
            System.out.println("\n" + name);

            List<?> result = em.createNativeQuery(sql).getResultList();

            for (Object row : result) {
                System.out.println(row);
            }
        }
    }
    /// FINAL SARCINA A///
}