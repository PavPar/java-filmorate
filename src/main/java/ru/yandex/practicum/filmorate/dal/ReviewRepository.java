package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepository extends BaseRepository<Review> {

    private static final String GET_ALL_QUERY = """
            SELECT * FROM (
            SELECT review_id,content,is_positive,user_id,film_id,
            COALESCE((
            SELECT SUM(CASE
                    WHEN rl.IS_POSITIVE THEN 1
                    ELSE -1
                END) AS useful
            FROM PUBLIC."review_like" rl
            WHERE REVIEW_ID = r.review_id
            GROUP BY rl.REVIEW_ID
            ),0) AS useful
            FROM PUBLIC."review" r
            )
            WHERE film_id = ?
            ORDER BY useful DESC
            LIMIT ?
            """;

    private static final String GET_ONE_QUERY = """
             SELECT * FROM (
                        SELECT review_id,content,is_positive,user_id,film_id,
                        COALESCE((
                        SELECT SUM(CASE
                                WHEN rl.IS_POSITIVE THEN 1
                                ELSE -1
                            END) AS useful
                        FROM PUBLIC."review_like" rl
                        WHERE REVIEW_ID = r.review_id
                        GROUP BY rl.REVIEW_ID
                        ),0) AS useful
                        FROM PUBLIC."review" r
                        )
            WHERE review_id = ?
            """;

    private static final String ADD_QUERY = """
            INSERT INTO PUBLIC.\"review\" (content, is_positive, user_id, film_id)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE_QUERY = """
            UPDATE PUBLIC.\"review\"
            SET content = ?, is_positive = ?
            WHERE review_id = ?
            """;

    private static final String DELETE_QUERY = """
            DELETE FROM PUBLIC.\"review\"
            WHERE review_id = ?
            """;

    private static final String INSERT_LIKE = """
            INSERT INTO PUBLIC.\"review_like\" (review_id, user_id, is_positive)
            VALUES (?, ?, ?)
            """;

    private static final String DELETE_LIKE = """
            DELETE FROM PUBLIC.\"review_like\"
            WHERE review_id = ? AND user_id = ?
            """;

    private static final String CHECK_LIKE_EXISTS = """
            SELECT COUNT(*) FROM PUBLIC.\"review_like\"
            WHERE review_id = ? AND user_id = ?
            """;

    private static final String GET_LIKE_TYPE = """
            SELECT is_positive FROM PUBLIC.\"review_like\"
            WHERE review_id = ? AND user_id = ?
            """;
    private static final String UPDATE_LIKE_QUERY = """
            UPDATE PUBLIC."review_like"
            SET IS_POSITIVE=?
            WHERE REVIEW_ID=? AND USER_ID=?;
            """;

    public ReviewRepository(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper, Review.class);
    }

    public Optional<Review> findById(Long id) {
        return findOne(GET_ONE_QUERY, id);
    }

    public List<Review> findAll(Long filmId, int count) {
        return findMany(GET_ALL_QUERY, filmId, count);
    }

    public Optional<Review> create(Review review) {
        long id = insert(ADD_QUERY, review.getContent(), review.getIsPositive(), review.getUserId(), review.getFilmId());
        return findById(id);
    }

    public Review update(Review review) {
        update(UPDATE_QUERY, review.getContent(), review.getIsPositive(), review.getReviewId());
        return findById(review.getReviewId()).orElseThrow(() -> new InternalServerException("Failed to update review with ID: " + review.getReviewId()));
    }

    public void delete(Long id) {
        update(DELETE_QUERY, id);
    }

    public boolean hasUserVoted(Long reviewId, Long userId) {
        Integer count = jdbc.queryForObject(CHECK_LIKE_EXISTS, Integer.class, reviewId, userId);
        return count != null && count > 0;
    }

    public void addVote(Long reviewId, Long userId, boolean isPositive) {
        if (hasUserVoted(reviewId, userId)) {
            update(UPDATE_LIKE_QUERY, isPositive, reviewId, userId);
            return;
        }
        update(INSERT_LIKE, reviewId, userId, isPositive);
    }

    public void removeVote(Long reviewId, Long userId) {
        Boolean isPositive = jdbc.query(GET_LIKE_TYPE, rs -> {
            if (rs.next()) return rs.getBoolean("is_positive");
            return null;
        }, reviewId, userId);

        if (isPositive == null) return;

        update(DELETE_LIKE, reviewId, userId);
    }
}
