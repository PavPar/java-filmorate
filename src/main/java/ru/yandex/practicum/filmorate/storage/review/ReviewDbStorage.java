package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Component("reviewDbStorage")
public class ReviewDbStorage implements ReviewStorage {

    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewDbStorage(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Review create(Review review) {
        return reviewRepository.create(review)
                .orElseThrow(() -> new NotFoundException("Review not found after creation"));
    }

    @Override
    public Review update(Review review) {
        return reviewRepository.update(review);
    }

    @Override
    public void delete(Long id) {
        reviewRepository.delete(id);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }

    @Override
    public List<Review> findAll(Long filmId, int count) {
        return reviewRepository.findAll(filmId, count);
    }

    @Override
    public void like(Long reviewId, Long userId) {
        reviewRepository.addVote(reviewId, userId, true);
    }

    @Override
    public void dislike(Long reviewId, Long userId) {
        reviewRepository.addVote(reviewId, userId, false);
    }

    @Override
    public void removeLikeOrDislike(Long reviewId, Long userId) {
        reviewRepository.removeVote(reviewId, userId);
    }
}
