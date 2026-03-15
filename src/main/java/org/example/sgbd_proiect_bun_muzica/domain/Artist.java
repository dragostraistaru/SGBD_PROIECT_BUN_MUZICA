package org.example.sgbd_proiect_bun_muzica.domain;

/**
 * Entitatea Artist - parintele in relatia 1-N cu Album.
 */
public class Artist extends Entity<Long> {
    private String name;
    private String country;
    private int formedYear;

    public Artist(Long id, String name, String country, int formedYear) {
        super(id);
        this.name = name;
        this.country = country;
        this.formedYear = formedYear;
    }

    public Artist() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public int getFormedYear() { return formedYear; }
    public void setFormedYear(int formedYear) { this.formedYear = formedYear; }

    @Override
    public String toString() { return name; }
}