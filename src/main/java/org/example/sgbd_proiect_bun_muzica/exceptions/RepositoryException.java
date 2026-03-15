package org.example.sgbd_proiect_bun_muzica.exceptions;

/**
 * Exceptie custom pentru erorile din layer-ul de repository.
 */
public class RepositoryException extends RuntimeException {
    public RepositoryException(String message) { super(message); }
    public RepositoryException(String message, Throwable cause) { super(message, cause); }
}