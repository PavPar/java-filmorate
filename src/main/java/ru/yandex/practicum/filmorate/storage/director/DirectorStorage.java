package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    Director addDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(long id);

    List<Director> getDirectors();

    Optional<Director> getDirector(long id);
}
