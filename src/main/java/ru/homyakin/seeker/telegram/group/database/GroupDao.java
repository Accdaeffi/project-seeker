package ru.homyakin.seeker.telegram.group.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.homyakin.seeker.locale.Language;
import ru.homyakin.seeker.telegram.group.models.ActiveTime;
import ru.homyakin.seeker.telegram.group.models.Group;
import ru.homyakin.seeker.telegram.group.models.GroupId;
import ru.homyakin.seeker.utils.TimeUtils;

@Component
public class GroupDao {
    private static final String GET_GROUP_BY_ID = "SELECT * FROM grouptg WHERE id = :id";
    private static final String GET_GROUP_WITH_LESS_NEXT_EVENT_DATE = """
        SELECT * FROM grouptg WHERE next_event_date  < :next_event_date and is_active = true
        """;
    private static final String GET_GROUP_WITH_LESS_NEXT_RUMOR_DATE = """
        SELECT * FROM grouptg WHERE next_rumor_date < :next_rumor_date and is_active = true
        """;
    private static final String SAVE_GROUP = """
        insert into grouptg (id, is_active, language_id, init_date, next_event_date)
        values (:id, :is_active, :language_id, :init_date, :next_event_date)
        """;
    private static final String UPDATE = """
        update grouptg
        set is_active = :is_active, language_id = :language_id, start_active_hour = :start_active_hour, 
        end_active_hour = :end_active_hour, active_time_zone = :active_time_zone
        where id = :id;
        """;

    private static final String UPDATE_NEXT_EVENT_DATE = """
        UPDATE grouptg SET next_event_date = :next_event_date WHERE id = :id
        """;

    private static final String UPDATE_NEXT_RUMOR_DATE = """
        UPDATE grouptg SET next_rumor_date = :next_rumor_date WHERE id = :id
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public GroupDao(DataSource dataSource) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public void save(Group group) {
        final var now = TimeUtils.moscowTime();
        final var params = new HashMap<String, Object>();
        params.put("id", group.id().value());
        params.put("is_active", group.isActive());
        params.put("language_id", group.language().id());
        params.put("init_date", now);
        params.put("next_event_date", now);
        jdbcTemplate.update(
            SAVE_GROUP,
            params
        );
    }

    public Optional<Group> getById(GroupId groupId) {
        final var result = jdbcTemplate.query(
            GET_GROUP_BY_ID,
            Collections.singletonMap("id", groupId.value()),
            this::mapRow
        );
        return result.stream().findFirst();
    }

    public List<Group> getGetGroupsWithLessNextEventDate(LocalDateTime maxNextEventDate) {
        final var params = Collections.singletonMap("next_event_date", maxNextEventDate);
        return jdbcTemplate.query(
            GET_GROUP_WITH_LESS_NEXT_EVENT_DATE,
            params,
            this::mapRow
        );
    }

    public List<Group> getGetGroupsWithLessNextRumorDate(LocalDateTime maxNextRumorDate) {
        final var params = Collections.singletonMap("next_rumor_date", maxNextRumorDate);
        return jdbcTemplate.query(
            GET_GROUP_WITH_LESS_NEXT_RUMOR_DATE,
            params,
            this::mapRow
        );
    }

    public void update(Group group) {
        final var params = new HashMap<String, Object>();
        params.put("id", group.id().value());
        params.put("is_active", group.isActive());
        params.put("language_id", group.language().id());
        params.put("start_active_hour", group.activeTime().startHour());
        params.put("end_active_hour", group.activeTime().endHour());
        params.put("active_time_zone", group.activeTime().timeZone());
        jdbcTemplate.update(
            UPDATE,
            params
        );
    }

    public void updateNextEventDate(GroupId groupId, LocalDateTime nextEventDate) {
        final var params = new HashMap<String, Object>();
        params.put("id", groupId.value());
        params.put("next_event_date", nextEventDate);
        jdbcTemplate.update(
            UPDATE_NEXT_EVENT_DATE,
            params
        );
    }

    public void updateNextRumorDate(GroupId groupId, LocalDateTime nextRumorDate) {
        final var params = new HashMap<String, Object>();
        params.put("id", groupId.value());
        params.put("next_rumor_date", nextRumorDate);
        jdbcTemplate.update(
            UPDATE_NEXT_RUMOR_DATE,
            params
        );
    }

    private Group mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Group(
            GroupId.from(rs.getLong("id")),
            rs.getBoolean("is_active"),
            Language.getOrDefault(rs.getInt("language_id")),
            new ActiveTime(
                rs.getInt("start_active_hour"),
                rs.getInt("end_active_hour"),
                rs.getInt("active_time_zone")
            )
        );
    }
}
