package issueissyu.backend.domain.alarm.support;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class AlarmTimeAgoFormatter {

    public String format(LocalDateTime createdAt, LocalDateTime now) {
        if (createdAt == null || now == null || !createdAt.isBefore(now)) {
            return "0000-00-00 00:00:00.000000";
        }

        LocalDateTime temp = createdAt;
        long years = ChronoUnit.YEARS.between(temp, now);
        temp = temp.plusYears(years);

        long months = ChronoUnit.MONTHS.between(temp, now);
        temp = temp.plusMonths(months);

        long days = ChronoUnit.DAYS.between(temp, now);
        temp = temp.plusDays(days);

        long hours = ChronoUnit.HOURS.between(temp, now);
        temp = temp.plusHours(hours);

        long minutes = ChronoUnit.MINUTES.between(temp, now);
        temp = temp.plusMinutes(minutes);

        long seconds = ChronoUnit.SECONDS.between(temp, now);
        temp = temp.plusSeconds(seconds);

        long micros = ChronoUnit.MICROS.between(temp, now);

        return String.format(
                "%04d-%02d-%02d %02d:%02d:%02d.%06d",
                years, months, days, hours, minutes, seconds, micros);
    }
}
