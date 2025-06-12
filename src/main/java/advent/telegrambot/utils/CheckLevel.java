package advent.telegrambot.utils;

public enum CheckLevel {
    WARNING(1),
    ERROR(2),
    ;

    private final int id;

    CheckLevel(int id) {
        this.id = id;
    }
}
