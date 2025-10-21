package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FilmDirector;

import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FilmDirectorRepository extends BaseRepository<FilmDirector> {
    private static final String GET_FILM_DIRECTORS_QUERY = "SELECT film_id, director_id FROM PUBLIC.\"film_director\"\n " +
            "WHERE film_id = ?";
    private static final String GET_DIRECTOR_FILMS_QUERY = "SELECT film_id, director_id FROM PUBLIC.\"film_director\"\n " +
            "WHERE director_id = ?";
    private static final String ADD_FILM_DIRECTOR_QUERY = """
            INSERT INTO PUBLIC."film_director"
            (FILM_ID, DIRECTOR_ID)
            VALUES(?, ?);""";
    private static final String DELETE_FILM_DIRECTOR_QUERY = "DELETE FROM PUBLIC.\"user_film_like\"\n" +
            "WHERE FILM_ID=? AND USER_ID=?;";

    public FilmDirectorRepository(JdbcTemplate jdbc, RowMapper<FilmDirector> mapper) {
        super(jdbc, mapper, FilmDirector.class);
    }

    public Set<Long> getDirectorFilmsIds(long directorId) {
        return this.findMany(GET_DIRECTOR_FILMS_QUERY, directorId)
                .stream()
                .map(FilmDirector::getFilmId)
                .collect(Collectors.toSet());
    }

    public Set<Long> getFilmDirectorIds(long filmId) {
        return this.findMany(GET_FILM_DIRECTORS_QUERY, filmId)
                .stream()
                .map(FilmDirector::getDirectorId)
                .collect(Collectors.toSet());
    }

    public void addFilmDirector(long filmId, long directorId) {
        this.insertNoKey(ADD_FILM_DIRECTOR_QUERY, filmId, directorId);
    }

    public void removeFilmDirector(long filmId, long directorId) {
        this.delete(DELETE_FILM_DIRECTOR_QUERY, filmId, directorId);
    }
}
