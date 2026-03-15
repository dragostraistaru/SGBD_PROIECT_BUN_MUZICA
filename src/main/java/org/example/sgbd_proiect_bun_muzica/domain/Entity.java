package org.example.sgbd_proiect_bun_muzica.domain;

/**
 * Clasa de baza abstracta pentru toate entitatile domeniului.
 */
public abstract class Entity<ID> {
    private ID id;

    public Entity(ID id) { this.id = id; }
    public Entity() {}

    public ID getId() { return id; }
    public void setId(ID id) { this.id = id; }
}