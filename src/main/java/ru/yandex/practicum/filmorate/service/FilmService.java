package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
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

        if (userStorage.getUser(userId).isEmpty() || filmStorage.getFilm(filmId).isEmpty()) {
            throw new NotFoundException("film/user not found");
        }

        filmLikeStorage.likeFilm(filmId, userId);
    }

    public void dislikeFilm(long filmId, long userId) {
        if (userStorage.getUser(userId).isEmpty() || filmStorage.getFilm(filmId).isEmpty()) {
            throw new NotFoundException("film/user not found");
        }
        filmLikeStorage.dislikeFilm(filmId, userId);
    }

    public Collection<Film> getTopN(int count, int genreId, int year) {
        return filmStorage.getTopN(count, genreId, year);
    }

    public Collection<Film> getFilmsByIds(Collection<Long> ids) {
        return this.filmStorage.getFilmsByIds(ids);
    }

    public Film updateFilm(@Valid Film film) {
        Film currentFilm = getFilm(film.getId());
        Film updatedFilm = filmStorage.updateFilm(film);

        if (!Objects.isNull(film.getDirectors())) {
            Set<Long> newIds = film.getDirectors().stream().map(Director::getId).collect(Collectors.toSet());
            Set<Long> oldIds = currentFilm.getDirectors().stream().map(Director::getId).collect(Collectors.toSet());

            if (!newIds.equals(oldIds)) {
                filmDirectorStorage.removeAllFilmDirectors(film.getId());
                for (long id : newIds) {
                    directorStorage.getDirector(id);
                    filmDirectorStorage.addDirector(updatedFilm.getId(), id);
                }
            }
        }

        if (!Objects.isNull(film.getGenres())) {
            Set<Long> newIds = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
            Set<Long> oldIds = currentFilm.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
            if (!newIds.equals(oldIds)) {
                genreStorage.removeAllFilmGenres(film.getId());
                for (long id : newIds) {
                    genreStorage.getById(id);
                    genreStorage.addFilmGenre(updatedFilm.getId(), id);
                }
            }
        }

        return getFilm(updatedFilm.getId());
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
            Set<Long> incomingDirectorIds = film.getDirectors().stream().map(Director::getId).collect(Collectors.toSet());
            Set<Director> existingStorageDirectors = this.directorStorage.getDirectorsViaIds(incomingDirectorIds);
            this.filmDirectorStorage.addDirectors(addedFilm.getId(), new ArrayList<>(incomingDirectorIds));
            addedFilm.setDirectors(existingStorageDirectors);
        }

        return addedFilm;
    }

    public Collection<Film> getDirectorsFilm(Long directorId, DirectorFilmSortValues sortBy) {
        Collection<Film> films = filmStorage.getDirectorFilms(directorId, sortBy);
        if (films.isEmpty()) {
            throw new NotFoundException("no films");
        }
        return films;
    }

    public List<Film> searchFilmsByDirectorOrTitleViaSubstring(String querySubstring, List<String> by) {
        return this.filmStorage.searchFilmsByDirectorOrTitleViaSubstring(querySubstring, by);
    }

    public void deleteFilm(long id) {
        filmStorage.deleteFilm(id);
    }

    public Collection<Film> getCommonFilms(long userId, long friendId) {
        Optional<User> user = userStorage.getUser(userId);
        Optional<User> friend = userStorage.getUser(friendId);
        if (friend.isEmpty() || user.isEmpty()) {
            throw new NotFoundException("no user/friend");
        }

        Collection<FilmLike> filmLikes = filmLikeStorage.getUsersWithSameFilmLikes(userId);

        Set<Long> commonFilmIdSet = filmLikes.stream()
                .filter(filmLike -> filmLike.getUserId() == friendId)
                .map(FilmLike::getFilmId)
                .collect(Collectors.toSet());

        return this.filmStorage.getFilmsByIdsOrderedByPopularity(commonFilmIdSet);
    }
}
