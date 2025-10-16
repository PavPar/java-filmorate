package ru.yandex.practicum.filmorate.storage.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

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
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film updateFilm(Film film) {
        if (Objects.isNull(film.getId()) || !films.containsKey(film.getId())) {
            log.error("Фильм не найден");
            throw new NotFoundException("Фильм не найден");
        }
        films.put(film.getId(), film);
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
    public List<Film> getTopN(int count) {
        return films.values().stream()
                .sorted((a, b) -> b.getLikes().size() - a.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public List<Film> findCommonFilms(long userId, long friendId) {
        return films.values().stream()
                .filter(f -> f.getLikes().contains(userId) && f.getLikes().contains(friendId))
                .sorted((a, b) -> b.getLikes().size() - a.getLikes().size())
                .collect(Collectors.toList());
    }
}
