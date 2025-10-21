package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public class FilmDirector {
    private long filmId;
    private long directorId;
}
