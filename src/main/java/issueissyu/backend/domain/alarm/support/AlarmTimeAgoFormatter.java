package issueissyu.backend.domain.alarm.support;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import org.springframework.stereotype.Component;

@Component
public class AlarmTimeAgoFormatter {

    public String format(LocalDateTime createdAt, LocalDateTime now) {
        if (createdAt == null || now == null || !createdAt.isBefore(now)) {
            return "0000-00-00 00:00:00.000000";
        }

        Period period = Period.between(createdAt.toLocalDate(), now.toLocalDate());
        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        LocalDateTime adjusted = createdAt.plusYears(years).plusMonths(months).plusDays(days);
        Duration remainder = Duration.between(adjusted, now);

        long hours = remainder.toHours();
        long minutes = remainder.toMinutesPart();
        long seconds = remainder.toSecondsPart();
        long micros = remainder.toNanosPart() / 1_000L;

        return String.format(
                "%04d-%02d-%02d %02d:%02d:%02d.%06d",
                years, months, days, hours, minutes, seconds, micros);
    }
}
