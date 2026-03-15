module org.example.sgbd_proiect_bun_muzica {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens org.example.sgbd_proiect_bun_muzica to javafx.fxml;
    opens org.example.sgbd_proiect_bun_muzica.controller to javafx.fxml;
    opens org.example.sgbd_proiect_bun_muzica.domain to javafx.fxml;

    exports org.example.sgbd_proiect_bun_muzica;
}