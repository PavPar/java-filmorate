package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate")
@Import(FilmLikeService.class)
class FilmLikeServiceTests {

    @Autowired
    private FilmLikeService filmLikeService;

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private FilmLikeDbStorage filmLikeDbStorage;

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Test
    void getUserRecomendationsTestOneMatch() {
        User user1 = User.builder()
                .email("test_1@user.com")
                .login("addAndGetCommonFriends_test1")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("test_2@user.com")
                .login("addAndGetCommonFriends_test2")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Film film1 = Film.builder()
                .name("test-1")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film film2 = Film.builder()
                .name("test-2")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film savedFilm1 = filmDbStorage.addFilm(film1);
        Film savedFilm2 = filmDbStorage.addFilm(film2);

        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);

        assertThat(savedUser1.getId()).isNotNull();
        assertThat(savedUser2.getId()).isNotNull();
        assertThat(savedFilm1.getId()).isNotNull();
        assertThat(savedFilm2.getId()).isNotNull();

        filmLikeDbStorage.likeFilm(savedFilm1.getId(), savedUser1.getId());

        filmLikeDbStorage.likeFilm(savedFilm1.getId(), savedUser2.getId());
        filmLikeDbStorage.likeFilm(savedFilm2.getId(), savedUser2.getId());

        Set<Long> films = filmLikeService.getFilmRecommendationsForUser(user1.getId());
        assertThat(films.size()).isEqualTo(1);
        assertThat(films.contains(savedFilm2.getId())).isTrue();
    }

    @Test
    void getUserRecomendationsTestNoMatches() {
        User user1 = User.builder()
                .email("test_1@user.com")
                .login("addAndGetCommonFriends_test1")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("test_2@user.com")
                .login("addAndGetCommonFriends_test2")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Film film1 = Film.builder()
                .name("test-1")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film film2 = Film.builder()
                .name("test-2")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film savedFilm1 = filmDbStorage.addFilm(film1);
        Film savedFilm2 = filmDbStorage.addFilm(film2);

        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);

        assertThat(savedUser1.getId()).isNotNull();
        assertThat(savedUser2.getId()).isNotNull();
        assertThat(savedFilm1.getId()).isNotNull();
        assertThat(savedFilm2.getId()).isNotNull();

        filmLikeDbStorage.likeFilm(savedFilm1.getId(), savedUser1.getId());

        filmLikeDbStorage.likeFilm(savedFilm2.getId(), savedUser2.getId());

        Set<Long> films = filmLikeService.getFilmRecommendationsForUser(user1.getId());
        assertThat(films.size()).isEqualTo(0);
    }

    @Test
    void getUserRecomendationsTestMultipleUserMatches() {
        User user1 = User.builder()
                .email("test_1@user.com")
                .login("addAndGetCommonFriends_test1")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("test_2@user.com")
                .login("addAndGetCommonFriends_test2")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user3 = User.builder()
                .email("test_3@user.com")
                .login("addAndGetCommonFriends_test3")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Film film1 = Film.builder()
                .name("test-1")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
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
                .description("test-3")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film savedFilm1 = filmDbStorage.addFilm(film1);
        Film savedFilm2 = filmDbStorage.addFilm(film2);
        Film savedFilm3 = filmDbStorage.addFilm(film3);

        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);
        User savedUser3 = userStorage.addUser(user3);

        assertThat(savedUser1.getId()).isNotNull();
        assertThat(savedUser2.getId()).isNotNull();
        assertThat(savedUser3.getId()).isNotNull();

        assertThat(savedFilm1.getId()).isNotNull();
        assertThat(savedFilm2.getId()).isNotNull();
        assertThat(savedFilm3.getId()).isNotNull();

        filmLikeDbStorage.likeFilm(savedFilm1.getId(), savedUser1.getId());

        filmLikeDbStorage.likeFilm(savedFilm1.getId(), savedUser2.getId());
        filmLikeDbStorage.likeFilm(savedFilm2.getId(), savedUser2.getId());

        filmLikeDbStorage.likeFilm(savedFilm1.getId(), savedUser3.getId());
        filmLikeDbStorage.likeFilm(savedFilm3.getId(), savedUser3.getId());

        Set<Long> films = filmLikeService.getFilmRecommendationsForUser(user1.getId());

        assertThat(films.size()).isEqualTo(2);
        assertThat(films.contains(savedFilm2.getId())).isTrue();
        assertThat(films.contains(savedFilm3.getId())).isTrue();
    }

    @Test
    void getUserRecommendationsTestNoUserLikes() {
        User user1 = User.builder()
                .email("test_1@user.com")
                .login("addAndGetCommonFriends_test1")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("test_2@user.com")
                .login("addAndGetCommonFriends_test2")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user3 = User.builder()
                .email("test_3@user.com")
                .login("addAndGetCommonFriends_test3")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        Film film1 = Film.builder()
                .name("test-1")
                .description("test")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
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
                .description("test-3")
                .releaseDate(LocalDate.of(1999, 3, 31))
                .duration(136)
                .mpa(Mpa.builder().id(1).build())
                .build();

        Film savedFilm1 = filmDbStorage.addFilm(film1);
        Film savedFilm2 = filmDbStorage.addFilm(film2);
        Film savedFilm3 = filmDbStorage.addFilm(film3);

        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);
        User savedUser3 = userStorage.addUser(user3);

        assertThat(savedUser1.getId()).isNotNull();
        assertThat(savedUser2.getId()).isNotNull();
        assertThat(savedUser3.getId()).isNotNull();

        assertThat(savedFilm1.getId()).isNotNull();
        assertThat(savedFilm2.getId()).isNotNull();
        assertThat(savedFilm3.getId()).isNotNull();

        filmLikeDbStorage.likeFilm(savedFilm1.getId(), savedUser2.getId());
        filmLikeDbStorage.likeFilm(savedFilm2.getId(), savedUser2.getId());

        filmLikeDbStorage.likeFilm(savedFilm1.getId(), savedUser3.getId());
        filmLikeDbStorage.likeFilm(savedFilm3.getId(), savedUser3.getId());

        Set<Long> films = filmLikeService.getFilmRecommendationsForUser(user1.getId());

        assertThat(films.size()).isEqualTo(0);
    }
}
