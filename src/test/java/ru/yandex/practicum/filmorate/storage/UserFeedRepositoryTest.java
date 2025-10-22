package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.UserFeedRepository;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate")
@Import(UserFeedRepository.class)
public class UserFeedRepositoryTest {
    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final FilmLikeStorage filmLikeStorage;
    private final ReviewDbStorage reviewStorage;


    @Autowired
    public UserFeedRepositoryTest(UserDbStorage userDbStorage, FilmDbStorage filmDbStorage,
                                  FilmLikeStorage filmLikeStorage, ReviewDbStorage reviewStorage) {
        this.userDbStorage = userDbStorage;
        this.filmDbStorage = filmDbStorage;
        this.filmLikeStorage = filmLikeStorage;
        this.reviewStorage = reviewStorage;
    }

    @Test
    void filmLike() {
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

        Collection<UserFeed> UserFeedTest1 = userDbStorage.getUserFeed(savedUser1.getId());

        assertNotNull(UserFeedTest1, "UserFeed коллекция не должна быть null");
        assertEquals(1, UserFeedTest1.size(), "Должно быть 1 событие в ленте");

        List<UserFeed> feedList = new ArrayList<>(UserFeedTest1);

        UserFeed event1 = feedList.get(0);

        assertEquals(EventType.LIKE, event1.getEventType(),
                "EventType должен быть LIKE");
        assertEquals(Operation.ADD, event1.getOperation(),
                "Operation должен быть ADD");
        assertEquals(savedFilm1.getId(), event1.getEntityId(),
                "Должен быть правильно заполнен id фильма");

        long moscowTimestamp = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
                .atZone(ZoneId.of("Europe/Moscow"))
                .toInstant()
                .toEpochMilli();
        assertTrue(event1.getTimestamp() > moscowTimestamp,
                "Timestamp должен быть после 1 января 2025 года");

    }

    @Test
    void filmDislike() {
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

        Collection<UserFeed> UserFeedTest1 = userDbStorage.getUserFeed(savedUser1.getId());

        assertNotNull(UserFeedTest1, "UserFeed коллекция не должна быть null");
        assertEquals(2, UserFeedTest1.size(), "Должно быть 2 события в ленте");

        List<UserFeed> feedList = new ArrayList<>(UserFeedTest1);

        UserFeed event1 = feedList.get(1);

        assertEquals(EventType.LIKE, event1.getEventType(),
                "EventType должен быть LIKE");
        assertEquals(Operation.REMOVE, event1.getOperation(),
                "Operation должен быть REMOVE");
        assertEquals(savedFilm1.getId(), event1.getEntityId(),
                "Должен быть правильно заполнен id фильма");

        long moscowTimestamp = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
                .atZone(ZoneId.of("Europe/Moscow"))
                .toInstant()
                .toEpochMilli();
        assertTrue(event1.getTimestamp() > moscowTimestamp,
                "Timestamp должен быть после 1 января 2025 года");


    }

    @Test
    void addUserFriends() {
        User user1 = User.builder()
                .email("test_1@user.com")
                .login("test1")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("test_2@user.com")
                .login("test2")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User savedUser1 = userDbStorage.addUser(user1);
        User savedUser2 = userDbStorage.addUser(user2);

        assertThat(savedUser1.getId()).isNotNull();
        assertThat(savedUser2.getId()).isNotNull();

        userDbStorage.addFriend(savedUser1.getId(), savedUser2.getId());

        Collection<UserFeed> UserFeedTest1 = userDbStorage.getUserFeed(savedUser1.getId());

        assertNotNull(UserFeedTest1, "UserFeed коллекция не должна быть null");
        assertEquals(1, UserFeedTest1.size(), "Должно быть 1 событие в ленте");

        List<UserFeed> feedList = new ArrayList<>(UserFeedTest1);

        UserFeed event1 = feedList.get(0);

        assertEquals(EventType.FRIEND, event1.getEventType(),
                "EventType должен быть FRIEND");
        assertEquals(Operation.ADD, event1.getOperation(),
                "Operation должен быть ADD");
        assertEquals(savedUser2.getId(), event1.getEntityId(),
                "Должен быть правильно заполнен id пользователя");

        long moscowTimestamp = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
                .atZone(ZoneId.of("Europe/Moscow"))
                .toInstant()
                .toEpochMilli();
        assertTrue(event1.getTimestamp() > moscowTimestamp,
                "Timestamp должен быть после 1 января 2025 года");


    }

    @Test
    void removeUserFriends() {
        User user1 = User.builder()
                .email("test_1@user.com")
                .login("test3")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("test_2@user.com")
                .login("test4")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User savedUser1 = userDbStorage.addUser(user1);
        User savedUser2 = userDbStorage.addUser(user2);

        assertThat(savedUser1.getId()).isNotNull();
        assertThat(savedUser2.getId()).isNotNull();

        userDbStorage.addFriend(savedUser1.getId(), savedUser2.getId());
        userDbStorage.removeFriend(savedUser1.getId(), savedUser2.getId());

        Collection<UserFeed> UserFeedTest1 = userDbStorage.getUserFeed(savedUser1.getId());

        assertNotNull(UserFeedTest1, "UserFeed коллекция не должна быть null");
        assertEquals(2, UserFeedTest1.size(), "Должно быть 2 события в ленте");

        List<UserFeed> feedList = new ArrayList<>(UserFeedTest1);

        UserFeed event1 = feedList.get(1);

        assertEquals(EventType.FRIEND, event1.getEventType(),
                "EventType должен быть FRIEND");
        assertEquals(Operation.REMOVE, event1.getOperation(),
                "Operation должен быть REMOVE");
        assertEquals(savedUser2.getId(), event1.getEntityId(),
                "Должен быть правильно заполнен id пользователя");

        long moscowTimestamp = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
                .atZone(ZoneId.of("Europe/Moscow"))
                .toInstant()
                .toEpochMilli();
        assertTrue(event1.getTimestamp() > moscowTimestamp,
                "Timestamp должен быть после 1 января 2025 года");
    }

    @Test
    void createReview() {
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

        Review review = Review.builder()
                .content("Great film")
                .isPositive(true)
                .userId(savedUser1.getId())
                .filmId(savedFilm1.getId())
                .build();

        Review created = reviewStorage.create(review);

        Collection<UserFeed> UserFeedTest = userDbStorage.getUserFeed(savedUser1.getId());

        assertNotNull(UserFeedTest, "UserFeed коллекция не должна быть null");
        assertEquals(1, UserFeedTest.size(), "Должно быть 1 событие в ленте");

        List<UserFeed> feedList = new ArrayList<>(UserFeedTest);

        UserFeed event1 = feedList.get(0);

        assertEquals(EventType.REVIEW, event1.getEventType(),
                "EventType должен быть REVIEW");
        assertEquals(Operation.ADD, event1.getOperation(),
                "Operation должен быть ADD");

        long moscowTimestamp = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
                .atZone(ZoneId.of("Europe/Moscow"))
                .toInstant()
                .toEpochMilli();
        assertTrue(event1.getTimestamp() > moscowTimestamp,
                "Timestamp должен быть после 1 января 2025 года");
    }

    @Test
    void updateReview() {
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

        Review review = Review.builder()
                .content("Great film")
                .isPositive(true)
                .userId(savedUser1.getId())
                .filmId(savedFilm1.getId())
                .build();

        Review created = reviewStorage.create(review);

        created.setContent(created.getContent() + " тест");

        reviewStorage.update(created);

        Collection<UserFeed> UserFeedTest1 = userDbStorage.getUserFeed(savedUser1.getId());

        assertNotNull(UserFeedTest1, "UserFeed коллекция не должна быть null");
        assertEquals(2, UserFeedTest1.size(), "Должно быть 2 события в ленте");

        List<UserFeed> feedList = new ArrayList<>(UserFeedTest1);

        UserFeed event1 = feedList.get(1);

        assertEquals(EventType.REVIEW, event1.getEventType(),
                "EventType должен быть REVIEW");
        assertEquals(Operation.UPDATE, event1.getOperation(),
                "Operation должен быть UPDATE");
        assertEquals(savedFilm1.getId(), event1.getEntityId(),
                "Должен быть правильно заполнен id фильма");

        long moscowTimestamp = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
                .atZone(ZoneId.of("Europe/Moscow"))
                .toInstant()
                .toEpochMilli();
        assertTrue(event1.getTimestamp() > moscowTimestamp,
                "Timestamp должен быть после 1 января 2025 года");
    }

    @Test
    void removeReview() {
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

        Review review = Review.builder()
                .content("Great film")
                .isPositive(true)
                .userId(savedUser1.getId())
                .filmId(savedFilm1.getId())
                .build();

        Review created = reviewStorage.create(review);

        reviewStorage.delete(created.getReviewId());

        Collection<UserFeed> UserFeedTest1 = userDbStorage.getUserFeed(savedUser1.getId());

        assertNotNull(UserFeedTest1, "UserFeed коллекция не должна быть null");
        assertEquals(2, UserFeedTest1.size(), "Должно быть 2 события в ленте");

        List<UserFeed> feedList = new ArrayList<>(UserFeedTest1);

        UserFeed event1 = feedList.get(1);

        assertEquals(EventType.REVIEW, event1.getEventType(),
                "EventType должен быть REVIEW");
        assertEquals(Operation.REMOVE, event1.getOperation(),
                "Operation должен быть REMOVE");
        assertEquals(savedFilm1.getId(), event1.getEntityId(),
                "Должен быть правильно заполнен id фильма");

        long moscowTimestamp = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
                .atZone(ZoneId.of("Europe/Moscow"))
                .toInstant()
                .toEpochMilli();
        assertTrue(event1.getTimestamp() > moscowTimestamp,
                "Timestamp должен быть после 1 января 2025 года");


    }

}
