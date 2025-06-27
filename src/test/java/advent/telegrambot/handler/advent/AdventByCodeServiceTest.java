package advent.telegrambot.handler.advent;

import advent.telegrambot.domain.advent.AdventByCode;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.CodeService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdventByCodeServiceTest {
    @Mock private StepRepository stepRepository;
    @Mock private AdventByCode adventByCode;
    @Mock private AdventService adventService;
    @Mock private CodeService codeService;
    @InjectMocks
    private AdventByCodeService adventByCodeService;

    @ParameterizedTest
    @MethodSource("getNeedCodesCountTestCases")
    void getNeedCodesCount_shouldCalculateCorrectly(
            LocalDate currentDate,
            LocalDate startDate,
            long distinctDays,
            int expectedCount
    ) {
        when(adventByCode.getStartDate()).thenReturn(startDate);
        when(stepRepository.countDistinctDaysByAdvent(adventByCode)).thenReturn(distinctDays);

        int actualCount = adventByCodeService.getNeedCodesCount(adventByCode, currentDate);

        assertEquals(expectedCount, actualCount);
    }

    private static Stream<Arguments> getNeedCodesCountTestCases() {
        LocalDate baseDate = LocalDate.of(2023, 12, 1);

        return Stream.of(
                // Случай 1: Текущая дата ДО начала адвента
                Arguments.of(
                        baseDate, // currentDate
                        baseDate.plusDays(5), // startDate (в будущем)
                        10L, // distinctDays
                        10 // expectedCount (берем просто distinctDays)
                ),

                // Случай 2: Текущая дата ПОСЛЕ начала адвента
                Arguments.of(
                        baseDate, // currentDate
                        baseDate.minusDays(3), // startDate (3 дня назад)
                        5L, // distinctDays
                        2
                ),

                // Случай 3: Текущая дата РАВНА дате начала
                Arguments.of(
                        baseDate, // currentDate
                        baseDate, // startDate
                        7L, // distinctDays
                        7 // expectedCount (берем distinctDays)
                ),

                // Случай 4: Адвент уже почти закончился
                Arguments.of(
                        baseDate, // currentDate
                        baseDate.minusDays(10), // startDate (10 дней назад)
                        12L, // distinctDays
                        2 // expectedCount: (startDate + 11) - currentDate + 1 = 2
                ),

                // Случай 5: Адвент закончился сегодня
                Arguments.of(
                        baseDate, // currentDate
                        baseDate.minusDays(9), // startDate (10 дней назад)
                        10L, // distinctDays
                        1 // expectedCount: (startDate + 11) - currentDate + 1 = 2
                ),

                // Случай 6: Адвент уже закончился (должен вернуть 0)
                Arguments.of(
                        baseDate, // currentDate
                        baseDate.minusDays(10), // startDate (10 дней назад)
                        5L, // distinctDays
                        0 // expectedCount: (startDate + 4) < currentDate
                )
        );
    }

    @ParameterizedTest
    @MethodSource("addCodesTestCases")
    void addCodes_ShouldModifyCodesListCorrectly(
            short daysCount,
            List<String> initialCodes,
            int expectedCodeCount
    ) {
        AdventByCode advent = new AdventByCode();
        advent.setId(1);
        advent.getCodes().addAll(initialCodes);

        when(adventService.findById(advent.getId())).thenReturn(advent);
        when(codeService.generateCode(10)).thenAnswer(_ -> UUID.randomUUID().toString());

        // Act
        adventByCodeService.addCodes(advent.getId(), daysCount);

        // Assert
        assertEquals(expectedCodeCount, advent.getCodes().size());
    }

    private static Stream<Arguments> addCodesTestCases() {
        return Stream.of(
                // Тест 1: Добавление кодов, когда текущее количество меньше требуемого
                Arguments.of(
                        (short) 5,
                        List.of("code1", "code2"),
                        5
                ),

                // Тест 2: Удаление кодов, когда текущее количество больше требуемого
                Arguments.of(
                        (short) 2,
                        List.of("code1", "code2", "code3", "code4"),
                        2
                ),

                // Тест 3: Без изменений, когда текущее количество равно требуемому
                Arguments.of(
                        (short) 3,
                        List.of("code1", "code2", "code3"),
                        3
                ),

                // Тест 4: Пустой список, нужно добавить все коды
                Arguments.of(
                        (short) 4,
                        List.of(),
                        4
                ),

                // Тест 5: Удаление всех кодов
                Arguments.of(
                        (short) 0,
                        List.of("code1", "code2", "code3"),
                        0
                )
        );
    }

}