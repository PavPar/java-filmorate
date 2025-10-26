package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.UserFeed;

import java.util.Collection;

@Repository
public class UserFeedRepository extends BaseRepository<UserFeed> {
    private static final String GET_USER_FEED = "SELECT \n" +
            "uf.event_id AS  eventId, \n" +
            "uf.entity_id AS entityId, \n" +
            "uf.operation AS operation, \n" +
            "uf.event_type AS eventType, \n" +
            "uf.user_id AS userId, \n" +
            "CAST(EXTRACT(EPOCH FROM uf.created_at) * 1000 AS BIGINT) AS timestamp \n" +
            "FROM PUBLIC.\"user_feed\" uf \n" +
            "WHERE uf.USER_ID = ? \n";

    public UserFeedRepository(JdbcTemplate jdbc, RowMapper<UserFeed> mapper) {
        super(jdbc, mapper, UserFeed.class);
    }

    public Collection<UserFeed> getUserFeed(long userId) {
        return findMany(GET_USER_FEED, userId);
    }

}
