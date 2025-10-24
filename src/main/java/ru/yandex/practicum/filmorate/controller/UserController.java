package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserFeed;
import ru.yandex.practicum.filmorate.service.FilmLikeService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    UserService service;
    FilmService filmService;
    FilmLikeService filmLikeService;

    @GetMapping
    public Collection<User> getAllUsers() {
        return service.getAllUsers();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) throws ValidationException {
        return service.addUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User userForUpdate) {
        return service.updateUser(userForUpdate);
    }

    @PutMapping("{id}/friends/{friendId}")
    Collection<User> addFriend(@PathVariable long id, @PathVariable long friendId) throws ValidationException {
        return service.addFriend(id, friendId);
    }

    @DeleteMapping("{id}/friends/{friendId}")
    Collection<User> deleteFriend(@PathVariable long id, @PathVariable long friendId) throws ValidationException {
        return service.removeFriend(id, friendId);
    }

    @GetMapping("{id}/friends")
    List<User> getUserFriendsList(@PathVariable long id) {
        return service.getUserFriends(id);
    }

    @GetMapping("{id}/friends/common/{otherId}")
    Collection<User> getUserCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        return service.getCommonFriends(id, otherId);
    }

    @GetMapping("{id}/feed")
    Collection<UserFeed> getUserFeed(@PathVariable long id) {
        return service.getUserFeed(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id) {
        service.deleteUser(id);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        return service.getUserById(id);
    }

    @GetMapping("{id}/recommendations")
    Collection<Film> getUserFilmRecommendations(@PathVariable long id) {
        return filmService.getFilmsByIds(filmLikeService.getFilmRecommendationsForUser(id));
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(final NotFoundException e) {
        return Map.of(HttpStatus.NOT_FOUND.toString(), e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationFail(final ValidationException e) {
        return Map.of(HttpStatus.BAD_REQUEST.toString(), e.getMessage());
    }
}
