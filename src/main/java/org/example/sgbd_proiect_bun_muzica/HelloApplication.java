package org.example.sgbd_proiect_bun_muzica;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.example.sgbd_proiect_bun_muzica.controller.MainController;
import org.example.sgbd_proiect_bun_muzica.repository.AlbumRepositoryORM;
import org.example.sgbd_proiect_bun_muzica.repository.ArtistRepositoryORM;
import org.example.sgbd_proiect_bun_muzica.service.MusicService;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ArtistRepositoryORM artistRepo = new ArtistRepositoryORM();
        AlbumRepositoryORM albumRepo = new AlbumRepositoryORM();
        MusicService musicService = new MusicService(artistRepo, albumRepo);

        FXMLLoader mainLoader = new FXMLLoader(
                HelloApplication.class.getResource(
                        "/org/example/sgbd_proiect_bun_muzica/main-view.fxml"));
        Tab mainTab = new Tab("Music Manager", mainLoader.load());
        mainTab.setClosable(false);

        MainController mainController = mainLoader.getController();
        mainController.setMusicService(musicService);

        // Tab 2: Pagination Demo
        FXMLLoader paginationLoader = new FXMLLoader(
                HelloApplication.class.getResource(
                        "/org/example/sgbd_proiect_bun_muzica/pagination-view.fxml"));
        Tab paginationTab = new Tab("Paginare Demo", paginationLoader.load());
        paginationTab.setClosable(false);

        TabPane tabPane = new TabPane(mainTab, paginationTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Scene scene = new Scene(tabPane, 1000, 700);

        primaryStage.setTitle("Music Manager - SGBD Optimizare");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}