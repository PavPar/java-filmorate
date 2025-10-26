package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
@Qualifier("InMemoryReviewStorage")
public class InMemoryReviewStorage implements ReviewStorage {
    private final Map<Long, Review> reviews = new HashMap<>();
    private final Map<Long, Set<Long>> likes = new HashMap<>();
    private final Map<Long, Set<Long>> dislikes = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private static final Logger log = LoggerFactory.getLogger(InMemoryReviewStorage.class);

    @Override
    public Review create(Review review) {
        long id = idGenerator.getAndIncrement();
        review.setReviewId(id);
        review.setUseful(0);
        reviews.put(id, review);
        likes.put(id, new HashSet<>());
        dislikes.put(id, new HashSet<>());
        return review;
    }

    @Override
    public Review update(Review review) {
        if (!reviews.containsKey(review.getReviewId())) {
            throw new NotFoundException("Review not found");
        }
        reviews.put(review.getReviewId(), review);
        return review;
    }

    @Override
    public void delete(Long id) {
        if (!reviews.containsKey(id)) {
            throw new NotFoundException("Review not found");
        }
        reviews.remove(id);
        likes.remove(id);
        dislikes.remove(id);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return Optional.ofNullable(reviews.get(id));
    }

    @Override
    public List<Review> findAll(Long filmId, int count) {
        return reviews.values().stream().filter(r -> filmId == null || r.getFilmId().equals(filmId)).sorted(Comparator.comparingInt(Review::getUseful).reversed()).limit(count).collect(Collectors.toList());
    }

    @Override
    public void like(Long reviewId, Long userId) {
        validateReview(reviewId);
        if (likes.get(reviewId).add(userId)) {
            dislikes.get(reviewId).remove(userId);
            updateUseful(reviewId);
        }
    }

    @Override
    public void dislike(Long reviewId, Long userId) {
        validateReview(reviewId);
        if (dislikes.get(reviewId).add(userId)) {
            likes.get(reviewId).remove(userId);
            updateUseful(reviewId);
        }
    }

    @Override
    public void removeLikeOrDislike(Long reviewId, Long userId) {
        validateReview(reviewId);
        boolean removed = likes.get(reviewId).remove(userId) || dislikes.get(reviewId).remove(userId);
        if (removed) {
            updateUseful(reviewId);
        }
    }

    private void updateUseful(Long reviewId) {
        Review review = reviews.get(reviewId);
        int useful = likes.get(reviewId).size() - dislikes.get(reviewId).size();
        review.setUseful(useful);
    }

    private void validateReview(Long reviewId) {
        if (!reviews.containsKey(reviewId)) {
            throw new NotFoundException("Review not found");
        }
    }
}
