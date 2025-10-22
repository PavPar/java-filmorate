package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    void delete(Long id);

    Optional<Review> findById(Long id);

    List<Review> findAll(Long filmId, int count);

    void like(Long reviewId, Long userId);

    void dislike(Long reviewId, Long userId);

    void removeLikeOrDislike(Long reviewId, Long userId);
}
