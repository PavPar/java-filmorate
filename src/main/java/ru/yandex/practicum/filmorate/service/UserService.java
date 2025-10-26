package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFeed;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class UserService {
    private final UserStorage storage;

    public UserService(@Qualifier("userDbStorage") UserStorage storage) {
        this.storage = storage;
    }

    public Collection<User> addFriend(long userId, long friendId) throws ValidationException {
        return storage.addFriend(userId, friendId);
    }

    public Collection<User> removeFriend(long userId, long friendId) throws ValidationException {
        return storage.removeFriend(userId, friendId);
    }

    public List<User> getUserFriends(long userId) {
        return storage.getUserFriends(userId);
    }

    public Collection<User> getCommonFriends(long userId, long otherUserId) {
        return storage.getCommonFriends(userId, otherUserId);
    }

    public Collection<User> getAllUsers() {
        return storage.getAllUsers();
    }

    public User addUser(@Valid User user) {
        if (Objects.isNull(user.getName()) || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return storage.addUser(user);
    }

    public User updateUser(@Valid User user) {
        return storage.updateUser(user);
    }

    public Collection<UserFeed> getUserFeed(long id) {
        getUserById(id);
        return storage.getUserFeed(id);
    }


    public void deleteUser(long id) {
        storage.deleteUser(id);
    }

    public User getUserById(long id) {
        return storage.getUser(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}
