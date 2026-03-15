package org.example.sgbd_proiect_bun_muzica.repository;

import java.util.List;
import java.util.Optional;

/**
 * Interfata generica pentru operatiile CRUD de baza.
 */
public interface Repository<T> {
    void add(T entity);
    void remove(Long id);
    void update(T entity);
    Optional<T> findById(Long id);
    List<T> getAll();
}