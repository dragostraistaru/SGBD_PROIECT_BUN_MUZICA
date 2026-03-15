-- ============================================
-- Script SQL pentru aplicatia Music Manager
-- Baza de date: PostgreSQL
-- ============================================

-- Stergem tabelele daca exista (ordinea conteaza din cauza FK)
DROP TABLE IF EXISTS album_genres;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS albums;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS artists;

-- ============================================
-- Tabel PARINTE: artists
-- ============================================
CREATE TABLE artists (
                         id          SERIAL PRIMARY KEY,
                         name        VARCHAR(100) NOT NULL,
                         country     VARCHAR(100),
                         formed_year INT
);

-- ============================================
-- Tabel COPIL: albums (relatie 1-N cu artists)
-- Un artist poate avea mai multe albume
-- ============================================
CREATE TABLE albums (
                        id           SERIAL PRIMARY KEY,
                        title        VARCHAR(200) NOT NULL,
                        release_year INT          NOT NULL,
                        artist_id    INT          NOT NULL,
                        CONSTRAINT fk_artist FOREIGN KEY (artist_id) REFERENCES artists(id) ON DELETE CASCADE
);

-- ============================================
-- Tabel pentru relatia M-N: genres <-> albums
-- ============================================
CREATE TABLE genres (
                        id   SERIAL PRIMARY KEY,
                        name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE album_genres (
                              album_id INT NOT NULL,
                              genre_id INT NOT NULL,
                              PRIMARY KEY (album_id, genre_id),
                              CONSTRAINT fk_album FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE,
                              CONSTRAINT fk_genre FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

-- ============================================
-- Date de exemplu: minim 3 parinti, 5-10 copii
-- ============================================

-- Artisti (parinti)
INSERT INTO artists (name, country, formed_year) VALUES
                                                     ('Pink Floyd',    'UK',      1965),
                                                     ('Radiohead',     'UK',      1985),
                                                     ('Daft Punk',     'France',  1993),
                                                     ('The Beatles',   'UK',      1960),
                                                     ('Massive Attack','UK',      1988);

-- Albume (copii) - legate de artisti
INSERT INTO albums (title, release_year, artist_id) VALUES
                                                        -- Pink Floyd (id=1)
                                                        ('The Dark Side of the Moon', 1973, 1),
                                                        ('Wish You Were Here',        1975, 1),
                                                        ('The Wall',                  1979, 1),

                                                        -- Radiohead (id=2)
                                                        ('OK Computer',               1997, 2),
                                                        ('Kid A',                     2000, 2),
                                                        ('In Rainbows',               2007, 2),

                                                        -- Daft Punk (id=3)
                                                        ('Homework',                  1997, 3),
                                                        ('Discovery',                 2001, 3),
                                                        ('Random Access Memories',    2013, 3),

                                                        -- The Beatles (id=4)
                                                        ('Abbey Road',                1969, 4),
                                                        ('Sgt. Peppers',              1967, 4);

-- Genuri
INSERT INTO genres (name) VALUES
                              ('Rock'),
                              ('Progressive Rock'),
                              ('Electronic'),
                              ('Alternative'),
                              ('Pop');

-- Relatia M-N: albume <-> genuri
INSERT INTO album_genres (album_id, genre_id) VALUES
                                                  (1, 2), -- Dark Side -> Progressive Rock
                                                  (1, 1), -- Dark Side -> Rock
                                                  (2, 2), -- Wish You Were Here -> Progressive Rock
                                                  (3, 1), -- The Wall -> Rock
                                                  (4, 4), -- OK Computer -> Alternative
                                                  (5, 4), -- Kid A -> Alternative
                                                  (5, 3), -- Kid A -> Electronic
                                                  (7, 3), -- Homework -> Electronic
                                                  (8, 3), -- Discovery -> Electronic
                                                  (9, 3), -- Random Access Memories -> Electronic
                                                  (10, 1),-- Abbey Road -> Rock
                                                  (11, 5);-- Sgt. Peppers -> Pop