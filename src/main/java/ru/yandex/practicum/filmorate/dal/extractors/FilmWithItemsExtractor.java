package ru.yandex.practicum.filmorate.dal.extractors;

import org.springframework.jdbc.core.ResultSetExtractor;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FilmWithItemsExtractor implements ResultSetExtractor<List<Film>> {
    @Override
    public List<Film> extractData(ResultSet rs) throws SQLException {
        List<Film> films = new ArrayList<>();
        Film currentFilm = null;
        Long currentFilmId = null;

        while (rs.next()) {
            Long filmId = rs.getLong("film_id");
            if (currentFilm == null || !filmId.equals(currentFilmId)) {
                if (currentFilm != null) {
                    films.add(currentFilm);
                }

                currentFilmId = filmId;
                currentFilm = Film.builder()
                        .id(rs.getLong("film_id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .releaseDate(rs.getDate("releaseDate").toLocalDate())
                        .duration(rs.getInt("duration"))
                        .build();
                int mpaId = rs.getInt("mpa_id");
                if (!rs.wasNull()) {
                    Mpa mpa = Mpa.builder().id(mpaId).name(rs.getString("mpa_name")).build();
                    currentFilm.setMpa(mpa);
                }
                currentFilm.setGenres(new ArrayList<>());
                currentFilm.setDirectors(new HashSet<>());
            }
            int genreId = rs.getInt("genre_id");
            if (!rs.wasNull()) {
                Genre genre = Genre.builder()
                        .id(genreId)
                        .name(rs.getString("genre_name"))
                        .build();
                currentFilm.addGenre(genre);
            }

            Long directorId = getLongOrNull(rs, "director_id");
            if (directorId != null) {
                String directorName = this.getStringOrNull(rs, "director_name");
                Director director = Director.builder().id(directorId).name(directorName).build();
                if (!currentFilm.getDirectors().contains(director)) {
                    currentFilm.getDirectors().add(director);
                }
            }
        }
        if (currentFilm != null) {
            films.add(currentFilm);
        }
        return films;
    }

    private Long getLongOrNull(ResultSet rs, String column) {
        try {
            long val = rs.getLong(column);
            return rs.wasNull() ? null : val;
        } catch (SQLException e) {
            return null;
        }
    }

    private String getStringOrNull(ResultSet rs, String columnLabel) {
        try {
            String value = rs.getString(columnLabel);
            return rs.wasNull() ? null : value;
        } catch (SQLException e) {
            return null;
        }
    }
}