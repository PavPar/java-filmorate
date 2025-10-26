MERGE INTO PUBLIC."mpa"
  KEY(ID)
VALUES (1, 'G'),
  (2, 'PG'),
  (3, 'PG-13'),
  (4, 'R'),
  (5, 'NC-17');

MERGE INTO PUBLIC."genre"
  KEY(ID)
VALUES (1, 'Комедия'),
  (2, 'Драма'),
  (3, 'Мультфильм'),
  (4, 'Триллер'),
  (5, 'Документальный'),
  (6, 'Боевик');

INSERT INTO PUBLIC."user" (birthday, email, login, name, birthday)
VALUES ('email@mail.ru', 'user1', 'test1', '1990-04-15');

INSERT INTO PUBLIC."user" (birthday, email, login, name, birthday)
VALUES ('email2@mail.ru', 'user2', 'test2', '1992-04-15');

