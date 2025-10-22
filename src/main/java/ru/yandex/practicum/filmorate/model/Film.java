package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import ru.yandex.practicum.filmorate.anotations.DateAfter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Builder(toBuilder = true)
@Data
public class Film {
    private long id;
    @NotBlank
    @NotNull
    private String name;
    @Size(max = 200)
    private String description;
    @NotNull
    @DateAfter(message = "Дата не может быть ранее чем 28-12-1895", datestring = "28-12-1895")
    private LocalDate releaseDate;
    @NotNull
    @Positive
    private long duration;
    @Getter(AccessLevel.NONE)
    @Builder.Default()
    private Set<Long> likes = new HashSet<>();
    private Mpa mpa;
    private List<Genre> genres;
    private Set<Director> directors = new HashSet<>();

    public Set<Long> getLikes() {
        if (Objects.isNull(likes)) {
            likes = new HashSet<>();
        }
        return likes;
    }

    public Set<Director> getDirectors() {
        if (Objects.isNull(directors)) {
            directors = new HashSet<>();
        }
        return directors;
    }

    public void addGenre(Genre genre) {
        genres.add(genre);
    }

}
