package ru.yandex.practicum.filmorate.storage.filmDirector;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.FilmDirectorRepository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FilmDirectorDbStorage implements FilmDirectorStorage {
    private final FilmDirectorRepository repository;
    private final DirectorRepository directorRepository;

    public FilmDirectorDbStorage(FilmDirectorRepository repository, DirectorRepository directorRepository) {
        this.repository = repository;
        this.directorRepository = directorRepository;
    }

    @Override
    public Set<Long> getDirectorFilms(Long directorId) {
        return repository.getDirectorFilmsIds(directorId);
    }

    @Override
    public void addDirector(long filmId, long directorId) {
        repository.addFilmDirector(filmId, directorId);
    }

    @Override
    public void removeDirector(long filmId, long directorId) {
        repository.removeFilmDirector(filmId, directorId);
    }

    @Override
    public Set<Director> getDirectors(long filmId) {
        Set<Long> directorIds = repository.getFilmDirectorIds(filmId);
        Map<Long, Director> directors = directorRepository.getAll().stream()
                .collect(Collectors.toMap(
                        Director::getId,
                        Function.identity()
                ));
        Set<Director> filmDirectors = new HashSet<>();
        directors.forEach((directorId, director) -> {
            if (directorIds.contains(directorId)) {
                filmDirectors.add(director);
            }
        });
        return filmDirectors;
    }
}
