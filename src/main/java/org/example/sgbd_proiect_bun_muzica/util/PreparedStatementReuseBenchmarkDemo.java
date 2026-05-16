package org.example.sgbd_proiect_bun_muzica.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import org.example.sgbd_proiect_bun_muzica.domain.Album;
import org.example.sgbd_proiect_bun_muzica.domain.Artist;

import java.util.ArrayList;
import java.util.List;

/**
 * CERINTA: Demonstrati beneficiul reutilizarii prepared statements.
 * Test A: creeaza un Query nou la fiecare iteratie.
 * Test B: creeaza Query-ul o singura data si il reutilizeaza.
 */
public class PreparedStatementReuseBenchmarkDemo {

    private static final int ITERATIONS = 1_000;
    private static final int WARM_UP_ITERATIONS = 50;
    private static final String JPQL = "SELECT a FROM Album a WHERE a.id = :id";

    private final EntityManagerFactory emf;

    public PreparedStatementReuseBenchmarkDemo(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void run() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CERINTA: CACHE / REUTILIZARE PREPARED STATEMENTS");
        System.out.println("=".repeat(80));

        List<Long> albumIds = seedBenchmarkData();
        if (albumIds.isEmpty()) {
            System.out.println("Nu exista date pentru benchmark.");
            return;
        }

        warmUp(albumIds);

        long withoutReuseNanos = benchmarkWithoutReuse(albumIds);
        long withReuseNanos = benchmarkWithReuse(albumIds);

        printComparison(withoutReuseNanos, withReuseNanos);
    }

    private List<Long> seedBenchmarkData() {
        List<Long> ids = new ArrayList<>(ITERATIONS);

        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            em.createQuery("DELETE FROM Album a WHERE a.title LIKE 'PS_%'").executeUpdate();
            em.createQuery("DELETE FROM Artist a WHERE a.name = 'PS_Benchmark_Artist'").executeUpdate();
            em.flush();

            Artist artist = new Artist();
            artist.setName("PS_Benchmark_Artist");
            artist.setCountry("Romania");
            artist.setFormedYear(2000);
            em.persist(artist);
            em.flush();

            for (int i = 1; i <= ITERATIONS; i++) {
                Album album = new Album(
                        "PS_Album_" + i,
                        1990 + (i % 30),
                        artist
                );
                em.persist(album);
                ids.add(album.getId());

                if (i % 200 == 0) {
                    em.flush();
                }
            }

            tx.commit();
        }

        return ids;
    }

    private void warmUp(List<Long> ids) {
        try (EntityManager em = emf.createEntityManager()) {
            for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
                Query query = em.createQuery(JPQL);
                query.setParameter("id", ids.get(i % ids.size()));
                query.getSingleResult();
            }
        }

        try (EntityManager em = emf.createEntityManager()) {
            Query query = em.createQuery(JPQL);
            for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
                query.setParameter("id", ids.get(i % ids.size()));
                query.getSingleResult();
            }
        }
    }

    private long benchmarkWithoutReuse(List<Long> ids) {
        try (EntityManager em = emf.createEntityManager()) {
            long start = System.nanoTime();

            for (int i = 0; i < ITERATIONS; i++) {
                Query query = em.createQuery(JPQL);
                query.setParameter("id", ids.get(i % ids.size()));
                query.getSingleResult();
            }

            return System.nanoTime() - start;
        }
    }

    private long benchmarkWithReuse(List<Long> ids) {
        try (EntityManager em = emf.createEntityManager()) {
            Query query = em.createQuery(JPQL);
            long start = System.nanoTime();

            for (int i = 0; i < ITERATIONS; i++) {
                query.setParameter("id", ids.get(i % ids.size()));
                query.getSingleResult();
            }

            return System.nanoTime() - start;
        }
    }

    private void printComparison(long withoutReuseNanos, long withReuseNanos) {
        double withoutReuseMs = withoutReuseNanos / 1_000_000.0;
        double withReuseMs = withReuseNanos / 1_000_000.0;
        double improvement = withoutReuseMs == 0.0 ? 0.0 : ((withoutReuseMs - withReuseMs) * 100.0 / withoutReuseMs);

        System.out.println();
        System.out.println("TEST A - FARA REUTILIZARE");
        System.out.println("  Timp total: " + String.format("%.2f", withoutReuseMs) + " ms");
        System.out.println("  Descriere: Query nou creat la fiecare iteratie");

        System.out.println();
        System.out.println("TEST B - CU REUTILIZARE");
        System.out.println("  Timp total: " + String.format("%.2f", withReuseMs) + " ms");
        System.out.println("  Descriere: Același Query refolosit cu parametri diferiti");

        System.out.println();
        System.out.println("COMPARATIE");
        System.out.println("  Diferenta: " + String.format("%.2f", (withoutReuseMs - withReuseMs)) + " ms");
        System.out.println("  Imbunatatire: " + String.format("%.2f", improvement) + "%");
        System.out.println();
        System.out.println("Interpretare:");
        System.out.println("- Varianta fara reutilizare are overhead suplimentar prin crearea repetata a Query-ului.");
        System.out.println("- Varianta cu reutilizare reduce costul de obiecte/intermediari si lasa driver-ul sa refoloseasca mai bine acelasi SQL/prepared statement.");
        System.out.println("- Diferenta exacta depinde de driver, cache-ul JDBC si baza de date.");
    }
}

