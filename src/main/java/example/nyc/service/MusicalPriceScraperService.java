package example.nyc.service;

import example.nyc.model.ScrapedPrice;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 각 대행사 사이트에서 뮤지컬 티켓 가격을 실시간으로 스크래핑하고
 * 30분 캐시를 통해 제공합니다.
 */
@Service
public class MusicalPriceScraperService {

    private static final int CACHE_MINUTES = 30;
    private static final int TIMEOUT_MS = 8000;
    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$([0-9]{2,4}(?:\\.[0-9]{2})?)");

    // 캐시 키 형식: "VENDOR:MUSICAL_ID"
    private final Map<String, ScrapedPrice> priceCache = new ConcurrentHashMap<>();

    // 대행사별 기준가 (스크래핑 실패 시 폴백)
    private static final Map<String, Map<String, Integer>> DEFAULT_PRICES = Map.of(
        "ticketmaster", Map.of(
            "LION_KING", 150, "WICKED", 140, "ALADDIN", 160,
            "HAMILTON", 200, "CHICAGO", 100, "PHANTOM", 130
        ),
        "athometrip", Map.of(
            "LION_KING", 135, "WICKED", 138, "ALADDIN", 139,
            "HAMILTON", 185, "CHICAGO", 95, "PHANTOM", 125
        ),
        "tamice", Map.of(
            "LION_KING", 145, "WICKED", 125, "ALADDIN", 155,
            "HAMILTON", 195, "CHICAGO", 98, "PHANTOM", 128
        ),
        "pureuntour", Map.of(
            "LION_KING", 155, "WICKED", 130, "ALADDIN", 140,
            "HAMILTON", 190, "CHICAGO", 102, "PHANTOM", 132
        )
    );

    public ScrapedPrice getTicketMasterPrice(String musicalId, String url) {
        return getCachedOrScrape("ticketmaster", musicalId, url, this::scrapeTicketMaster);
    }

    public ScrapedPrice getAthomeTripPrice(String musicalId, String url) {
        return getCachedOrScrape("athometrip", musicalId, url, this::scrapeAthomeTrip);
    }

    public ScrapedPrice getTamicePrice(String musicalId, String url) {
        return getCachedOrScrape("tamice", musicalId, url, this::scrapeTamice);
    }

    public ScrapedPrice getPureunTourPrice(String musicalId, String url) {
        return getCachedOrScrape("pureuntour", musicalId, url, this::scrapePureunTour);
    }

    private ScrapedPrice getCachedOrScrape(String vendor, String musicalId, String url, Scraper scraper) {
        String cacheKey = vendor + ":" + musicalId;
        ScrapedPrice cached = priceCache.get(cacheKey);

        if (cached != null && !cached.isExpired(CACHE_MINUTES)) {
            return cached;
        }

        try {
            int price = scraper.scrape(url);
            if (price > 0) {
                ScrapedPrice result = new ScrapedPrice(price, true);
                priceCache.put(cacheKey, result);
                return result;
            }
        } catch (Exception ignored) {}

        // 스크래핑 실패 시 기준가 사용
        int defaultPrice = DEFAULT_PRICES.getOrDefault(vendor, Map.of()).getOrDefault(musicalId, 0);
        return new ScrapedPrice(defaultPrice, false);
    }

    private int scrapeTicketMaster(String url) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .timeout(TIMEOUT_MS)
                .get();

        // TicketMaster JSON-LD 가격 파싱 시도
        for (Element script : doc.select("script[type=application/ld+json]")) {
            String json = script.html();
            if (json.contains("lowPrice")) {
                Matcher m = Pattern.compile("\"lowPrice\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]+)?)\"?").matcher(json);
                if (m.find()) return (int) Double.parseDouble(m.group(1));
            }
        }

        // 일반 가격 텍스트 파싱 시도
        Element priceEl = doc.selectFirst("[data-testid=price], .price-with-fees, .event-listing-price");
        if (priceEl != null) {
            return extractDollarAmount(priceEl.text());
        }

        return extractFromPageText(doc.body().text());
    }

    private int scrapeAthomeTrip(String url) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .referrer("https://www.athometrip.com")
                .timeout(TIMEOUT_MS)
                .get();

        // 앳홈트립: 한국어 사이트, price 관련 class/element 탐색
        Element priceEl = doc.selectFirst(".price, .product-price, .ticket-price, [class*=price]");
        if (priceEl != null) {
            int p = extractDollarAmount(priceEl.text());
            if (p > 0) return p;
        }

        return extractFromPageText(doc.body().text());
    }

    private int scrapeTamice(String url) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .referrer("https://www.tamice.com")
                .timeout(TIMEOUT_MS)
                .get();

        // 타미스: price 관련 요소 탐색
        Element priceEl = doc.selectFirst(".price, .product_price, .ticket_price, [class*=price]");
        if (priceEl != null) {
            int p = extractDollarAmount(priceEl.text());
            if (p > 0) return p;
        }

        return extractFromPageText(doc.body().text());
    }

    private int scrapePureunTour(String url) throws Exception {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .timeout(TIMEOUT_MS)
                .get();

        Element priceEl = doc.selectFirst(".price, .product-price, [class*=price]");
        if (priceEl != null) {
            int p = extractDollarAmount(priceEl.text());
            if (p > 0) return p;
        }

        return extractFromPageText(doc.body().text());
    }

    private int extractDollarAmount(String text) {
        Matcher m = PRICE_PATTERN.matcher(text);
        int best = 0;
        while (m.find()) {
            int val = (int) Double.parseDouble(m.group(1));
            // 뮤지컬 티켓 유효 가격 범위: $50 ~ $500
            if (val >= 50 && val <= 500) {
                if (best == 0 || val < best) best = val;
            }
        }
        return best;
    }

    private int extractFromPageText(String pageText) {
        return extractDollarAmount(pageText);
    }

    @FunctionalInterface
    private interface Scraper {
        int scrape(String url) throws Exception;
    }
}
