package advent.telegrambot.repository;

import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.dto.AdventInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdventRepository extends JpaRepository<Advent, Integer> {
    Advent findLastByChatIdNullOrderByIdDesc();
    Advent findAdventByChatId(Long chatId);
    Optional<Advent> findByStepsQuestsId(Long questId);

    @Query(value = """
                select a.a_id as id, a.a_start_date as startDate,
                    c.cat_name as type,
                    (select count(distinct s.s_day) from step s where s.a_id = a.a_id) as daysCount
                from advent a
                    inner join cls_advent_type c on a.cat_id = c.cat_id
                where a.a_finish_date is null or a.a_finish_date > :finishDate
                order by a.a_start_date desc
                limit 5
                """,
            nativeQuery = true)
    List<AdventInfo> findNotFinishedAdvents(@Param("finishDate") LocalDate finishDate);

}
