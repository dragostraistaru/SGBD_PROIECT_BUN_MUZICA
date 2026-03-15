package org.example.sgbd_proiect_bun_muzica.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.sgbd_proiect_bun_muzica.HelloApplication;
import org.example.sgbd_proiect_bun_muzica.domain.Album;
import org.example.sgbd_proiect_bun_muzica.domain.Artist;
import org.example.sgbd_proiect_bun_muzica.exceptions.RepositoryException;
import org.example.sgbd_proiect_bun_muzica.service.MusicService;

import java.io.IOException;
import java.util.List;

/**
 * Controller pentru fereastra principala (main-view.fxml).
 * Gestioneaza vizualizarea master-detail: artisti -> albume.
 * Include functionalitate de cautare/filtrare in timp real.
 */
public class MainController {

    // Tabel parinte - Artisti
    @FXML private TableView<Artist>            artistTable;
    @FXML private TableColumn<Artist, Long>    artistIdCol;
    @FXML private TableColumn<Artist, String>  artistNameCol;
    @FXML private TableColumn<Artist, String>  artistCountryCol;
    @FXML private TableColumn<Artist, Integer> artistYearCol;

    // Tabel copil - Albume
    @FXML private TableView<Album>             albumTable;
    @FXML private TableColumn<Album, Long>     albumIdCol;
    @FXML private TableColumn<Album, String>   albumTitleCol;
    @FXML private TableColumn<Album, Integer>  albumYearCol;

    @FXML private Label     selectedArtistLabel;
    @FXML private TextField searchField;

    private MusicService musicService;

    // Lista completa de artisti - sursa pentru FilteredList
    private final ObservableList<Artist> allArtists = FXCollections.observableArrayList();

    public void setMusicService(MusicService musicService) {
        this.musicService = musicService;
        initTables();
        loadArtists();
        initSearch();
    }

    /**
     * Initializeaza cautarea in timp real pe tabelul de artisti.
     * FilteredList filtreaza allArtists pe baza textului din searchField.
     */
    private void initSearch() {
        FilteredList<Artist> filteredArtists = new FilteredList<>(allArtists, a -> true);

        // La fiecare tasta apasata, actualizeaza filtrul
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredArtists.setPredicate(artist -> {
                if (newVal == null || newVal.trim().isEmpty()) return true;
                return artist.getName().toLowerCase().contains(newVal.toLowerCase().trim());
            });
        });

        artistTable.setItems(filteredArtists);
    }

    private void initTables() {
        // Coloane artisti
        artistIdCol.setCellValueFactory(d ->
                new SimpleLongProperty(d.getValue().getId()).asObject());
        artistNameCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getName()));
        artistCountryCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCountry()));
        artistYearCol.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getFormedYear()).asObject());

        // Coloane albume
        albumIdCol.setCellValueFactory(d ->
                new SimpleLongProperty(d.getValue().getId()).asObject());
        albumTitleCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTitle()));
        albumYearCol.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getReleaseYear()).asObject());

        // Listener selectie artist -> incarca albumele automat
        artistTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedArtistLabel.setText(newVal.getName());
                        loadAlbumsForArtist(newVal.getId());
                    } else {
                        albumTable.getItems().clear();
                        selectedArtistLabel.setText("");
                    }
                });
    }

    private void loadArtists() {
        try {
            List<Artist> artists = musicService.getAllArtists();
            allArtists.setAll(artists);
        } catch (RepositoryException e) {
            showError("Eroare la incarcarea artistilor: " + e.getMessage());
        }
    }

    private void loadAlbumsForArtist(Long artistId) {
        try {
            List<Album> albums = musicService.getAlbumsForArtist(artistId);
            albumTable.setItems(FXCollections.observableArrayList(albums));
        } catch (RepositoryException e) {
            showError("Eroare la incarcarea albumelor: " + e.getMessage());
        }
    }

    @FXML
    private void onAddAlbum() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("Selecteaza un artist mai intai!"); return; }
        openAlbumDialog(null, selected);
    }

    @FXML
    private void onEditAlbum() {
        Album selectedAlbum   = albumTable.getSelectionModel().getSelectedItem();
        Artist selectedArtist = artistTable.getSelectionModel().getSelectedItem();
        if (selectedAlbum == null) { showWarning("Selecteaza un album pentru editare!"); return; }
        openAlbumDialog(selectedAlbum, selectedArtist);
    }

    @FXML
    private void onDeleteAlbum() {
        Album selected = albumTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("Selecteaza un album pentru stergere!"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmare stergere");
        confirm.setHeaderText("Stergi albumul: " + selected.getTitle() + "?");
        confirm.setContentText("Aceasta actiune nu poate fi anulata!");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    musicService.deleteAlbum(selected.getId());
                    Artist artist = artistTable.getSelectionModel().getSelectedItem();
                    if (artist != null) loadAlbumsForArtist(artist.getId());
                } catch (RepositoryException e) {
                    showError("Eroare la stergere: " + e.getMessage());
                }
            }
        });
    }

    /** Reincarca toti artistii si albumele din baza de date */
    @FXML
    private void onRefresh() {
        loadArtists();
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected != null) loadAlbumsForArtist(selected.getId());
    }

    /** Reseteaza campul de cautare si afiseaza toti artistii */
    @FXML
    private void onResetSearch() {
        searchField.clear();
    }

    private void openAlbumDialog(Album album, Artist artist) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getResource(
                            "/org/example/sgbd_proiect_bun_muzica/album-dialog.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(album == null ? "Adauga Album" : "Editeaza Album");
            dialogStage.setScene(new Scene(loader.load()));

            AlbumDialogController controller = loader.getController();
            controller.setData(musicService, album, artist, dialogStage);

            dialogStage.showAndWait();
            if (artist != null) loadAlbumsForArtist(artist.getId());
        } catch (IOException e) {
            showError("Eroare la deschiderea dialogului: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eroare");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atentie");
        alert.setContentText(message);
        alert.showAndWait();
    }
}