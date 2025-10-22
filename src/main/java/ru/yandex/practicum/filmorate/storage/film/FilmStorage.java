package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.util.DirectorFilmSortValues;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);

    Collection<Film> getFilms();

    Optional<Film> getFilm(long id);

    Film updateFilm(Film film);

    List<Film> getTopN(int count, int genreId, int year);

    List<Film> getDirectorFilms(Long directorId, DirectorFilmSortValues sortBy);
}
