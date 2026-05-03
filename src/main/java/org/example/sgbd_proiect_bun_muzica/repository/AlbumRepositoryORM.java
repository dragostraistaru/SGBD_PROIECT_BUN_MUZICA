package org.example.sgbd_proiect_bun_muzica.repository;

import jakarta.persistence.*;
import org.example.sgbd_proiect_bun_muzica.domain.Album;
import org.example.sgbd_proiect_bun_muzica.domain.Artist;
import org.example.sgbd_proiect_bun_muzica.exceptions.RepositoryException;
import org.example.sgbd_proiect_bun_muzica.util.JPAUtil;

import java.util.List;
import java.util.Optional;

public class AlbumRepositoryORM implements IAlbumRepository {

    @Override
    public void add(Album album) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Artist managedArtist = em.find(Artist.class, album.getArtist().getId());
            album.setArtist(managedArtist);

            em.persist(album);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RepositoryException("Eroare la adaugare album", e);
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
            Album album = em.find(Album.class, id);
            if (album == null)
                throw new RepositoryException("Albumul cu id=" + id + " nu exista.");
            em.remove(album);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RepositoryException("Eroare la stergere album", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Album album) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(album);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw new RepositoryException("Eroare la update album", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Album> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            Album album = em.find(Album.class, id);
            return Optional.ofNullable(album);
        } catch (Exception e) {
            throw new RepositoryException("Eroare la findById album", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Album> getAll() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT a FROM Album a ORDER BY a.releaseYear",
                            Album.class)
                    .getResultList();
        } catch (Exception e) {
            throw new RepositoryException("Eroare la getAll albume", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Album> findByArtistId(Long artistId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT a FROM Album a WHERE a.artist.id = :artistId ORDER BY a.releaseYear",
                            Album.class)
                    .setParameter("artistId", artistId)
                    .getResultList();
        } catch (Exception e) {
            throw new RepositoryException("Eroare la findByArtistId", e);
        } finally {
            em.close();
        }
    }
}