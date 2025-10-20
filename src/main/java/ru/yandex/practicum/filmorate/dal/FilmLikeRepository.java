package ru.yandex.practicum.filmorate.dal;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FilmLike;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FilmLikeRepository extends BaseRepository<FilmLike> {
    private static final String LIKE_FILM_QUERY = "INSERT INTO PUBLIC.\"user_film_like\"\n" +
            "(FILM_ID, USER_ID)\n" +
            "VALUES(?, ?);"
            +USER_FEED_QUERY;
    private static final String DELETE_FILM_LIKE_QUERY = "DELETE FROM PUBLIC.\"user_film_like\"\n" +
            "WHERE FILM_ID=? AND USER_ID=?;"
            +USER_FEED_QUERY;
    private static final String GET_FILM_LIKES = "\n" +
            "SELECT film_id,user_id\n" +
            "FROM PUBLIC.\"user_film_like\" ufl \n" +
            "WHERE ufl.FILM_ID = ?";

    public FilmLikeRepository(JdbcTemplate jdbc, RowMapper<FilmLike> mapper) {
        super(jdbc, mapper, FilmLike.class);
    }

    public void likeFilm(long filmId, long userId) {
        this.insertNoKey(LIKE_FILM_QUERY, filmId, userId, userId, EventType.LIKE.name(), Operation.ADD.name(), filmId);
    }

    public void dislikeFilm(long filmId, long userId) {
        this.delete(DELETE_FILM_LIKE_QUERY, filmId, userId, userId, EventType.LIKE.name(), Operation.REMOVE.name(), filmId);
    }

    public Set<Long> getFilmLikes(long filmId) {
        return this.findMany(GET_FILM_LIKES, filmId).stream().map(FilmLike::getUserId).collect(Collectors.toSet());
    }
}
