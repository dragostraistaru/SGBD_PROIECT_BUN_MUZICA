package org.example.sgbd_proiect_bun_muzica.domain;

/**
 * Entitatea Album - copilul in relatia 1-N cu Artist.
 */
public class Album extends Entity<Long> {
    private String title;
    private int releaseYear;
    private long artistId;

    public Album(Long id, String title, int releaseYear, long artistId) {
        super(id);
        this.title = title;
        this.releaseYear = releaseYear;
        this.artistId = artistId;
    }

    public Album() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    public long getArtistId() { return artistId; }
    public void setArtistId(long artistId) { this.artistId = artistId; }

    @Override
    public String toString() { return title + " (" + releaseYear + ")"; }
}