package org.example.sgbd_proiect_bun_muzica.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "albums")
public class Album extends BaseEntity<Long> {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "release_year", nullable = false)
    private int releaseYear;

    @Column(name = "label", nullable = false, length = 100)
    private String label = "Unknown";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    private Artist artist;

    public Album(Long id, String title, int releaseYear, Artist artist) {
        super(id);
        this.title = title;
        this.releaseYear = releaseYear;
        this.artist = artist;
    }

    public Album(String title, int releaseYear, Artist artist) {
        this.title = title;
        this.releaseYear = releaseYear;
        this.artist = artist;
    }

    public Album() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Artist getArtist() { return artist; }
    public void setArtist(Artist artist) { this.artist = artist; }

    public long getArtistId() {
        return artist != null ? artist.getId() : 0;
    }

    @Override
    public String toString() { return title + " (" + releaseYear + ")"; }
}
