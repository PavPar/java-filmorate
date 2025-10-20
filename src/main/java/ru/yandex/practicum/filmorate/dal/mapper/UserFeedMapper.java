package ru.yandex.practicum.filmorate.dal.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.UserFeed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;


@Component
public class UserFeedMapper implements RowMapper<UserFeed> {

    @Override
    public UserFeed mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return UserFeed.builder()
                .timestamp(resultSet.getLong("timestamp"))
                .userId(resultSet.getLong("user_id"))
                .eventType(EventType.valueOf(resultSet.getString("event_type")))
                .operation(Operation.valueOf(resultSet.getString("operation")))
                .eventId(resultSet.getLong("event_id"))
                .entityId(resultSet.getLong("entity_id"))
                .build();
    }
}
