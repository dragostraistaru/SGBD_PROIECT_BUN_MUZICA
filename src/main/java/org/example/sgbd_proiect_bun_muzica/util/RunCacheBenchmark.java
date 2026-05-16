package org.example.sgbd_proiect_bun_muzica.util;

import jakarta.persistence.EntityManagerFactory;
import org.example.sgbd_proiect_bun_muzica.service.MusicService;

import java.time.Duration;

public class RunCacheBenchmark {
    public static void main(String[] args) {
        EntityManagerFactory emf = JPAUtil.getEntityManagerFactory();

        MusicService musicService = ArtistCacheBenchmarkDemo.createServiceWithCache(Duration.ofSeconds(3));
        ArtistCacheBenchmarkDemo demo = new ArtistCacheBenchmarkDemo(musicService, Duration.ofSeconds(3));
        demo.run();

        emf.close();
    }
}

