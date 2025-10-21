package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.util.DirectorFilmSortValues;

import java.util.List;
import java.util.Optional;

@Repository
public class FilmRepository extends BaseRepository<Film> {
    private static final String GET_ALL_FILMS_QUERY = "SELECT * FROM PUBLIC.\"film\"";
    private static final String GET_ONE_FILM_QUERY = "SELECT * FROM PUBLIC.\"film\" WHERE id = ?";
    private static final String ADD_FILM_QUERY = "INSERT INTO PUBLIC.\"film\"\n" +
            "(NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID)\n" +
            "VALUES(?, ?, ?, ?, ?);";
    private static final String UPDATE_FILM_QUERY = "UPDATE PUBLIC.\"film\"\n" +
            "SET NAME=?, DESCRIPTION=?, RELEASE_DATE=?, DURATION=?, MPA_ID=?\n" +
            "WHERE ID=?;";
    private static final String GET_TOP_N_QUERY = "SELECT *\n" +
            "FROM (\n" +
            "SELECT f.id,f.name,f.description,f.RELEASE_DATE,f.DURATION ,f.MPA_ID\n" +
            "FROM PUBLIC.\"film\" f LEFT JOIN (\n" +
            "SELECT film_id,count(user_id) AS user_like_cnt\n" +
            "FROM PUBLIC.\"user_film_like\"\n" +
            "GROUP BY film_id\n" +
            ") lcnt ON f.id = lcnt.film_id \n" +
            "ORDER BY lcnt.USER_LIKE_CNT  DESC\n" +
            ")\n" +
            "LIMIT ?\n";

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
        long id = insert(ADD_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        return getFilm(id);
    }

    public Film updateFilm(Film film) {
        this.update(
                UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        Optional<Film> optionalFilm = getFilm(film.getId());
        if (optionalFilm.isEmpty()) {
            throw new InternalServerException("failed to create film");
        }
        return optionalFilm.get();
    }

    public List<Film> getTopN(int count) {
        return this.findMany(GET_TOP_N_QUERY, count);
    }

    private static final String BASE_FILM_DIRECTOR_QUERY = """
        SELECT f.id AS film_id, f.name, f.description, f.release_date, f.duration,
               f.mpa_id, m.name AS mpa_name,
               COALESCE(lc.user_like_cnt, 0) AS like_count
        FROM PUBLIC.\"film\" AS f
        INNER JOIN PUBLIC.\"mpa\" AS m ON f.mpa_id = m.id
        INNER JOIN PUBLIC.\"film_director\" AS fd ON f.id = fd.film_id
        LEFT JOIN (
            SELECT film_id,count(user_id) AS user_like_cnt
            FROM PUBLIC.\"user_film_like\"
            GROUP BY film_id
        ) AS lc ON f.id = lc.film_id
        WHERE fd.director_id = ?
        """;

    private static final String ORDER_BY_YEAR = " ORDER BY f.release_date ASC";
    private static final String ORDER_BY_LIKES = " ORDER BY like_count DESC";

    public List<Film> getDirectorFilms(long directorId, DirectorFilmSortValues sortBy) {
        String query = BASE_FILM_DIRECTOR_QUERY;
        if (sortBy == DirectorFilmSortValues.year) {
            query += ORDER_BY_YEAR;
        } else if (sortBy == DirectorFilmSortValues.likes) {
            query += ORDER_BY_LIKES;
        }
        return this.findMany(query, directorId);
    }
}
