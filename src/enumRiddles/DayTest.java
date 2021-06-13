package enumRiddles;

enum Day {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    public Day next() {
        int nextDayIndex = this.ordinal() + 1 == values().length ? 0 : this.ordinal() + 1;
        return values()[nextDayIndex];
    }

    int getDayNumber() {
        return ordinal() + 1;
    }
}

public class DayTest {
    public static void main(String[] args) {
        for (Day day : Day.values()) {
            System.out.printf("%s (%d), next is %s\n", day, day.getDayNumber(), day.next());
        }
    }
}
