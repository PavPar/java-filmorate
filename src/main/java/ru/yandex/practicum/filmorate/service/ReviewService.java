package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewStorage storage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public ReviewService(@Qualifier("reviewDbStorage") ReviewStorage storage,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.storage = storage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Review create(@Valid Review review) {
        Optional<User> user = userStorage.getUser(review.getUserId());
        Optional<Film> film = filmStorage.getFilm(review.getFilmId());

        if (user.isEmpty()) {
            throw new NotFoundException("user not found");
        }

        if (film.isEmpty()) {
            throw new NotFoundException("film not found");
        }

        return storage.create(review);
    }

    public Review update(Review review) {
        Optional<User> user = userStorage.getUser(review.getUserId());
        Optional<Film> film = filmStorage.getFilm(review.getFilmId());

        if (user.isEmpty()) {
            throw new NotFoundException("user not found");
        }

        if (film.isEmpty()) {
            throw new NotFoundException("film not found");
        }
        return storage.update(review);
    }

    public void delete(Long id) {
        storage.delete(id);
    }

    public Review findById(Long id) {
        return storage.findById(id).orElseThrow(() -> new NotFoundException("Review not found"));
    }

    public List<Review> findAll(Long filmId, int count) {
        return storage.findAll(filmId, count);
    }

    public void like(Long reviewId, Long userId) {
        storage.like(reviewId, userId);
    }

    public void dislike(Long reviewId, Long userId) {
        storage.dislike(reviewId, userId);
    }

    public void removeReaction(Long reviewId, Long userId) {
        storage.removeLikeOrDislike(reviewId, userId);
    }
}
