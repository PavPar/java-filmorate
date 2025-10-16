package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

@Repository
public class FilmRepository extends BaseRepository<Film> {
    private static final String GET_ALL_FILMS_QUERY = "SELECT * FROM PUBLIC.\"film\"";
    private static final String GET_ONE_FILM_QUERY = "SELECT * FROM PUBLIC.\"film\" WHERE id = ?";
    private static final String ADD_FILM_QUERY = "INSERT INTO PUBLIC.\"film\" (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID) VALUES(?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_QUERY = "UPDATE PUBLIC.\"film\" SET NAME=?, DESCRIPTION=?, RELEASE_DATE=?, DURATION=?, MPA_ID=? WHERE ID=?";
    private static final String GET_TOP_N_QUERY = """
            SELECT * FROM (
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id
                FROM PUBLIC."film" f
                LEFT JOIN (
                    SELECT film_id, COUNT(user_id) AS user_like_cnt
                    FROM PUBLIC."user_film_like"
                    GROUP BY film_id
                ) lcnt ON f.id = lcnt.film_id
                ORDER BY lcnt.user_like_cnt DESC
            ) LIMIT ?
            """;
    private static final String GET_COMMON_FILMS_QUERY = """
            SELECT f.*, COUNT(l_all.user_id) AS like_count
            FROM PUBLIC."film" f
            JOIN PUBLIC."user_film_like" l1 ON f.id = l1.film_id AND l1.user_id = ?
            JOIN PUBLIC."user_film_like" l2 ON f.id = l2.film_id AND l2.user_id = ?
            LEFT JOIN PUBLIC."user_film_like" l_all ON f.id = l_all.film_id
            GROUP BY f.id
            ORDER BY like_count DESC, f.id ASC
            """;


    public FilmRepository(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper, Film.class);
    }

    public List<Film> getAll() {
        return findMany(GET_ALL_FILMS_QUERY);
    }

    public Optional<Film> getFilm(long id) {
        return findOne(GET_ONE_FILM_QUERY, id);
    }

    public Optional<Film> addFilm(Film film) {
        long id = insert(ADD_FILM_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId());
        return getFilm(id);
    }

    public Film updateFilm(Film film) {
        update(UPDATE_FILM_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());
        Optional<Film> optionalFilm = getFilm(film.getId());
        if (optionalFilm.isEmpty()) {
            throw new InternalServerException("failed to update film");
        }
        return optionalFilm.get();
    }

    public List<Film> getTopN(int count) {
        return findMany(GET_TOP_N_QUERY, count);
    }

    public List<Film> findCommonFilms(long userId, long friendId) {
        return findMany(GET_COMMON_FILMS_QUERY, userId, friendId);
    }
}
