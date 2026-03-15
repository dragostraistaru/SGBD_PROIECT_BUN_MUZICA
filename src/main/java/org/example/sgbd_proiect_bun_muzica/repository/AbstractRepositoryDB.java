package org.example.sgbd_proiect_bun_muzica.repository;

import org.example.sgbd_proiect_bun_muzica.exceptions.RepositoryException;
import org.example.sgbd_proiect_bun_muzica.util.ConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementare abstracta JDBC - Template Method pattern.
 * Subclasele implementeaza doar detaliile specifice fiecarei entitati.
 */
public abstract class AbstractRepositoryDB<T> implements Repository<T> {

    protected abstract T mapToEntity(ResultSet rs) throws SQLException;
    protected abstract String getTableName();
    protected abstract String getInsertSQL();
    protected abstract void setInsertParameters(PreparedStatement ps, T entity) throws SQLException;
    protected abstract String getUpdateSQL();
    protected abstract void setUpdateParameters(PreparedStatement ps, T entity) throws SQLException;

    @Override
    public void add(T entity) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(getInsertSQL())) {
            setInsertParameters(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la adaugare in " + getTableName(), e);
        }
    }

    @Override
    public void remove(Long id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0)
                throw new RepositoryException("Entitatea cu id=" + id + " nu exista.");
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la stergere din " + getTableName(), e);
        }
    }

    @Override
    public void update(T entity) {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(getUpdateSQL())) {
            setUpdateParameters(ps, entity);
            int rows = ps.executeUpdate();
            if (rows == 0)
                throw new RepositoryException("Entitatea nu a fost gasita pentru update.");
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la update in " + getTableName(), e);
        }
    }

    @Override
    public Optional<T> findById(Long id) {
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapToEntity(rs));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la cautare in " + getTableName(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<T> getAll() {
        List<T> entities = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY id";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) entities.add(mapToEntity(rs));
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la getAll din " + getTableName(), e);
        }
        return entities;
    }
}