package org.example.sgbd_proiect_bun_muzica.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "artists")
public class Artist extends BaseEntity<Long> {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "formed_year")
    private int formedYear;

    @OneToMany(mappedBy = "artist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Album> albums = new ArrayList<>();

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

    public List<Album> getAlbums() { return albums; }
    public void setAlbums(List<Album> albums) { this.albums = albums; }

    @Override
    public String toString() { return name; }


    public void addAlbum(Album album) {
        albums.add(album);
        album.setArtist(this);
    }
}