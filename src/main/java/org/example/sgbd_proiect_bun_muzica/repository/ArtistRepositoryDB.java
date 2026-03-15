package org.example.sgbd_proiect_bun_muzica.repository;

import org.example.sgbd_proiect_bun_muzica.domain.Artist;
import org.example.sgbd_proiect_bun_muzica.exceptions.RepositoryException;
import org.example.sgbd_proiect_bun_muzica.util.ConnectionFactory;

import java.sql.*;
import java.util.Optional;

/**
 * Implementarea concreta pentru repository-ul de artisti.
 */
public class ArtistRepositoryDB extends AbstractRepositoryDB<Artist> implements IArtistRepository {

    @Override
    protected Artist mapToEntity(ResultSet rs) throws SQLException {
        return new Artist(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("country"),
                rs.getInt("formed_year")
        );
    }

    @Override
    protected String getTableName() { return "artists"; }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO artists(name, country, formed_year) VALUES (?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Artist a) throws SQLException {
        ps.setString(1, a.getName());
        ps.setString(2, a.getCountry());
        ps.setInt(3, a.getFormedYear());
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE artists SET name=?, country=?, formed_year=? WHERE id=?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Artist a) throws SQLException {
        ps.setString(1, a.getName());
        ps.setString(2, a.getCountry());
        ps.setInt(3, a.getFormedYear());
        ps.setLong(4, a.getId());
    }

    @Override
    public Optional<Artist> findByName(String name) {
        String sql = "SELECT * FROM artists WHERE LOWER(name) LIKE LOWER(?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapToEntity(rs));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la findByName", e);
        }
        return Optional.empty();
    }
}