import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class NextOneAMTime {
    public static void main(String[] args) {
        // Current time
        LocalDateTime now = LocalDateTime.now();

        // Set the time to 1 AM of the next day
        LocalDateTime nextOneAM = now.withHour(1).withMinute(0).withSecond(0).withNano(0);

        // If the current time is past 1 AM, move to the next day's 1 AM
        if (!now.isBefore(nextOneAM)) {
            nextOneAM = nextOneAM.plus(1, ChronoUnit.DAYS);
        }

        System.out.println("Next 1 AM time: " + nextOneAM);
    }
}
