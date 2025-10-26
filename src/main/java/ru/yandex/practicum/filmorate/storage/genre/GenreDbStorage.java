package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Component
public class GenreDbStorage implements GenreStorage {
    private GenreRepository repository;

    @Autowired
    public GenreDbStorage(GenreRepository repo) {
        this.repository = repo;
    }

    @Override
    public List<Genre> getAll() {
        return repository.getAll();
    }

    @Override
    public Genre getById(long id) {
        Optional<Genre> genre = repository.getById(id);
        if (genre.isEmpty()) {
            throw new NotFoundException("genre not found");
        }
        return genre.get();
    }

    @Override
    public List<Genre> getFilmGenre(long id) {
        return repository.getFilmGenre(id);
    }

    @Override
    public void addFilmGenre(long filmId, long genreId) {
        repository.addFilmGenre(filmId, genreId);
    }

    @Override
    public void removeAllFilmGenres(long filmId) {
        repository.removeAllFilmGenres(filmId);
    }
}
