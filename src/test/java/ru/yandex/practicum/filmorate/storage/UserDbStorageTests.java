package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan("ru.yandex.practicum.filmorate")
@Import(UserDbStorage.class)
class UserDbStorageTests {

    private final UserDbStorage userStorage;

    @Autowired
    public UserDbStorageTests(UserDbStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Test
    void addAndFindUser() {
        User newUser = User.builder()
                .email("test@user.com")
                .login("tester")
                .name("Tester")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User savedUser = userStorage.addUser(newUser);

        assertThat(savedUser.getId()).isNotNull();

        Optional<User> found = userStorage.getUser(savedUser.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@user.com");
    }

    @Test
    void addAndGetUserFriends() {
        User user1 = User.builder()
                .email("test_1@user.com")
                .login("test1")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("test_2@user.com")
                .login("test2")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);

        assertThat(savedUser1.getId()).isNotNull();
        assertThat(savedUser2.getId()).isNotNull();

        userStorage.addFriend(savedUser1.getId(), savedUser2.getId());

        List<User> friends1 = userStorage.getUserFriends(savedUser1.getId());
        List<User> friends2 = userStorage.getUserFriends(savedUser1.getId());

        Optional<User> friendo1 = friends1.stream().filter(u -> u.getId() == savedUser2.getId()).findFirst();
        Optional<User> friendo2 = friends2.stream().filter(u -> u.getId() == savedUser1.getId()).findFirst();

        assertTrue(friendo1.isPresent());
        assertTrue(friendo2.isEmpty());
    }

    @Test
    void addAndRemoveUserFriends() {
        User user1 = User.builder()
                .email("test_1@user.com")
                .login("test3")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("test_2@user.com")
                .login("test4")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);

        assertThat(savedUser1.getId()).isNotNull();
        assertThat(savedUser2.getId()).isNotNull();

        userStorage.addFriend(savedUser1.getId(), savedUser2.getId());
        userStorage.removeFriend(savedUser1.getId(), savedUser2.getId());

        List<User> friends1 = userStorage.getUserFriends(savedUser1.getId());
        List<User> friends2 = userStorage.getUserFriends(savedUser2.getId());

        Optional<User> friendo1 = friends1.stream().filter(u -> u.getId() == savedUser2.getId()).findFirst();
        Optional<User> friendo2 = friends2.stream().filter(u -> u.getId() == savedUser1.getId()).findFirst();

        assertTrue(friendo1.isEmpty());
        assertTrue(friendo2.isEmpty());
    }

    @Test
    void addAndGetCommonFriends() {
        User user1 = User.builder()
                .email("test_1@user.com")
                .login("addAndGetCommonFriends_test1")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("test_2@user.com")
                .login("addAndGetCommonFriends_test2")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User user3 = User.builder()
                .email("test_2@user.com")
                .login("addAndGetCommonFriends_test3")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User user4 = User.builder()
                .email("test_2@user.com")
                .login("addAndGetCommonFriends_test4")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User savedUser1 = userStorage.addUser(user1);
        User savedUser2 = userStorage.addUser(user2);
        User savedUser3 = userStorage.addUser(user3);
        User savedUser4 = userStorage.addUser(user4);

        assertThat(savedUser1.getId()).isNotNull();
        assertThat(savedUser2.getId()).isNotNull();
        assertThat(savedUser3.getId()).isNotNull();
        assertThat(savedUser4.getId()).isNotNull();

        userStorage.addFriend(savedUser1.getId(), savedUser3.getId());
        userStorage.addFriend(savedUser2.getId(), savedUser3.getId());
        userStorage.addFriend(savedUser2.getId(), savedUser4.getId());

        Collection<User> commonFriends = userStorage.getCommonFriends(savedUser1.getId(), savedUser2.getId());

        Optional<User> commonFriendo1 = commonFriends.stream().filter(u -> u.getId() == savedUser3.getId()).findFirst();
        Optional<User> commonFriendo2 = commonFriends.stream().filter(u -> u.getId() == savedUser4.getId()).findFirst();

        assertTrue(commonFriendo1.isPresent());
        assertTrue(commonFriendo2.isEmpty());
    }


    @Test
    void updateUserTest() {
        User user1 = User.builder()
                .email("test_1@user.com")
                .login("test3")
                .name("Test")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User savedUser1 = userStorage.addUser(user1);

        assertThat(savedUser1.getId()).isNotNull();
        savedUser1.setName("new-name");
        userStorage.updateUser(savedUser1);
        Optional<User> updatedUser = userStorage.getUser(savedUser1.getId());
        assertTrue(updatedUser.isPresent());
        assertTrue(updatedUser.get().getName().equals("new-name"));
    }

    @Test
    void deleteUserById() {
        User user = User.builder()
                .email("delete@user.com")
                .login("deleteUser")
                .name("To Delete")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User savedUser = userStorage.addUser(user);
        Optional<User> beforeDelete = userStorage.getUser(savedUser.getId());
        assertTrue(beforeDelete.isPresent());

        userStorage.deleteUser(savedUser.getId());

        Optional<User> afterDelete = userStorage.getUser(savedUser.getId());
        assertTrue(afterDelete.isEmpty());
    }
}
