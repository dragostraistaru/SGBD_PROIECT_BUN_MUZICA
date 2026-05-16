package org.example.sgbd_proiect_bun_muzica.util;

import org.example.sgbd_proiect_bun_muzica.domain.Artist;
import org.example.sgbd_proiect_bun_muzica.repository.AlbumRepositoryORM;
import org.example.sgbd_proiect_bun_muzica.repository.ArtistRepositoryORM;
import org.example.sgbd_proiect_bun_muzica.service.MusicService;
import org.example.sgbd_proiect_bun_muzica.util.cache.ArtistCache;

import java.time.Duration;
import java.util.List;

/**
 * Demo pentru cache-ul de entitati parinte (Artist):
 * - cache miss la primul apel
 * - cache hit la apelurile ulterioare
 * - invalidare la update
 * - expirare TTL
 */
public class ArtistCacheBenchmarkDemo {

    private final MusicService musicService;
    private final Duration ttl;

    public ArtistCacheBenchmarkDemo(MusicService musicService, Duration ttl) {
        this.musicService = musicService;
        this.ttl = ttl;
    }

    public static MusicService createServiceWithCache(Duration ttl) {
        ArtistRepositoryORM artistRepo = new ArtistRepositoryORM();
        AlbumRepositoryORM albumRepo = new AlbumRepositoryORM();
        return new MusicService(artistRepo, albumRepo, new ArtistCache(ttl));
    }

    public void run() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CERINTA 4: CACHE PENTRU ARTISTI (READ-ONLY)");
        System.out.println("=".repeat(80));

        long start;

        start = System.currentTimeMillis();
        musicService.getAllArtists();
        System.out.println("[1] getAllArtists() first call -> " + (System.currentTimeMillis() - start) + " ms (cache miss)");

        start = System.currentTimeMillis();
        List<Artist> artistsSecond = musicService.getAllArtists();
        System.out.println("[2] getAllArtists() second call -> " + (System.currentTimeMillis() - start) + " ms (cache hit)");

        if (artistsSecond.isEmpty()) {
            System.out.println("Nu exista artisti in baza de date. Demo-ul de cache se opreste aici.");
            System.out.println(musicService.getArtistCacheStats());
            return;
        }

        Long sampleId = artistsSecond.getFirst().getId();

        start = System.currentTimeMillis();
        musicService.findArtistById(sampleId);
        System.out.println("[3] findArtistById() first call -> " + (System.currentTimeMillis() - start) + " ms (cache miss)");

        start = System.currentTimeMillis();
        musicService.findArtistById(sampleId);
        System.out.println("[4] findArtistById() second call -> " + (System.currentTimeMillis() - start) + " ms (cache hit)");

        Artist artist = musicService.findArtistById(sampleId).orElseThrow();
        String originalCountry = artist.getCountry();
        artist.setCountry((originalCountry == null ? "RO" : originalCountry) + "_CACHE_TMP");
        musicService.updateArtist(artist);
        System.out.println("[5] updateArtist() -> cache invalidated");

        start = System.currentTimeMillis();
        musicService.findArtistById(sampleId);
        System.out.println("[6] findArtistById() after update -> " + (System.currentTimeMillis() - start) + " ms (cache miss after eviction)");

        start = System.currentTimeMillis();
        musicService.findArtistById(sampleId);
        System.out.println("[7] findArtistById() again -> " + (System.currentTimeMillis() - start) + " ms (cache hit)");

        Artist restored = musicService.findArtistById(sampleId).orElseThrow();
        restored.setCountry(originalCountry);
        musicService.updateArtist(restored);
        System.out.println("[8] restore original artist data -> cache invalidated again");

        musicService.findArtistById(sampleId);
        System.out.println("[9] cache repopulated, astept TTL=" + ttl.toSeconds() + " secunde...");
        try {
            Thread.sleep(ttl.toMillis() + 500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        start = System.currentTimeMillis();
        musicService.findArtistById(sampleId);
        System.out.println("[10] after TTL expiration -> " + (System.currentTimeMillis() - start) + " ms (cache miss)");

        System.out.println();
        System.out.println(musicService.getArtistCacheStats());
        System.out.println();
        System.out.println("Interpretare:");
        System.out.println("- primul apel: incarca din DB (miss)");
        System.out.println("- urmatoarele apeluri: se servesc din cache (hit)");
        System.out.println("- updateArtist(): invalideaza cache-ul");
        System.out.println("- TTL: dupa expirare, urmatorul apel revine la DB");
    }
}

