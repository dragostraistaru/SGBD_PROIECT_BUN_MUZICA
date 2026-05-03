package org.example.sgbd_proiect_bun_muzica.util;

import jakarta.persistence.EntityManager;
import org.example.sgbd_proiect_bun_muzica.domain.Artist;
import java.util.List;

public class LoadingDemo {

    /** LAZY - albumele NU sunt incarcate initial */
    public static void demonstrateLazy() {
        System.out.println("\n=== LAZY LOADING ===");

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            Artist artist = em.find(Artist.class, 1L);

            System.out.println("Artist incarcat: " + artist.getName());
            System.out.println("Albumele NU sunt incarcate inca...");

            System.out.println("Accesam albumele...");
            int count = artist.getAlbums().size();

            System.out.println("Numar albume: " + count);

        } finally {
            em.close();
        }
    }

    /** EAGER - artist + albume intr-un singur query */
    public static void demonstrateEager() {
        System.out.println("\n=== EAGER LOADING (JOIN FETCH) ===");

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            List<Artist> artists = em.createQuery(
                            "SELECT DISTINCT a FROM Artist a LEFT JOIN FETCH a.albums",
                            Artist.class)
                    .getResultList();

            for (Artist a : artists) {
                System.out.println("Artist: " + a.getName() +
                        " -> " + a.getAlbums().size() + " albume");
            }

        } finally {
            em.close();
        }
    }

    public static void runDemo() {
        demonstrateLazy();
        demonstrateEager();
    }
}