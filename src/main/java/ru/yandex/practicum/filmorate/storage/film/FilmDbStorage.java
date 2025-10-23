package ru.yandex.practicum.filmorate.storage.film;

import com.sun.jdi.InternalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.util.DirectorFilmSortValues;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@Qualifier("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final FilmRepository filmRepository;

    @Autowired
    public FilmDbStorage(FilmRepository filmRepository) {
        this.filmRepository = filmRepository;
    }


    @Override
    public Film addFilm(Film film) {
        Optional<Film> addedFilm = filmRepository.addFilm(film);
        if (addedFilm.isEmpty()) {
            throw new InternalException("film wasn't added");
        }
        return addedFilm.get();
    }

    @Override
    public Collection<Film> getFilms() {
        return filmRepository.getAll();
    }

    @Override
    public Optional<Film> getFilm(long id) {
        Optional<Film> film = filmRepository.getFilm(id);
        if (film.isEmpty()) {
            throw new NotFoundException("film wasn't found");
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        return filmRepository.updateFilm(film);
    }

    @Override
    public List<Film> getTopN(int count, int genreId, int year) {
        return filmRepository.getTopN(count, genreId, year);
    }

    public List<Film> getDirectorFilms(Long directorId, DirectorFilmSortValues sortBy) {
        return filmRepository.getDirectorFilms(directorId, sortBy);
    }

    @Override
    public void deleteFilm(long id) {
        Optional<Film> film = filmRepository.getFilm(id);
        if (film.isEmpty()) {
            throw new NotFoundException("film wasn't found");
        }
        filmRepository.delete(id);
    }

    @Override
    public List<Film> getFilmsByIds(Collection<Long> ids) {
        return filmRepository.getFilmsByIds(ids);
    }
}
