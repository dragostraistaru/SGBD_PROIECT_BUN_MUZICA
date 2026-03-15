module org.example.sgbd_proiect_bun_muzica {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.sgbd_proiect_bun_muzica to javafx.fxml;
    exports org.example.sgbd_proiect_bun_muzica;
}