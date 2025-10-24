package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class DirectorRepository extends BaseRepository<Director> {
    private static final String GET_ALL_QUERY = "SELECT * FROM PUBLIC.\"director\"";
    private static final String GET_ONE_QUERY = "SELECT * FROM PUBLIC.\"director\" WHERE id = ?";
    private static final String CREATE_QUERY = "INSERT INTO PUBLIC.\"director\" (name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE PUBLIC.\"director\" SET name=? WHERE id=?";
    private static final String DELETE_QUERY = "DELETE FROM PUBLIC.\"director\" WHERE id = ?";
    private static final String GET_DIRECTORS_VIA_IDS = "SELECT * FROM PUBLIC.\"director\" WHERE id IN (:ids);";

    public DirectorRepository(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper, Director.class);
    }

    public List<Director> getAll() {
        return findMany(GET_ALL_QUERY);
    }

    public Optional<Director> getById(long id) {
        return findOne(GET_ONE_QUERY, id);
    }

    public Director save(Director director) {
        long id = insert(CREATE_QUERY, director.getName());
        director.setId(id);
        return director;
    }

    public Director update(Director director) {
        this.update(UPDATE_QUERY, director.getName(), director.getId());
        return director;
    }

    public void remove(long id) {
        delete(DELETE_QUERY, id);
    }

    public Set<Director> getDirectorsViaIds(Set<Long> incomingDirectorIds) {
        if (incomingDirectorIds.isEmpty()) {
            return Set.of();
        }

        final NamedParameterJdbcOperations npjt = new NamedParameterJdbcTemplate(jdbc);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", incomingDirectorIds);

        return new HashSet<>(npjt.query(GET_DIRECTORS_VIA_IDS, params, new DirectorRowMapper()));
    }
}
