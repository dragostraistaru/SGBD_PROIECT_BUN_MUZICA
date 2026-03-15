package org.example.sgbd_proiect_bun_muzica.repository;

import org.example.sgbd_proiect_bun_muzica.domain.Album;
import java.util.List;

/**
 * Interfata specifica pentru repository-ul de albume.
 */
public interface IAlbumRepository extends Repository<Album> {
    List<Album> findByArtistId(Long artistId);
}