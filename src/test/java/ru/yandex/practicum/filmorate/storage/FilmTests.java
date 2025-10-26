package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate")
@Import(FilmDbStorage.class)
public class FilmTests {
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final FilmLikeStorage filmLikeStorage;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmTests(FilmDbStorage storage, UserDbStorage userDbStorage, FilmLikeStorage filmLikeStorage, GenreStorage genreStorage) {
        this.filmDbStorage = storage;
        this.userDbStorage = userDbStorage;
        this.filmLikeStorage = filmLikeStorage;
        this.genreStorage = genreStorage;
    }

    @Test
    void addAndFind() {
        Film film = Film.builder()
                .name("test")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film savedFilm = filmDbStorage.addFilm(film);

        assertThat(savedFilm.getId()).isNotNull();

        Optional<Film> found = filmDbStorage.getFilm(savedFilm.getId());
        assertThat(found).isPresent();
    }

    @Test
    void updateFilm() {
        Film film = Film.builder()
                .name("test")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film savedFilm = filmDbStorage.addFilm(film);
        savedFilm.setName("test-updated");
        filmDbStorage.updateFilm(savedFilm);


        Optional<Film> updated = filmDbStorage.getFilm(savedFilm.getId());
        assertThat(updated).isPresent();
        assertTrue(updated.get().getName().equals("test-updated"));
    }

    @Test
    void filmLikeAndGet() {
        Film film1 = Film.builder()
                .name("test-1")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .build();
        User user1 = User.builder()
                .email("test@user.com")
                .login("tester-1")
                .name("Tester")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User savedUser1 = userDbStorage.addUser(user1);
        Film savedFilm1 = filmDbStorage.addFilm(film1);

        filmLikeStorage.likeFilm(savedFilm1.getId(), savedUser1.getId());
        Set<Long> filmLikes = filmLikeStorage.getFilmLikes(savedFilm1.getId());
        assertTrue(filmLikes.contains(savedUser1.getId()));
    }

    @Test
    void filmDislikeAndGet() {
        Film film1 = Film.builder()
                .name("test-1")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .build();
        User user1 = User.builder()
                .email("test@user.com")
                .login("tester-1")
                .name("Tester")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User savedUser1 = userDbStorage.addUser(user1);
        Film savedFilm1 = filmDbStorage.addFilm(film1);

        filmLikeStorage.likeFilm(savedFilm1.getId(), savedUser1.getId());
        Set<Long> filmLikes = filmLikeStorage.getFilmLikes(savedFilm1.getId());
        assertTrue(filmLikes.contains(savedUser1.getId()));

        filmLikeStorage.dislikeFilm(savedFilm1.getId(), savedUser1.getId());
        filmLikes = filmLikeStorage.getFilmLikes(savedFilm1.getId());
        assertFalse(filmLikes.contains(savedUser1.getId()));

    }

    @Test
    void getTopN() {

        List<Genre> lGenre = new ArrayList<Genre>();
        Genre genre = Genre.builder()
                .id(1)
                .name("Комедия")
                .build();
        lGenre.add(genre);

        Film film1 = Film.builder()
                .name("test-1")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .genres(lGenre)
                .build();
        Film film2 = Film.builder()
                .name("test-2")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .build();
        Film film3 = Film.builder()
                .name("test-3")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .genres(lGenre)
                .build();

        User user1 = User.builder()
                .email("test@user.com")
                .login("tester-1")
                .name("Tester")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User user2 = User.builder()
                .email("test@user.com")
                .login("tester-2")
                .name("Tester")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User user3 = User.builder()
                .email("test@user.com")
                .login("tester-3")
                .name("Tester")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User savedUser1 = userDbStorage.addUser(user1);
        User savedUser2 = userDbStorage.addUser(user2);
        User savedUser3 = userDbStorage.addUser(user3);

        Film savedFilm1 = filmDbStorage.addFilm(film1);
        Film savedFilm2 = filmDbStorage.addFilm(film2);
        Film savedFilm3 = filmDbStorage.addFilm(film3);

        filmLikeStorage.likeFilm(savedFilm1.getId(), savedUser1.getId());

        filmLikeStorage.likeFilm(savedFilm2.getId(), savedUser1.getId());
        filmLikeStorage.likeFilm(savedFilm2.getId(), savedUser2.getId());

        filmLikeStorage.likeFilm(savedFilm3.getId(), savedUser1.getId());
        filmLikeStorage.likeFilm(savedFilm3.getId(), savedUser2.getId());
        filmLikeStorage.likeFilm(savedFilm3.getId(), savedUser3.getId());

        List<Film> topN = filmDbStorage.getTopN(3,-1,1999);

        assertTrue(topN.get(0).getId() == savedFilm3.getId());
        assertTrue(topN.get(1).getId() == savedFilm2.getId());
        assertTrue(topN.get(2).getId() == savedFilm1.getId());
    }

    @Test
    void addAndFindFilmGenre() {
        Film film1 = Film.builder()
                .name("test-1")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film savedFilm1 = filmDbStorage.addFilm(film1);

        genreStorage.addFilmGenre(savedFilm1.getId(), 1);
        List<Genre> filmGenre = genreStorage.getFilmGenre(savedFilm1.getId());

        assertTrue(filmGenre.stream().filter(g -> g.getId() == 1).findFirst().get().getId() == 1);
    }

    @Test
    void addMultipleAndFindFilmGenre() {
        Film film1 = Film.builder()
                .name("test-1")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film savedFilm1 = filmDbStorage.addFilm(film1);

        genreStorage.addFilmGenre(savedFilm1.getId(), 1);
        genreStorage.addFilmGenre(savedFilm1.getId(), 2);

        List<Genre> filmGenre = genreStorage.getFilmGenre(savedFilm1.getId());

        assertTrue(filmGenre.stream().filter(g -> g.getId() == 1).findFirst().get().getId() == 1);
        assertTrue(filmGenre.stream().filter(g -> g.getId() == 2).findFirst().get().getId() == 2);
    }

    @Test
    void deleteFilmById() {
        Film film = Film.builder()
                .name("To Delete")
                .description("desc")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film savedFilm = filmDbStorage.addFilm(film);
        Optional<Film> beforeDelete = filmDbStorage.getFilm(savedFilm.getId());
        assertTrue(beforeDelete.isPresent());

        filmDbStorage.deleteFilm(savedFilm.getId());

        assertThrows(NotFoundException.class, () -> filmDbStorage.getFilm(savedFilm.getId()));
    }
}
