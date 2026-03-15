package org.example.sgbd_proiect_bun_muzica.repository;

import org.example.sgbd_proiect_bun_muzica.domain.Album;
import org.example.sgbd_proiect_bun_muzica.exceptions.RepositoryException;
import org.example.sgbd_proiect_bun_muzica.util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementarea concreta pentru repository-ul de albume.
 */
public class AlbumRepositoryDB extends AbstractRepositoryDB<Album> implements IAlbumRepository {

    @Override
    protected Album mapToEntity(ResultSet rs) throws SQLException {
        return new Album(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getInt("release_year"),
                rs.getLong("artist_id")
        );
    }

    @Override
    protected String getTableName() { return "albums"; }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO albums(title, release_year, artist_id) VALUES (?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Album a) throws SQLException {
        ps.setString(1, a.getTitle());
        ps.setInt(2, a.getReleaseYear());
        ps.setLong(3, a.getArtistId());
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE albums SET title=?, release_year=?, artist_id=? WHERE id=?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Album a) throws SQLException {
        ps.setString(1, a.getTitle());
        ps.setInt(2, a.getReleaseYear());
        ps.setLong(3, a.getArtistId());
        ps.setLong(4, a.getId());
    }

    /**
     * Interogare cheie pentru relatia parinte-copil.
     * SELECT copiii WHERE artist_id = ?
     */
    @Override
    public List<Album> findByArtistId(Long artistId) {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM albums WHERE artist_id = ? ORDER BY release_year";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, artistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) albums.add(mapToEntity(rs));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la findByArtistId=" + artistId, e);
        }
        return albums;
    }
}