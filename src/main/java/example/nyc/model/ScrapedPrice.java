package example.nyc.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class ScrapedPrice {
    private final int price;
    private final boolean isRealtime;        // true: 실시간 스크래핑, false: 기준가 사용
    private final LocalDateTime scrapedAt;

    public ScrapedPrice(int price, boolean isRealtime) {
        this.price = price;
        this.isRealtime = isRealtime;
        this.scrapedAt = LocalDateTime.now();
    }

    public boolean isExpired(int cacheMinutes) {
        return LocalDateTime.now().isAfter(scrapedAt.plusMinutes(cacheMinutes));
    }

    public String getFormattedTime() {
        return scrapedAt.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));
    }
}
