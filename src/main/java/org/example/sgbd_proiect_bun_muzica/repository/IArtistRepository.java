package org.example.sgbd_proiect_bun_muzica.repository;

import org.example.sgbd_proiect_bun_muzica.domain.Artist;
import java.util.Optional;

/**
 * Interfata specifica pentru repository-ul de artisti.
 */
public interface IArtistRepository extends Repository<Artist> {
    Optional<Artist> findByName(String name);
}