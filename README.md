# Music Manager

Aplicatie desktop JavaFX pentru gestionarea artistilor si albumelor muzicale, construita cu JDBC pur si PostgreSQL.

## Tehnologii

- Java 23
- JavaFX 21
- JDBC (fara ORM)
- PostgreSQL
- Gradle

---

## Configurarea bazei de date

### 1. Creaza baza de date in PostgreSQL

```sql
CREATE DATABASE musicdb;
```

### 2. Ruleaza schema.sql

In pgAdmin, deschide un Query Tool conectat la `musicdb` si ruleaza continutul fisierului `schema.sql`.

Sau din terminal:

```bash
psql -U postgres -d musicdb -f schema.sql
```

---

## Configurarea conexiunii

Editeaza fisierul `src/main/resources/db.properties`:

```properties
db.url=jdbc:postgresql://localhost:5432/musicdb
db.username=postgres
db.password=parola_ta
```

---

## Rularea aplicatiei

```bash
./gradlew run
```

---

## Structura proiectului

```
src/main/java/org/example/sgbd_proiect_bun_muzica/
├── controller/
│   ├── MainController.java          # Controller fereastra principala
│   └── AlbumDialogController.java   # Controller dialog add/edit album
├── domain/
│   ├── Entity.java                  # Clasa abstracta generica
│   ├── Artist.java                  # Entitate parinte
│   └── Album.java                   # Entitate copil
├── exceptions/
│   └── RepositoryException.java     # Exceptie custom pentru erori DB
├── repository/
│   ├── Repository.java              # Interfata generica CRUD
│   ├── IArtistRepository.java       # Interfata specifica artisti
│   ├── IAlbumRepository.java        # Interfata specifica albume
│   ├── AbstractRepositoryDB.java    # Implementare JDBC comuna
│   ├── ArtistRepositoryDB.java      # DAO concret pentru artisti
│   └── AlbumRepositoryDB.java       # DAO concret pentru albume
├── service/
│   └── MusicService.java            # Logica de business si validare
├── util/
│   ├── DatabaseConfig.java          # Citire db.properties
│   └── ConnectionFactory.java       # Factory pentru conexiuni JDBC
├── HelloApplication.java            # Punct de intrare JavaFX
└── Launcher.java                    # Launcher fix pentru JavaFX

src/main/resources/org/example/sgbd_proiect_bun_muzica/
├── main-view.fxml                   # UI fereastra principala
├── album-dialog.fxml                # UI dialog add/edit
└── style.css                        # Stiluri

src/main/resources/
└── db.properties                    # Configuratie baza de date
```

---

## Functionalitati

- Vizualizare master-detail: artisti (parinte) → albume (copil)
- CRUD complet pe albume (Adauga, Editeaza, Sterge)
- Dialog de confirmare la stergere
- Validare campuri obligatorii
- Cautare/filtrare artisti in timp real
- Sortare pe coloanele tabelului
- Buton Refresh pentru reincarcarea datelor

---

## Design Patterns

| Pattern | Unde |
|---|---|
| Repository Pattern | `Repository<T>`, `IArtistRepository`, `IAlbumRepository` |
| Template Method | `AbstractRepositoryDB` |
| Factory | `ConnectionFactory` |
| Dependency Inversion | `MusicService` depinde de interfete |
