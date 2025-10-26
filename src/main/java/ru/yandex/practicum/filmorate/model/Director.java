package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Director {
    long id;
    @NotNull
    @NotEmpty
    @NotBlank
    String name;
}
