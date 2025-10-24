package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.extractors.FilmWithItemsExtractor;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.util.DirectorFilmSortValues;

import java.util.Collections;
import java.util.Collection;
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
    private static final String GET_TOP_N_QUERY_BASE = "SELECT \n" +
            "films.id AS film_id, \n" +
            "films.name, \n" +
            "films.description, \n" +
            "films.releaseDate, \n" +
            "films.duration, \n" +
            "films.mpa_id, \n" +
            "films.mpa_name, \n" +
            "films.genre_id, \n" +
            "films.genre_name \n" +
            "FROM (\n" +
            "SELECT f.id,f.name,f.description,f.RELEASE_DATE AS releaseDate,f.DURATION ,f.MPA_ID,\n" +
            "m.name AS mpa_name, g.id AS genre_id, g.name AS genre_name\n" +
            "FROM PUBLIC.\"film\" f LEFT JOIN (\n" +
            "SELECT film_id,count(user_id) AS user_like_cnt\n" +
            "FROM PUBLIC.\"user_film_like\"\n" +
            "GROUP BY film_id\n" +
            ") lcnt ON f.id = lcnt.film_id \n" +
            "LEFT JOIN PUBLIC.\"film_genre\" AS fg ON fg.film_id=f.id \n" +
            "LEFT JOIN PUBLIC.\"mpa\" AS m ON f.mpa_id = m.id \n" +
            "LEFT JOIN PUBLIC.\"genre\" AS g ON fg.genre_id = g.id \n" +
            "WHERE (EXTRACT(YEAR FROM release_date) = ? OR ?)  AND (fg.genre_id = ?  OR ?) \n" +
            "ORDER BY lcnt.USER_LIKE_CNT  DESC\n" +
            ") films \n";

    private static final String GET_TOP_N_QUERY_LIMIT = GET_TOP_N_QUERY_BASE + " LIMIT ?\n";
    private static final String DELETE_FILM_QUERY = "DELETE FROM PUBLIC.\"film\" WHERE id = ?";

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


    private static final String SEARCH_FILMS_BY_POPULARITY =
            "SELECT f.id AS film_id, f.name, f.description, f.release_date AS releaseDate, f.duration, f.mpa_id, m.name AS mpa_name, " +
                    "g.id AS genre_id, g.name AS genre_name, " +
                    "d.id AS director_id, d.name AS director_name " +
                    "FROM PUBLIC.\"film\" f " +
                    "LEFT JOIN PUBLIC.\"mpa\" m ON f.mpa_id = m.id " +
                    "LEFT JOIN PUBLIC.\"film_genre\" fg ON f.id = fg.film_id " +
                    "LEFT JOIN PUBLIC.\"genre\" g ON fg.genre_id = g.id " +
                    "LEFT JOIN PUBLIC.\"film_director\" fd ON f.id = fd.film_id " +
                    "LEFT JOIN PUBLIC.\"director\" d ON fd.director_id = d.id " +
                    "LEFT JOIN ( " +
                    "    SELECT film_id, COUNT(user_id) AS likes_count " +
                    "    FROM PUBLIC.\"user_film_like\" " +
                    "    GROUP BY film_id " +
                    ") uf ON f.id = uf.film_id " +
                    "ORDER BY COALESCE(uf.likes_count, 0) DESC";

    private static final String SEARCH_FILMS_BY_DIRECTOR_OR_TITLE =
            "SELECT " +
                    "f.id AS film_id, " +
                    "f.name, " +
                    "f.description, " +
                    "f.release_date AS releaseDate, " +
                    "f.duration, " +
                    "f.mpa_id, " +
                    "m.name AS mpa_name, " +
                    "g.id AS genre_id, " +
                    "g.name AS genre_name, " +
                    "d.id AS director_id, " +
                    "d.name AS director_name, " +
                    "COALESCE(COUNT(ul.user_id), 0) AS calculated_likes_count " +
                    "FROM PUBLIC.\"film\" f " +
                    "LEFT JOIN PUBLIC.\"mpa\" m ON f.mpa_id = m.id " +
                    "LEFT JOIN PUBLIC.\"film_genre\" fg ON f.id = fg.film_id " +
                    "LEFT JOIN PUBLIC.\"genre\" g ON fg.genre_id = g.id " +
                    "LEFT JOIN PUBLIC.\"film_director\" fd ON f.id = fd.film_id " +
                    "LEFT JOIN PUBLIC.\"director\" d ON fd.director_id = d.id " +
                    "LEFT JOIN PUBLIC.\"user_film_like\" ul ON f.id = ul.film_id " +
                    "WHERE (" +
                    "(? IS NOT NULL AND LOWER(f.name) LIKE LOWER(CONCAT('%', ?, '%'))) " +
                    "OR " +
                    "(? IS NOT NULL AND LOWER(d.name) LIKE LOWER(CONCAT('%', ?, '%'))) " +
                    ") " +
                    "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, " +
                    "m.name, g.id, g.name, d.id, d.name " +
                    "ORDER BY calculated_likes_count DESC";


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

    public List<Film> getTopN(int count, int genreId, int year) {
        boolean allYear = year < 0;
        boolean allGenre = genreId < 0;

        if (count > 0) {
            return this.findManyExtract(GET_TOP_N_QUERY_LIMIT, new FilmWithItemsExtractor(), year, allYear, genreId, allGenre, count);
        } else {
            return this.findManyExtract(GET_TOP_N_QUERY_BASE, new FilmWithItemsExtractor(), year, allYear, genreId, allGenre);
        }
    }

    public List<Film> getFilmsByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(",", ids.stream().map(id -> "?").toList());
        String sql = "SELECT * FROM PUBLIC.\"film\" WHERE id IN (" + placeholders + ")";
        return findMany(sql, ids.toArray());
    }

    public List<Film> getDirectorFilms(long directorId, DirectorFilmSortValues sortBy) {
        String query = BASE_FILM_DIRECTOR_QUERY;
        if (sortBy == DirectorFilmSortValues.year) {
            query += ORDER_BY_YEAR;
        } else if (sortBy == DirectorFilmSortValues.likes) {
            query += ORDER_BY_LIKES;
        }
        return this.findMany(query, directorId);
    }

    public List<Film> searchFilmsByDirectorOrTitleViaSubstring(String querySubstring, List<String> by) {
        if (querySubstring == null || querySubstring.isBlank() && (by == null || by.isEmpty())) {
            return this.findManyExtract(SEARCH_FILMS_BY_POPULARITY, new FilmWithItemsExtractor());
        }

        boolean searchByTitle = by.contains("title");
        boolean searchByDirector = by.contains("director");

        if (!searchByTitle && !searchByDirector) {
            return Collections.emptyList();
        }

        String titleQuery = searchByTitle ? querySubstring : null;
        String directorQuery = searchByDirector ? querySubstring : null;

        return this.findManyExtract(
                SEARCH_FILMS_BY_DIRECTOR_OR_TITLE,
                new FilmWithItemsExtractor(),
                titleQuery,
                querySubstring,
                directorQuery,
                querySubstring
        );
    }

    public void delete(long id) {
        delete("DELETE FROM PUBLIC.\"user_film_like\" WHERE film_id = ?", id);
        delete("DELETE FROM PUBLIC.\"film_genre\" WHERE film_id = ?", id);
        delete("DELETE FROM PUBLIC.\"film\" WHERE id = ?", id);
    }
}
