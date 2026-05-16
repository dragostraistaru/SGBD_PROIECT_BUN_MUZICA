package org.example.sgbd_proiect_bun_muzica.service;

import org.example.sgbd_proiect_bun_muzica.domain.Album;
import org.example.sgbd_proiect_bun_muzica.domain.Artist;
import org.example.sgbd_proiect_bun_muzica.exceptions.RepositoryException;
import org.example.sgbd_proiect_bun_muzica.repository.IAlbumRepository;
import org.example.sgbd_proiect_bun_muzica.repository.IArtistRepository;
import org.example.sgbd_proiect_bun_muzica.util.cache.ArtistCache;

import java.util.List;
import java.util.Optional;

/**
 * Service pentru logica de business.
 * Depinde de interfete - respecta Dependency Inversion (SOLID).
 */
public class MusicService {

    private final IArtistRepository artistRepository;
    private final IAlbumRepository  albumRepository;
    private final ArtistCache artistCache;

    public MusicService(IArtistRepository artistRepository, IAlbumRepository albumRepository) {
        this(artistRepository, albumRepository, new ArtistCache());
    }

    public MusicService(IArtistRepository artistRepository, IAlbumRepository albumRepository, ArtistCache artistCache) {
        this.artistRepository = artistRepository;
        this.albumRepository  = albumRepository;
        this.artistCache = artistCache;
    }

    public List<Artist> getAllArtists() {
        return artistCache.getAllArtists(() -> artistRepository.getAll());
    }

    public Optional<Artist> findArtistById(Long id) {
        return artistCache.getArtistById(id, artistRepository::findById);
    }

    public void updateArtist(Artist artist) {
        artistRepository.update(artist);
        artistCache.invalidateArtist(artist.getId());
    }

    public String getArtistCacheStats() {
        return artistCache.formatStatistics();
    }


    /** Returneaza albumele artistului selectat (relatia parinte-copil) */
    public List<Album> getAlbumsForArtist(Long artistId) {
        return albumRepository.findByArtistId(artistId);
    }

    /** Adauga album nou cu validare */
    public void addAlbum(String title, int releaseYear, Long artistId) {
        validateAlbum(title, releaseYear);
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RepositoryException("Artistul nu a fost gasit!"));
        albumRepository.add(new Album(title, releaseYear, artist));
    }

    /** Actualizeaza album cu validare */
    public void updateAlbum(Long id, String title, int releaseYear, Long artistId) {
        validateAlbum(title, releaseYear);
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RepositoryException("Artistul nu a fost gasit!"));
        Album album = new Album(title, releaseYear, artist);
        album.setId(id);
        albumRepository.update(album);
    }

    /** Sterge album */
    public void deleteAlbum(Long id) {
        albumRepository.remove(id);
    }

    private void validateAlbum(String title, int releaseYear) {
        if (title == null || title.trim().isEmpty())
            throw new RepositoryException("Titlul albumului nu poate fi gol!");
        if (releaseYear < 1900 || releaseYear > 2100)
            throw new RepositoryException("Anul trebuie sa fie intre 1900 si 2100!");
    }
}