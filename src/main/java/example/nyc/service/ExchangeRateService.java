package example.nyc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

/**
 * open.er-api.com 무료 API로 USD/KRW 실시간 환율을 조회합니다.
 * 1시간 캐시, 실패 시 기준환율(1,380원) 폴백.
 */
@Service
public class ExchangeRateService {

    private static final String API_URL = "https://open.er-api.com/v6/latest/USD";
    private static final int DEFAULT_RATE = 1380;
    private static final int CACHE_HOURS = 1;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private int cachedRate = DEFAULT_RATE;
    private boolean isRealtime = false;
    private LocalDateTime lastFetched = null;

    public int getUsdToKrwRate() {
        if (lastFetched == null || LocalDateTime.now().isAfter(lastFetched.plusHours(CACHE_HOURS))) {
            fetchAndCache();
        }
        return cachedRate;
    }

    public boolean isRealtime() {
        return isRealtime;
    }

    public String getFormattedRate() {
        return String.format("%,d", cachedRate);
    }

    private void fetchAndCache() {
        try {
            String json = restTemplate.getForObject(API_URL, String.class);
            JsonNode root = objectMapper.readTree(json);
            JsonNode rates = root.path("rates");
            if (rates.has("KRW")) {
                cachedRate = (int) Math.round(rates.get("KRW").asDouble());
                isRealtime = true;
                lastFetched = LocalDateTime.now();
            }
        } catch (Exception e) {
            cachedRate = DEFAULT_RATE;
            isRealtime = false;
        }
    }
}
