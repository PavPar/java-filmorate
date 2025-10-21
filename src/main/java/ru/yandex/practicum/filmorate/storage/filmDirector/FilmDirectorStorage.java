package ru.yandex.practicum.filmorate.storage.filmDirector;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Set;

public interface FilmDirectorStorage {
    void addDirector(long filmId, long directorId);

    void removeDirector(long filmId, long directorId);

    Set<Director> getDirectors(long filmId);

    Set<Long> getDirectorFilms(Long directorId);
}
