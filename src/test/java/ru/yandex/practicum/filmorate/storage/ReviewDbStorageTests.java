package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate")
@Import(ReviewDbStorage.class)
class ReviewDbStorageTests {

    private final ReviewDbStorage reviewStorage;
    private final JdbcTemplate jdbc;

    @Autowired
    public ReviewDbStorageTests(ReviewDbStorage reviewStorage, JdbcTemplate jdbc) {
        this.reviewStorage = reviewStorage;
        this.jdbc = jdbc;
    }

    @BeforeEach
    void setup() {
        insertMpa(); // mpa_id = 1
        insertUser(1L);
        insertUser(100L);
        insertUser(123L);
        insertUser(200L);
        insertUser(321L);
        insertFilm(1L);
        insertFilm(2L);
    }

    private void insertMpa() {
        jdbc.update("MERGE INTO \"mpa\" (id, name) VALUES (?, ?)", 1, "G");
    }

    private void insertUser(long id) {
        jdbc.update("""
            INSERT INTO "user" (id, login, name, email, birthday)
            VALUES (?, ?, ?, ?, ?)
        """, id, "user" + id, "User " + id, "user" + id + "@mail.com", LocalDate.of(1990, 1, 1));
    }

    private void insertFilm(long id) {
        jdbc.update("""
            INSERT INTO "film" (id, name, description, release_date, duration, mpa_id)
            VALUES (?, ?, ?, ?, ?, ?)
        """, id, "Film " + id, "Description", LocalDate.of(2000, 1, 1), 100, 1);
    }

    private Review createBaseReview() {
        return Review.builder()
                .content("Great film")
                .isPositive(true)
                .userId(1L)
                .filmId(1L)
                .useful(0)
                .build();
    }

    @Test
    void createAndFindReview() {
        Review review = createBaseReview();
        Review created = reviewStorage.create(review);

        Optional<Review> found = reviewStorage.findById(created.getReviewId());
        assertTrue(found.isPresent());
        assertEquals("Great film", found.get().getContent());
    }

    @Test
    void updateReview_shouldModifyContentAndPositivity() {
        Review review = reviewStorage.create(createBaseReview());
        review.setContent("Updated content");
        review.setIsPositive(false);

        Review updated = reviewStorage.update(review);

        assertEquals("Updated content", updated.getContent());
        assertFalse(updated.getIsPositive());
    }

    @Test
    void findReviewById_shouldReturnOptionalReview() {
        Review review = reviewStorage.create(createBaseReview());
        Optional<Review> found = reviewStorage.findById(review.getReviewId());

        assertTrue(found.isPresent());
        assertEquals(review.getContent(), found.get().getContent());
    }

    @Test
    void deleteReview_shouldRemoveReview() {
        Review review = reviewStorage.create(createBaseReview());
        reviewStorage.delete(review.getReviewId());

        Optional<Review> found = reviewStorage.findById(review.getReviewId());
        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_shouldReturnReviewsByFilmId() {
        Review r1 = reviewStorage.create(createBaseReview());
        Review r2 = reviewStorage.create(Review.builder()
                .content("Another review")
                .isPositive(false)
                .filmId(2L)
                .userId(1L)
                .useful(0)
                .build());

        List<Review> film1Reviews = reviewStorage.findAll(1L, 10);
        List<Review> film2Reviews = reviewStorage.findAll(2L, 10);

        assertEquals(1, film1Reviews.size());
        assertEquals(r1.getReviewId(), film1Reviews.get(0).getReviewId());

        assertEquals(1, film2Reviews.size());
        assertEquals(r2.getReviewId(), film2Reviews.get(0).getReviewId());
    }

    @Test
    void like_shouldIncreaseUsefulAndPreventDuplicates() {
        Review review = reviewStorage.create(createBaseReview());

        reviewStorage.like(review.getReviewId(), 100L);
        reviewStorage.like(review.getReviewId(), 100L); // повторный лайк не должен менять useful

        Review updated = reviewStorage.findById(review.getReviewId()).get();
        assertEquals(1, updated.getUseful());
    }

    @Test
    void dislike_shouldDecreaseUsefulAndPreventDuplicates() {
        Review review = reviewStorage.create(createBaseReview());

        reviewStorage.dislike(review.getReviewId(), 200L);
        reviewStorage.dislike(review.getReviewId(), 200L); // повторный дизлайк не должен менять useful

        Review updated = reviewStorage.findById(review.getReviewId()).get();
        assertEquals(-1, updated.getUseful());
    }

    @Test
    void removeLike_shouldAdjustUsefulScore() {
        Review review = reviewStorage.create(createBaseReview());
        reviewStorage.like(review.getReviewId(), 123L);

        reviewStorage.removeLikeOrDislike(review.getReviewId(), 123L);

        Review updated = reviewStorage.findById(review.getReviewId()).get();
        assertEquals(0, updated.getUseful());
    }

    @Test
    void removeDislike_shouldAdjustUsefulScore() {
        Review review = reviewStorage.create(createBaseReview());
        reviewStorage.dislike(review.getReviewId(), 321L);

        reviewStorage.removeLikeOrDislike(review.getReviewId(), 321L);

        Review updated = reviewStorage.findById(review.getReviewId()).get();
        assertEquals(0, updated.getUseful());
    }
}
