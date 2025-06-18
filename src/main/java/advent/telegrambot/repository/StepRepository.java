package advent.telegrambot.repository;

import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.advent.Advent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StepRepository extends JpaRepository<Step, Long> {
    Optional<Step> findFirstByAdventAndDayAndOrderGreaterThanOrderByOrderAsc(
            Advent advent,
            Short day,
            Short minOrder);

    Optional<Step> findFirstByAdventAndDayAndOrder(Advent advent,
                                                   Short day,
                                                   Short order);

    List<Step> findByAdventAndDayAndOrderGreaterThanOrderByOrderAsc(
            Advent advent,
            Short day,
            Short minOrder);

    @EntityGraph("Step.withContentAndQuests")
    Optional<Step> findFullGraphById(Long id);


    @Query("SELECT COUNT(DISTINCT s.day) FROM Step s WHERE s.advent = :advent")
    long countDistinctDaysByAdvent(@Param("advent") Advent advent);

    @Query("SELECT coalesce(MAX(s.day), 0) FROM Step s WHERE s.advent = :advent")
    short getMaxDaysByAdvent(@Param("advent") Advent advent);

    @Query("SELECT coalesce(max(s.order), 0) from Step s where s.advent = :advent and s.day = :day")
    short getMaxOrderAtDayByAdvent(@Param("advent") Advent advent, @Param("day") Short day);

    @Query(value = """
            SELECT EXISTS(
                select 1
                from step s
                where s.a_id = :adventId
                    and (s.s_day > :day or (s.s_day = :day and s.s_order > :order)))
        """, nativeQuery = true)
    boolean existsNextSteps(@Param("adventId") Integer adventId, @Param("day") Short day, @Param("order") Short order);
}
