package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DirectorStorage {
    Director addDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(long id);

    List<Director> getDirectors();

    Set<Director> getDirectorsViaIds(Set<Long> incomingDirectorIds);

    Optional<Director> getDirector(long id);
}
