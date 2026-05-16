package org.example.sgbd_proiect_bun_muzica.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.sgbd_proiect_bun_muzica.domain.Artist;
import org.example.sgbd_proiect_bun_muzica.exceptions.RepositoryException;
import org.example.sgbd_proiect_bun_muzica.util.JPAUtil;
import org.example.sgbd_proiect_bun_muzica.util.paging.Page;
import org.example.sgbd_proiect_bun_muzica.util.paging.Pageable;

import java.util.List;
import java.util.Optional;

public class ArtistRepositoryORM implements IArtistRepository {

    @Override
    public void add(Artist artist) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(artist);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RepositoryException("Eroare la adaugare artist", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void remove(Long id) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Artist artist = em.find(Artist.class, id);
            if (artist == null) {
                throw new RepositoryException("Artistul cu id=" + id + " nu exista.");
            }
            em.remove(artist);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RepositoryException("Eroare la stergere artist", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Artist artist) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(artist);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RepositoryException("Eroare la update artist", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Artist> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            Artist artist = em.find(Artist.class, id);
            return Optional.ofNullable(artist);
        } catch (Exception e) {
            throw new RepositoryException("Eroare la cautare artist dupa id", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Artist> getAll() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery("SELECT a FROM Artist a ORDER BY a.id", Artist.class)
                    .getResultList();
        } catch (Exception e) {
            throw new RepositoryException("Eroare la getAll artisti", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Artist> findByName(String name) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            List<Artist> results = em.createQuery(
                            "SELECT a FROM Artist a WHERE LOWER(a.name) LIKE LOWER(:name)",
                            Artist.class)
                    .setParameter("name", "%" + name + "%")
                    .getResultList();

            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            throw new RepositoryException("Eroare la findByName", e);
        } finally {
            em.close();
        }
    }

    /**
     * STRATEGIE A: Paginare cu OFFSET/LIMIT
     * Simplu și direct, dar mai lent pe pagini mari
     */
    public Page<Artist> findAllOffset(Pageable pageable) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            long total = em.createQuery("SELECT COUNT(a) FROM Artist a", Long.class)
                    .getSingleResult();

            List<Artist> content = em.createQuery("SELECT a FROM Artist a ORDER BY a.id", Artist.class)
                    .setFirstResult(pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            return new Page<>(content, pageable.getPageNumber(), pageable.getPageSize(), total);
        } catch (Exception e) {
            throw new RepositoryException("Eroare la findAllOffset", e);
        } finally {
            em.close();
        }
    }

    /**
     * STRATEGIE B: Paginare cu Cursor (Keyset)
     * Mai rapid pentru pagini mari - uses indexed column
     * lastId = ID-ul ultimului element din pagina anterioara
     */
    public Page<Artist> findAllCursor(Pageable pageable, Long lastId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            // Count total
            long total = em.createQuery("SELECT COUNT(a) FROM Artist a", Long.class)
                    .getSingleResult();

            // Fetch page - only fetch what's needed
            List<Artist> content;
            if (lastId == null) {
                // First page
                content = em.createQuery("SELECT a FROM Artist a ORDER BY a.id", Artist.class)
                        .setMaxResults(pageable.getPageSize())
                        .getResultList();
            } else {
                // Next pages - efficient keyset pagination
                content = em.createQuery(
                        "SELECT a FROM Artist a WHERE a.id > :lastId ORDER BY a.id",
                        Artist.class)
                        .setParameter("lastId", lastId)
                        .setMaxResults(pageable.getPageSize())
                        .getResultList();
            }

            return new Page<>(content, pageable.getPageNumber(), pageable.getPageSize(), total);
        } catch (Exception e) {
            throw new RepositoryException("Eroare la findAllCursor", e);
        } finally {
            em.close();
        }
    }
}