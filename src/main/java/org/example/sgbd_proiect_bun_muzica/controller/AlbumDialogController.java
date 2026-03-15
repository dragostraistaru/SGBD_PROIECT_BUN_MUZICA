package org.example.sgbd_proiect_bun_muzica.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.sgbd_proiect_bun_muzica.domain.Album;
import org.example.sgbd_proiect_bun_muzica.domain.Artist;
import org.example.sgbd_proiect_bun_muzica.exceptions.RepositoryException;
import org.example.sgbd_proiect_bun_muzica.service.MusicService;

/**
 * Controller pentru dialogul de adaugare/editare album.
 */
public class AlbumDialogController {

    @FXML private Label     dialogTitleLabel;
    @FXML private TextField titleField;
    @FXML private TextField yearField;
    @FXML private Label     errorLabel;

    private MusicService musicService;
    private Album        existingAlbum;
    private Artist       artist;
    private Stage        dialogStage;

    public void setData(MusicService musicService, Album album, Artist artist, Stage dialogStage) {
        this.musicService  = musicService;
        this.existingAlbum = album;
        this.artist        = artist;
        this.dialogStage   = dialogStage;

        if (album != null) {
            dialogTitleLabel.setText("Editeaza Album");
            titleField.setText(album.getTitle());
            yearField.setText(String.valueOf(album.getReleaseYear()));
        } else {
            dialogTitleLabel.setText("Adauga Album pentru: " + artist.getName());
        }
    }

    @FXML
    private void onSave() {
        errorLabel.setText("");
        String title    = titleField.getText().trim();
        String yearText = yearField.getText().trim();

        if (title.isEmpty()) {
            errorLabel.setText("Titlul nu poate fi gol!");
            titleField.requestFocus();
            return;
        }
        if (yearText.isEmpty()) {
            errorLabel.setText("Anul nu poate fi gol!");
            yearField.requestFocus();
            return;
        }

        int releaseYear;
        try {
            releaseYear = Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            errorLabel.setText("Anul trebuie sa fie un numar intreg!");
            yearField.requestFocus();
            return;
        }

        try {
            if (existingAlbum == null) {
                musicService.addAlbum(title, releaseYear, artist.getId());
            } else {
                musicService.updateAlbum(existingAlbum.getId(), title, releaseYear, artist.getId());
            }
            dialogStage.close();
        } catch (RepositoryException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        dialogStage.close();
    }
}