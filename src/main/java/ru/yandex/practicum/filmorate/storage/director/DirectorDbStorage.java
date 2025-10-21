package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

@Component
@Qualifier("directorDbStorage")
public class DirectorDbStorage implements DirectorStorage {
    private final DirectorRepository directorRepository;

    @Autowired
    public DirectorDbStorage(DirectorRepository directorRepository) {
        this.directorRepository = directorRepository;
    }

    @Override
    public Director addDirector(Director director) {
        return directorRepository.save(director);
    }

    @Override
    public Director updateDirector(Director director) {
        Optional<Director> savedDirector = getDirector(director.getId());

        if (savedDirector.isEmpty()) {
            throw new NotFoundException("Режиссер  не найден");
        }
        return directorRepository.update(director);
    }

    @Override
    public void deleteDirector(long id) {
        directorRepository.remove(id);
    }

    @Override
    public Optional<Director> getDirector(long id) {
        return directorRepository.getById(id);
    }

    @Override
    public List<Director> getDirectors() {
        return directorRepository.getAll();
    }
}
