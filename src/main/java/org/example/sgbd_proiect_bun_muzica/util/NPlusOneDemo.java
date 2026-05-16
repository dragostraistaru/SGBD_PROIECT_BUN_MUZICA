package org.example.sgbd_proiect_bun_muzica.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.example.sgbd_proiect_bun_muzica.domain.Album;
import org.example.sgbd_proiect_bun_muzica.domain.Artist;

import java.util.List;

public class NPlusOneDemo {

    private final EntityManagerFactory emf;

    public NPlusOneDemo(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void run() {
        System.out.println("\n--- N+1 DEMO ---");

        seedSmallData();
        demonstrateNPlusOne();
        demonstrateFetchJoin();
    }

    private void seedSmallData() {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            em.createQuery("DELETE FROM Album a").executeUpdate();
            em.createQuery("DELETE FROM Artist a").executeUpdate();
            em.flush();

            for (int i = 1; i <= 10; i++) {
                Artist artist = new Artist();
                artist.setName("Test Artist " + i);
                artist.setCountry("Romania");
                artist.setFormedYear(2000 + i);

                for (int j = 1; j <= 3; j++) {
                    Album album = new Album(
                            "Test Album " + i + "." + j,
                            2000 + j,
                            artist
                    );
                    artist.getAlbums().add(album);
                }

                em.persist(artist);
            }

            tx.commit();
            System.out.println(" N+1 Demo Data: 10 artisti x 3 albume = 30 albume");
        }
    }

    private void demonstrateNPlusOne() {
        try (EntityManager em = emf.createEntityManager()) {
            long start = System.currentTimeMillis();

            List<Artist> artists = em
                    .createQuery("SELECT a FROM Artist a", Artist.class)
                    .getResultList();

            for (Artist artist : artists) {
                System.out.println(artist.getName() + " -> " + artist.getAlbums().size() + " albume");
            }

            long time = System.currentTimeMillis() - start;
            System.out.println("N+1 time: " + time + " ms");
        }
    }

    private void demonstrateFetchJoin() {
        try (EntityManager em = emf.createEntityManager()) {
            long start = System.currentTimeMillis();

            List<Artist> artists = em
                    .createQuery(
                            "SELECT DISTINCT a FROM Artist a LEFT JOIN FETCH a.albums",
                            Artist.class
                    )
                    .getResultList();

            for (Artist artist : artists) {
                System.out.println("[FETCH] " + artist.getName() + " -> " + artist.getAlbums().size() + " albume");
            }

            long time = System.currentTimeMillis() - start;
            System.out.println("FETCH time: " + time + " ms");
        }
    }
}