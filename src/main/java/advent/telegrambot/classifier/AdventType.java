package advent.telegrambot.classifier;

public enum AdventType {
    BY_MESSAGE(1), BY_CODE(2);

    public int getId() {
        return id;
    }

    private int id;

    AdventType(int id) {
        this.id = id;
    }
}
