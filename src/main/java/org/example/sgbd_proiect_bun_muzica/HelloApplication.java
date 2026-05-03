package org.example.sgbd_proiect_bun_muzica;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.sgbd_proiect_bun_muzica.controller.MainController;
import org.example.sgbd_proiect_bun_muzica.repository.AlbumRepositoryORM;
import org.example.sgbd_proiect_bun_muzica.repository.ArtistRepositoryORM;
import org.example.sgbd_proiect_bun_muzica.service.MusicService;

/**
 * Punctul de intrare al aplicatiei JavaFX.
 * Instantiaza repository-urile, service-ul si porneste fereastra principala.
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ArtistRepositoryORM artistRepo = new ArtistRepositoryORM();
        AlbumRepositoryORM albumRepo = new AlbumRepositoryORM();
        MusicService musicService = new MusicService(artistRepo, albumRepo);

        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource(
                        "/org/example/sgbd_proiect_bun_muzica/main-view.fxml"));
        Scene scene = new Scene(loader.load(), 750, 600);

        MainController controller = loader.getController();
        controller.setMusicService(musicService);

        primaryStage.setTitle("Music Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}