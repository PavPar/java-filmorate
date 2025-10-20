package ru.yandex.practicum.filmorate.storage.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFeed;

import java.util.*;
import java.util.stream.Collectors;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private HashMap<Long, User> users = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(InMemoryUserStorage.class);

    @Override
    public User addUser(User user) {
        user.setId(getNextId());
        if (Objects.isNull(user.getName()) || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (Objects.isNull(user.getId()) || !users.containsKey(user.getId())) {
            log.error("Пользователь не найден");
            throw new NotFoundException("Пользователь  не найден");
        }

        User currentUser = users.get(user.getId());
        users.put(currentUser.getId(), user);

        return users.get(user.getId());
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public Optional<User> getUser(long id) {
        return Optional.of(users.get(id));
    }


    public Collection<User> addFriend(User user, User friend) throws ValidationException {
        if (user.getId() == friend.getId()) {
            throw new ValidationException("cant be friend with self");
        }
        user.getFriends().add(friend.getId());
        friend.getFriends().add(user.getId());

        return List.of(user, friend);
    }

    @Override
    public Collection<User> addFriend(long userId, long friendId) throws ValidationException {
        Optional<User> user = getUser(userId);
        Optional<User> friend = getUser(friendId);

        if (user.isEmpty() || friend.isEmpty()) {
            throw new NotFoundException("Пользователь  не найден");
        }

        return addFriend(user.get(), friend.get());
    }

    public Collection<User> removeFriend(User user, User friend) throws ValidationException {
        user.getFriends().remove(friend.getId());
        friend.getFriends().remove(user.getId());

        return List.of(user, friend);
    }

    public Collection<User> removeFriend(long userId, long friendId) throws ValidationException {

        Optional<User> user = getUser(userId);
        Optional<User> friend = getUser(friendId);

        if (user.isEmpty() || friend.isEmpty()) {
            throw new NotFoundException("Пользователь  не найден");
        }


        return removeFriend(user.get(), friend.get());
    }

    public List<User> getUserFriends(User user) {
        return getAllUsers().stream().filter(u -> user.getFriends().contains(u.getId())).collect(Collectors.toList());
    }

    @Override
    public List<User> getUserFriends(long userId) {

        Optional<User> user = getUser(userId);

        if (user.isEmpty()) {
            throw new NotFoundException("Пользователь  не найден");
        }

        return getUserFriends(user.get());
    }

    public Collection<User> getCommonFriends(User user, User otherUser) {
        Set<User> firstUserFriends = new HashSet<>(getUserFriends(user));
        return getUserFriends(otherUser).stream().filter(firstUserFriends::contains).toList();
    }

    @Override
    public Collection<User> getCommonFriends(long userId, long otherUserId) {

        Optional<User> user = getUser(userId);
        Optional<User> otherUser = getUser(otherUserId);

        if (user.isEmpty() || otherUser.isEmpty()) {
            throw new NotFoundException("Пользователь  не найден");
        }


        return getCommonFriends(user.get(), otherUser.get());
    }

    @Override
    public Collection<UserFeed> getUserFeed(long id){
        throw new NotFoundException("Метод getUserFeed не реализован для inMemoryUserStorage");
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
