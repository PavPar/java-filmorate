package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final FilmLikeStorage filmLikeStorage;

    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage, @Qualifier("userDbStorage") UserStorage userStorage, GenreStorage genreStorage, MpaStorage mpaStorage, FilmLikeStorage filmLikeStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.filmLikeStorage = filmLikeStorage;
    }

    public void likeFilm(long filmId, long userId) {
        userStorage.getUser(userId);
        filmLikeStorage.likeFilm(filmId, userId);
    }

    public void dislikeFilm(long filmId, long userId) {
        userStorage.getUser(userId);
        filmLikeStorage.dislikeFilm(filmId, userId);
    }

    public Collection<Film> getTopN(int count) {
        List<Film> films = filmStorage.getTopN(count);
        for (Film film : films) {
            film.setGenres(genreStorage.getFilmGenre(film.getId()));
            film.setMpa(mpaStorage.getById(film.getMpa().getId()));
            film.setLikes(filmLikeStorage.getFilmLikes(film.getId()));
        }
        return films;
    }

    public Collection<Film> getTopN() {
        return filmStorage.getTopN(10);
    }

    public Film updateFilm(@Valid Film film) {
        return filmStorage.updateFilm(film);
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
        return addedFilm;
    }

    public Collection<Film> getCommonFilms(long userId, long friendId) {
        userStorage.getUser(userId);
        userStorage.getUser(friendId);
        List<Film> commonFilms = filmStorage.findCommonFilms(userId, friendId);
        for (Film film : commonFilms) {
            film.setGenres(genreStorage.getFilmGenre(film.getId()));
            film.setMpa(mpaStorage.getById(film.getMpa().getId()));
            film.setLikes(filmLikeStorage.getFilmLikes(film.getId()));
        }
        return commonFilms;
    }
}
