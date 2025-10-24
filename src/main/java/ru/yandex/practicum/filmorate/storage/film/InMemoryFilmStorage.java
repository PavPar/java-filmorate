package ru.yandex.practicum.filmorate.storage.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.util.DirectorFilmSortValues;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private HashMap<Long, Film> films = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(InMemoryFilmStorage.class);

    @Override
    public Film addFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> getFilms() {
        return this.films.values();
    }

    @Override
    public Optional<Film> getFilm(long id) {
        return Optional.of(films.get(id));
    }


    @Override
    public Film updateFilm(Film film) {
        if (Objects.isNull(film.getId()) || !films.containsKey(film.getId())) {
            log.error("Фильм не найден");
            throw new NotFoundException("Фильм не найден");
        }

        Film currentFilmValue = films.get(film.getId());
        films.put(currentFilmValue.getId(), film);

        return films.get(film.getId());
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public void likeFilm(long filmId, long userId) {
        Optional<Film> foundFilm = getFilm(filmId);
        if (foundFilm.isEmpty()) {
            throw new NotFoundException("Not found");
        }
        Film film = foundFilm.get();
        film.getLikes().add(userId);
    }

    public void dislikeFilm(long filmId, long userId) {
        Optional<Film> foundFilm = getFilm(filmId);
        if (foundFilm.isEmpty()) {
            throw new NotFoundException("Not found");
        }
        Film film = foundFilm.get();
        film.getLikes().remove(userId);
    }

    public Set<Long> getFilmLikes(long filmId) {
        Optional<Film> foundFilm = getFilm(filmId);
        if (foundFilm.isEmpty()) {
            throw new NotFoundException("Not found");
        }
        Film film = foundFilm.get();
        return film.getLikes();
    }

    @Override
    public List<Film> getTopN(int count, int genreId, int year) {
        Collection<Film> films = getFilms();
        return films.stream()
                .sorted((a, b) -> b.getLikes().size() - a.getLikes().size())
                .toList().subList(0, Math.min(films.size(), count));
    }

    @Override
    public List<Film> getFilmsByIds(Collection<Long> ids) {
        Set<Long> idSet = new HashSet<>(ids);

        return films.values().stream().filter((film -> idSet.contains(film.getId()))).collect(Collectors.toList());
    }

    @Override
    public List<Film> getDirectorFilms(Long directorId, DirectorFilmSortValues sortBy) {
        return List.of();
    }

    @Override
    public List<Film> searchFilmsByDirectorOrTitleViaSubstring(String querySubstring, List<String> by) {
        return List.of();
    }

    @Override
    public void deleteFilm(long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм не найден");
        }
        films.remove(id);
    }
}
