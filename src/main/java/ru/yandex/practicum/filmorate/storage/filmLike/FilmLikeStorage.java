package ru.yandex.practicum.filmorate.storage.filmLike;

import ru.yandex.practicum.filmorate.model.FilmLike;

import java.util.Collection;
import java.util.Set;

public interface FilmLikeStorage {

    void likeFilm(long filmId, long userId);

    void dislikeFilm(long filmId, long userId);

    Set<Long> getFilmLikes(long filmId);

    Collection<FilmLike> getUsersWithSameFilmLikes(long userId);
}
