package ru.homyakin.seeker.game.top;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import ru.homyakin.seeker.game.event.models.EventStatus;
import ru.homyakin.seeker.game.personage.badge.BadgeView;
import ru.homyakin.seeker.game.personage.models.PersonageId;
import ru.homyakin.seeker.game.top.models.TopRaidPosition;
import ru.homyakin.seeker.game.top.models.TopSpinPosition;
import ru.homyakin.seeker.telegram.group.models.GroupId;

@Component
public class TopDao {
    private final JdbcClient jdbcClient;

    public TopDao(DataSource dataSource) {
        this.jdbcClient = JdbcClient.create(dataSource);
    }

    public List<TopRaidPosition> getUnsortedTopRaid(LocalDate start, LocalDate end) {
        final var sql = GLOBAL_RAIDS_COUNT + RAIDS_PERSONAGE_INFO;
        return jdbcClient.sql(sql)
            .param("success_id", EventStatus.SUCCESS.id())
            .param("fail_id", EventStatus.FAILED.id())
            .param("start_date", start)
            .param("end_date", end)
            .query(this::mapRaidPosition)
            .list();
    }

    public List<TopRaidPosition> getUnsortedTopRaidGroup(LocalDate start, LocalDate end, GroupId groupId) {
        final var sql = GROUP_TG_RAIDS_COUNT + RAIDS_PERSONAGE_INFO;
        return jdbcClient.sql(sql)
            .param("success_id", EventStatus.SUCCESS.id())
            .param("fail_id", EventStatus.FAILED.id())
            .param("start_date", start)
            .param("end_date", end)
            .param("grouptg_id", groupId.value())
            .query(this::mapRaidPosition)
            .list();
    }

    public List<TopSpinPosition> getUnsortedTopSpinGroup(GroupId groupId) {
        return jdbcClient.sql(TOP_SPIN_GROUP)
            .param("grouptg_id", groupId.value())
            .query(this::mapSpinPosition)
            .list();
    }

    private TopRaidPosition mapRaidPosition(ResultSet rs, int rowNum) throws SQLException {
        return new TopRaidPosition(
            PersonageId.from(rs.getLong("personage_id")),
            rs.getString("personage_name"),
            BadgeView.findByCode(rs.getString("badge_code")),
            rs.getInt("success_count"),
            rs.getInt("fail_count")
        );
    }

    private TopSpinPosition mapSpinPosition(ResultSet rs, int rowNum) throws SQLException {
        return new TopSpinPosition(
            PersonageId.from(rs.getLong("personage_id")),
            rs.getString("personage_name"),
            BadgeView.findByCode(rs.getString("badge_code")),
            rs.getInt("count")
        );
    }

    private static String GLOBAL_RAIDS_COUNT = """
        WITH event_points AS (
        SELECT
            pte.personage_id,
            SUM(CASE WHEN le.status_id = :success_id THEN 1 ELSE 0 END) AS success_count,
            SUM(CASE WHEN le.status_id = :fail_id THEN 1 ELSE 0 END) AS fail_count
        FROM personage_to_event pte
        LEFT JOIN launched_event le on le.id = pte.launched_event_id
        WHERE le.start_date::date >= :start_date AND le.start_date::date <= :end_date
        GROUP BY pte.personage_id
        )""";

    private static String GROUP_TG_RAIDS_COUNT = """
        WITH event_points AS (
        SELECT
            pte.personage_id,
            SUM(CASE WHEN le.status_id = :success_id THEN 1 ELSE 0 END) AS success_count,
            SUM(CASE WHEN le.status_id = :fail_id THEN 1 ELSE 0 END) AS fail_count
        FROM personage_to_event pte
        LEFT JOIN launched_event le on le.id = pte.launched_event_id
        LEFT JOIN grouptg_to_launched_event gtle on le.id = gtle.launched_event_id
        WHERE le.start_date::date >= :start_date AND le.start_date::date <= :end_date
        AND gtle.grouptg_id = :grouptg_id
        GROUP BY pte.personage_id
        )""";

    private static String RAIDS_PERSONAGE_INFO = """
        SELECT p.id personage_id, p.name personage_name, b.code badge_code, success_count, fail_count FROM personage p
        INNER JOIN event_points ep ON p.id = ep.personage_id
        LEFT JOIN public.personage_available_badge pab on p.id = pab.personage_id
        LEFT JOIN public.badge b on b.id = pab.badge_id
        WHERE pab.is_active = true""";

    private static String TOP_SPIN_GROUP = """
        WITH personage_count
        AS (
            SELECT personage_id, COUNT(*) as count
            FROM everyday_spin_tg
            WHERE grouptg_id = :grouptg_id
            GROUP BY personage_id
        )
        SELECT p.id personage_id, p.name personage_name, b.code badge_code, pc.count FROM personage p
        INNER JOIN personage_count pc ON p.id = pc.personage_id
        LEFT JOIN public.personage_available_badge pab on p.id = pab.personage_id
        LEFT JOIN public.badge b on b.id = pab.badge_id
        WHERE pab.is_active = true""";
}
