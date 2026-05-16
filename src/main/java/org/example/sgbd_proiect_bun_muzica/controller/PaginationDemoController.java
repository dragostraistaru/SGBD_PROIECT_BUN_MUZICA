package org.example.sgbd_proiect_bun_muzica.controller;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.sgbd_proiect_bun_muzica.domain.Album;
import org.example.sgbd_proiect_bun_muzica.repository.AlbumRepositoryORM;
import org.example.sgbd_proiect_bun_muzica.util.IndexBenchmarkDemo;
import org.example.sgbd_proiect_bun_muzica.util.JPAUtil;
import org.example.sgbd_proiect_bun_muzica.util.paging.Page;
import org.example.sgbd_proiect_bun_muzica.util.paging.Pageable;
public class PaginationDemoController {
    @FXML private Label subtitleLabel;
    @FXML private TableView<Album> albumTable;
    @FXML private TableColumn<Album, Long> idColumn;
    @FXML private TableColumn<Album, String> titleColumn;
    @FXML private TableColumn<Album, Integer> yearColumn;
    @FXML private TableColumn<Album, String> artistColumn;
    @FXML private Label loadTimeLabel;
    @FXML private ToggleButton offsetToggle, cursorToggle;
    @FXML private ComboBox<Integer> pageSizeCombo;
    @FXML private Button firstBtn, prevBtn, nextBtn, lastBtn;
    @FXML private Spinner<Integer> pageSpinner;
    @FXML private Label totalPagesLabel, totalLabel, statusLabel;
    private AlbumRepositoryORM albumRepo;
    private Page<Album> currentPage;
    private int currentPageNumber = 0;
    private boolean useOffset = true;
    private Long lastCursorId = null;
    @FXML
    public void initialize() {
        albumRepo = new AlbumRepositoryORM();
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("releaseYear"));
        artistColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getArtist() != null ? cellData.getValue().getArtist().getName() : "N/A"
        ));
        

        pageSizeCombo.setItems(FXCollections.observableArrayList(10, 25, 50, 100));
        pageSizeCombo.setValue(100);
        
        offsetToggle.selectedProperty().addListener((o, old, neu) -> {
            if (neu) {
                useOffset = true;
                currentPageNumber = 0;
                lastCursorId = null;
                loadPage();
            }
        });
        cursorToggle.selectedProperty().addListener((o, old, neu) -> {
            if (neu) {
                useOffset = false;
                currentPageNumber = 0;
                lastCursorId = null;
                loadPage();
            }
        });
        pageSizeCombo.valueProperty().addListener((o, old, neu) -> {
            currentPageNumber = 0;
            lastCursorId = null;
            loadPage();
        });
        pageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        pageSpinner.valueProperty().addListener((o, old, neu) -> {
            if (neu != null && neu > 0) {
                currentPageNumber = neu - 1;
                loadPage();
            }
        });
        offsetToggle.setSelected(true);
        
        new Thread(() -> {
            try {
                IndexBenchmarkDemo indexDemo = new IndexBenchmarkDemo(JPAUtil.getEntityManagerFactory());
                indexDemo.seedUIData();
                
                Platform.runLater(() -> {
                    subtitleLabel.setText(" Date încărcate: 10.000+ albume");
                    loadPage();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    subtitleLabel.setText(" Eroare: " + e.getMessage());
                    loadPage();
                });
            }
        }).start();
    }
    private void loadPage() {
        new Thread(() -> {
            try {
                long start = System.currentTimeMillis();
                Page<Album> page = useOffset ?
                    albumRepo.findAllOffset(new Pageable(currentPageNumber, pageSizeCombo.getValue())) :
                    albumRepo.findAllCursor(new Pageable(currentPageNumber, pageSizeCombo.getValue()), lastCursorId);
                currentPage = page;
                if (!useOffset && !page.getContent().isEmpty()) {
                    lastCursorId = page.getContent().get(page.getContent().size() - 1).getId();
                }
                long time = System.currentTimeMillis() - start;
                Platform.runLater(() -> {
                    albumTable.setItems(FXCollections.observableArrayList(page.getContent()));
                    loadTimeLabel.setText("⏱ " + time + " ms");
                    totalLabel.setText("Total: " + page.getTotalElements());
                    totalPagesLabel.setText(String.valueOf((int) page.getTotalPages()));
                    
                    SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = 
                        (SpinnerValueFactory.IntegerSpinnerValueFactory) pageSpinner.getValueFactory();
                    valueFactory.setMax((int) page.getTotalPages());
                    valueFactory.setValue(currentPageNumber + 1);
                    
                    firstBtn.setDisable(page.isFirst());
                    prevBtn.setDisable(page.isFirst());
                    nextBtn.setDisable(page.isLast());
                    lastBtn.setDisable(page.isLast());
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    @FXML
    private void onFirst() {
        currentPageNumber = 0;
        lastCursorId = null;
        loadPage();
    }
    @FXML
    private void onPrevious() {
        if (currentPageNumber > 0) {
            currentPageNumber--;
            loadPage();
        }
    }
    @FXML
    private void onNext() {
        if (currentPage != null && !currentPage.isLast()) {
            currentPageNumber++;
            loadPage();
        }
    }
    @FXML
    private void onLast() {
        if (currentPage != null) {
            currentPageNumber = (int) currentPage.getTotalPages() - 1;
            loadPage();
        }
    }
    @FXML
    private void onBenchmark() {
        statusLabel.setText(" Benchmark în curs...");
        new Thread(() -> {
            try {
                StringBuilder results = new StringBuilder();
                results.append("BENCHMARK PAGINARE\n");
                results.append("=".repeat(50)).append("\n\n");

                int pageSize = 100;

                results.append("OFFSET/LIMIT (Random Access):\n");
                long offsetTotalTime = 0;
                int[] offsetPages = {0, 49, 99};
                for (int pageNum : offsetPages) {
                    long start = System.currentTimeMillis();
                    albumRepo.findAllOffset(new Pageable(pageNum, pageSize));
                    long time = System.currentTimeMillis() - start;
                    offsetTotalTime += time;
                    results.append("  Pagina ").append(pageNum + 1).append(": ").append(time).append(" ms\n");
                }
                results.append("  TOTAL: ").append(offsetTotalTime).append(" ms\n\n");

                results.append("CURSOR (KEYSET - Sequential):\n");
                long cursorTotalTime = 0;
                Long lastId = null;
                int[] cursorPages = {1, 50, 100};
                
                for (int targetPage : cursorPages) {
                    long pageTime = 0;
                    for (int p = 0; p < targetPage; p++) {
                        long start = System.currentTimeMillis();
                        Page<Album> page = albumRepo.findAllCursor(new Pageable(p, pageSize), p == 0 ? null : lastId);
                        long time = System.currentTimeMillis() - start;
                        
                        if (p == targetPage - 1) {
                            pageTime = time;
                        }
                        
                        if (!page.getContent().isEmpty()) {
                            lastId = page.getContent().get(page.getContent().size() - 1).getId();
                        }
                    }
                    cursorTotalTime += pageTime;
                    results.append("  Pagina ").append(targetPage).append(": ").append(pageTime).append(" ms\n");
                }
                results.append("  TOTAL: ").append(cursorTotalTime).append(" ms\n\n");

                // Rezultat
                double improvement = ((double) (offsetTotalTime - cursorTotalTime) / offsetTotalTime) * 100;
                if (improvement > 0) {
                    results.append(" CURSOR mai rapid cu: ").append(String.format("%.1f%%", improvement));
                } else {
                    results.append(" OFFSET mai rapid cu: ").append(String.format("%.1f%%", Math.abs(improvement)));
                }

                String finalResult = results.toString();
                System.out.println(finalResult);

                Platform.runLater(() -> {
                    statusLabel.setText(" Benchmark completat!");
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Rezultate Benchmark");
                    alert.setHeaderText("Comparație OFFSET vs CURSOR");
                    alert.setContentText(finalResult);
                    alert.showAndWait();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText(" Eroare benchmark: " + e.getMessage());
                });
            }
        }).start();
    }
}