package org.example.sgbd_proiect_bun_muzica.util.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.example.sgbd_proiect_bun_muzica.domain.Artist;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Cache in-memory pentru date read-only accesate frecvent (ex: Artist).
 *
 * - cache by id pentru lookup-uri repetate
 * - cache pentru lista completa de artisti
 * - TTL automat
 * - statistici hit/miss/eviction
 */
public final class ArtistCache {

    private static final String ALL_ARTISTS_KEY = "ALL_ARTISTS";

    private final Cache<Long, Optional<Artist>> artistByIdCache;
    private final Cache<String, List<Artist>> allArtistsCache;

    public ArtistCache() {
        this(Duration.ofMinutes(5), 1_000);
    }

    public ArtistCache(Duration ttl) {
        this(ttl, 1_000);
    }

    public ArtistCache(Duration ttl, long maxSize) {
        this.artistByIdCache = Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maxSize)
                .recordStats()
                .build();

        this.allArtistsCache = Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(1)
                .recordStats()
                .build();
    }

    public Optional<Artist> getArtistById(Long id, Function<Long, Optional<Artist>> loader) {
        if (id == null) {
            return Optional.empty();
        }

        return artistByIdCache.get(id, key -> loader.apply(key).map(ArtistCache::snapshot));
    }

    public List<Artist> getAllArtists(Supplier<List<Artist>> loader) {
        return allArtistsCache.get(ALL_ARTISTS_KEY, key -> snapshotList(loader.get()));
    }

    public void invalidateArtist(Long id) {
        if (id != null) {
            artistByIdCache.invalidate(id);
        }
        allArtistsCache.invalidate(ALL_ARTISTS_KEY);
    }

    public void invalidateAll() {
        artistByIdCache.invalidateAll();
        allArtistsCache.invalidateAll();
    }

    public void cleanup() {
        artistByIdCache.cleanUp();
        allArtistsCache.cleanUp();
    }

    public CacheStats getArtistByIdStats() {
        cleanup();
        return artistByIdCache.stats();
    }

    public CacheStats getAllArtistsStats() {
        cleanup();
        return allArtistsCache.stats();
    }

    public CacheStats getCombinedStats() {
        cleanup();
        return artistByIdCache.stats().plus(allArtistsCache.stats());
    }

    public String formatStatistics() {
        CacheStats byId = getArtistByIdStats();
        CacheStats all = getAllArtistsStats();
        CacheStats total = byId.plus(all);

        return String.format(
                "CACHE STATISTICS%n" +
                        "  Artist-by-id -> requests=%d, hits=%d, misses=%d, hitRate=%.2f%%, missRate=%.2f%%, evictions=%d%n" +
                        "  All-artists   -> requests=%d, hits=%d, misses=%d, hitRate=%.2f%%, missRate=%.2f%%, evictions=%d%n" +
                        "  TOTAL         -> requests=%d, hits=%d, misses=%d, hitRate=%.2f%%, missRate=%.2f%%, evictions=%d",
                byId.requestCount(), byId.hitCount(), byId.missCount(), byId.hitRate() * 100.0, byId.missRate() * 100.0, byId.evictionCount(),
                all.requestCount(), all.hitCount(), all.missCount(), all.hitRate() * 100.0, all.missRate() * 100.0, all.evictionCount(),
                total.requestCount(), total.hitCount(), total.missCount(), total.hitRate() * 100.0, total.missRate() * 100.0, total.evictionCount()
        );
    }

    private static Artist snapshot(Artist artist) {
        if (artist == null) {
            return null;
        }

        Artist copy = new Artist();
        copy.setId(artist.getId());
        copy.setName(artist.getName());
        copy.setCountry(artist.getCountry());
        copy.setFormedYear(artist.getFormedYear());
        return copy;
    }

    private static List<Artist> snapshotList(List<Artist> artists) {
        if (artists == null || artists.isEmpty()) {
            return List.of();
        }

        List<Artist> copies = new ArrayList<>(artists.size());
        for (Artist artist : artists) {
            copies.add(snapshot(artist));
        }
        return List.copyOf(copies);
    }
}

