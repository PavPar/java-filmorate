package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.filmDirector.FilmDirectorStorage;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.util.DirectorFilmSortValues;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final DirectorStorage directorStorage;
    private final FilmLikeStorage filmLikeStorage;
    private final FilmDirectorStorage filmDirectorStorage;

    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       GenreStorage genreStorage, MpaStorage mpaStorage, DirectorStorage directorStorage,
                       FilmLikeStorage filmLikeStorage, FilmDirectorStorage filmDirectorStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.directorStorage = directorStorage;
        this.filmLikeStorage = filmLikeStorage;
        this.filmDirectorStorage = filmDirectorStorage;
    }

    public void likeFilm(long filmId, long userId) {
        userStorage.getUser(userId);
        filmLikeStorage.likeFilm(filmId, userId);
    }

    public void dislikeFilm(long filmId, long userId) {
        userStorage.getUser(userId);
        filmLikeStorage.dislikeFilm(filmId, userId);
    }

    public Collection<Film> getTopN(int count, int genreId, int year) {
        return filmStorage.getTopN(count, genreId, year);
    }

    public Collection<Film> getFilmsByIds(Collection<Long> ids) {
        return this.filmStorage.getFilmsByIds(ids);
    }

    public Film updateFilm(@Valid Film film) {
        Film updatedFilm = filmStorage.updateFilm(film);
        if (!Objects.isNull(film.getDirectors())) {
            Set<Long> directorIds = film.getDirectors().stream().map(Director::getId).collect(Collectors.toSet());
            for (long id : directorIds) {
                directorStorage.getDirector(id);
                filmDirectorStorage.addDirector(film.getId(), id);
            }
            updatedFilm.setDirectors(film.getDirectors());
        }
        return updatedFilm;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilm(long id) {
        Optional<Film> foundFilm = filmStorage.getFilm(id);
        if (foundFilm.isEmpty()) {
            throw new NotFoundException("film wasn't found");
        }
        Film film = foundFilm.get();
        film.setGenres(genreStorage.getFilmGenre(film.getId()));
        film.setMpa(mpaStorage.getById(film.getMpa().getId()));
        film.setLikes(filmLikeStorage.getFilmLikes(film.getId()));
        film.setDirectors(filmDirectorStorage.getDirectors(film.getId()));

        return film;
    }

    public Film addFilm(@Valid Film film) {
        mpaStorage.getById(film.getMpa().getId());
        Film addedFilm = filmStorage.addFilm(film);

        if (!Objects.isNull(film.getGenres())) {
            Set<Long> genreIdList = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
            for (long id : genreIdList) {
                genreStorage.getById(id);
            }

            for (long id : genreIdList) {
                genreStorage.addFilmGenre(addedFilm.getId(), id);
            }

            addedFilm.setGenres(genreStorage.getFilmGenre(addedFilm.getId()));
        }

        if (!Objects.isNull(film.getDirectors())) {
            Set<Long> directorIds = film.getDirectors().stream().map(Director::getId).collect(Collectors.toSet());
            for (long id : directorIds) {
                directorStorage.getDirector(id);
                filmDirectorStorage.addDirector(film.getId(), id);
            }
        }

        return addedFilm;
    }

    public Collection<Film> getDirectorsFilm(Long directorId, DirectorFilmSortValues sortBy) {
        List<Film> films = filmStorage.getDirectorFilms(directorId, sortBy);

        films.forEach(film -> {
            film.setDirectors(filmDirectorStorage.getDirectors(film.getId()));
        });

        return films;
    }

    public void deleteFilm(long id) {
        filmStorage.deleteFilm(id);
    }
}
