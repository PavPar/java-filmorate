package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFeed;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository extends BaseRepository<User> {
    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM PUBLIC.\"user\"";
    private static final String FIND_ONE_USER_QUERY = "SELECT * FROM PUBLIC.\"user\" WHERE id = ?";
    private static final String UPDATE_USER_QUERY = "UPDATE PUBLIC.\"user\" SET LOGIN=?, NAME=?, BIRTHDAY=?, EMAIL=? WHERE ID=?";
    private static final String ADD_USER_QUERY = "INSERT INTO PUBLIC.\"user\"(LOGIN, NAME, BIRTHDAY, EMAIL)" +
            "VALUES (?, ?, ?, ?)";
    private static final String GET_FRIENDS_QUERY = "SELECT * \n" +
            "FROM PUBLIC.\"user\" u \n" +
            "WHERE u.id IN (\n" +
            "SELECT uf.FRIEND_ID\n" +
            "FROM PUBLIC.\"user_friend\" uf \n" +
            "WHERE uf.USER_ID = ? AND uf.IS_ACCEPTED = true\n" +
            ")";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO PUBLIC.\"user_friend\"\n" +
            "(USER_ID, FRIEND_ID, IS_ACCEPTED)\n" +
            "VALUES(?, ?, TRUE);"+USER_FEED_QUERY;
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM PUBLIC.\"user_friend\"\n" +
            "WHERE  USER_ID=? AND FRIEND_ID=?;"
            +USER_FEED_QUERY;
    private static final String GET_COMMON_FRIENDS = "SELECT * \n" +
            "FROM PUBLIC.\"user\" u \n" +
            "WHERE u.id IN (\n" +
            "SELECT uf.FRIEND_ID\n" +
            "FROM PUBLIC.\"user_friend\" uf \n" +
            "WHERE uf.USER_ID = ? AND uf.IS_ACCEPTED = TRUE\n" +
            "INTERSECT \n" +
            "SELECT uf.FRIEND_ID\n" +
            "FROM PUBLIC.\"user_friend\" uf \n" +
            "WHERE uf.USER_ID = ? AND uf.IS_ACCEPTED = TRUE\n" +
            ")";
    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper, User.class);
    }

    public List<User> findAll() {
        return findMany(FIND_ALL_USERS_QUERY);
    }

    public Optional<User> findOne(long id) {
        return this.findOne(FIND_ONE_USER_QUERY, id);
    }

    public User save(User user) {
        long id = insert(
                ADD_USER_QUERY,
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getEmail()
        );
        user.setId(id);
        return user;
    }

    public User update(User user) {
        this.update(
                UPDATE_USER_QUERY,
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getEmail(),
                user.getId()
        );

        return user;
    }

    public List<User> getFriends(long id) {
        return findMany(GET_FRIENDS_QUERY, id);
    }

    public Collection<User> addFriend(long userId, long friendId) {
        insertNoKey(ADD_FRIEND_QUERY, userId, friendId, userId, EventType.FRIEND.name(), Operation.ADD.name(), friendId);

        return getFriends(userId);
    }

    public Collection<User> removeFriend(long userId, long friendId) {
        delete(DELETE_FRIEND_QUERY, userId, friendId, userId, EventType.FRIEND.name(), Operation.REMOVE.name(), friendId);
        return getFriends(userId);
    }

    public Collection<User> getCommonFriends(long userId, long friendId) {
        return findMany(GET_COMMON_FRIENDS, userId, friendId);
    }

}
