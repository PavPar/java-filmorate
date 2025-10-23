package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FilmLike;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmLikeService {
    private final FilmLikeStorage filmLikeStorage;

    @Autowired
    public FilmLikeService(FilmLikeStorage filmLikeStorage) {
        this.filmLikeStorage = filmLikeStorage;
    }

    public Set<Long> getFilmRecommendationsForUser(long id) {
        HashMap<Long, HashMap<Long, Double>> data = this.generateMatrixUserFilm(this.filmLikeStorage.getUsersWithSameFilmLikes(id));
        Set<Long> userLikedAlreadyFilms = data.getOrDefault(id, new HashMap<>())
                .entrySet()
                .stream()
                .filter((entry) -> entry.getValue() != 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        HashMap<Long, HashMap<Long, Double>> diff = new HashMap<>();
        HashMap<Long, HashMap<Long, Integer>> freq = new HashMap<>();
        for (HashMap<Long, Double> user : data.values()) {

            for (Map.Entry<Long, Double> filmEntry : user.entrySet()) {
                Long filmId = filmEntry.getKey();

                if (!diff.containsKey(filmId)) {
                    diff.put(filmId, new HashMap<Long, Double>());
                    freq.put(filmId, new HashMap<Long, Integer>());
                }

                for (Map.Entry<Long, Double> userFilmEntry : user.entrySet()) {
                    Long userFilmId = userFilmEntry.getKey();
                    int oldCount = 0;

                    if (freq.get(filmId).containsKey(userFilmId)) {
                        oldCount = freq.get(filmId).get(userFilmId);
                    }

                    double oldDiff = 0.0;
                    if (diff.get(filmId).containsKey(userFilmId)) {
                        oldDiff = diff.get(filmId).get(userFilmId);
                    }

                    double observedDiff = filmEntry.getValue() - userFilmEntry.getValue();
                    freq.get(filmId).put(userFilmId, oldCount + 1);
                    diff.get(filmId).put(userFilmId, oldDiff + observedDiff);
                }
            }
        }

        for (Long j : diff.keySet()) {
            for (Long i : diff.get(j).keySet()) {
                double oldValue = diff.get(j).get(i);
                int count = freq.get(j).get(i);
                diff.get(j).put(i, oldValue / count);
            }
        }

        Map<Long, Double> uFreq = new HashMap<>();
        Map<Long, Double> uPred = new HashMap<>();
        for (Map.Entry<Long, HashMap<Long, Double>> e : data.entrySet()) {
            for (Long j : e.getValue().keySet()) {
                for (Long k : diff.keySet()) {
                    double predictedValue =
                            diff.get(k).get(j) + e.getValue().get(j);
                    double finalValue = predictedValue * freq.get(k).get(j);
                    uPred.put(k, uPred.getOrDefault(k, 0.0) + finalValue);
                    uFreq.put(k, uFreq.getOrDefault(k, 0.0) + freq.get(k).get(j));
                }
            }
        }

        HashMap<Long, Double> clean = new HashMap<Long, Double>();
        for (Long j : uPred.keySet()) {
            if (uFreq.get(j) > 0 && !userLikedAlreadyFilms.contains(j)) {
                clean.put(j, uPred.get(j) / uFreq.get(j).intValue());
            }
        }
        return clean.keySet();
    }

    private HashMap<Long, HashMap<Long, Double>> generateMatrixUserFilm(Collection<FilmLike> filmLikes) {
        HashMap<Long, HashMap<Long, Double>> result = new HashMap<>();
        Set<Long> films = filmLikes.stream().map(FilmLike::getFilmId).collect(Collectors.toSet());
        Set<Long> users = filmLikes.stream().map(FilmLike::getUserId).collect(Collectors.toSet());
        for (Long userId : users) {
            for (Long filmId : films) {
                result.put(userId, new HashMap<>());
                result.get(userId).put(filmId, 0.0);
            }
        }

        for (FilmLike fl : filmLikes) {
            long userId = fl.getUserId();
            long filmId = fl.getFilmId();

            result.get(userId).put(filmId, 1.0);
        }
        return result;
    }
}
