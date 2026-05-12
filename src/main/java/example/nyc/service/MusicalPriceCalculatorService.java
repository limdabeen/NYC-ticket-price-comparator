package example.nyc.service;

import example.nyc.model.ComparisonResult;
import example.nyc.model.Musical;
import example.nyc.model.ScrapedPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MusicalPriceCalculatorService {

    @Autowired
    private MusicalPriceScraperService scraper;

    private final List<Musical> allMusicals = Arrays.asList(
        Musical.builder()
            .id("LION_KING").name("라이온 킹").category("뮤지컬").recommendationScore("매우 높음")
            .ticketMasterPrice(150).cheapestSitePrice(140).athomeTripPrice(135).tamicePrice(145).pureunTourPrice(155)
            .ticketMasterLink("https://www.ticketmaster.com/the-lion-king-new-york/event/")
            .cheapestSiteLink("https://www.broadway.com/shows/lion-king/")
            .athomeTripLink("https://www.athometrip.com/product/lion-king/")
            .tamiceLink("https://www.tamice.com/musicals/lion-king")
            .pureunTourLink("https://pureuntour.com/nyc/lion-king")
            .build(),

        Musical.builder()
            .id("WICKED").name("위키드").category("뮤지컬").recommendationScore("높음")
            .ticketMasterPrice(140).cheapestSitePrice(130).athomeTripPrice(138).tamicePrice(125).pureunTourPrice(130)
            .ticketMasterLink("https://www.ticketmaster.com/wicked-new-york/event/")
            .cheapestSiteLink("https://www.broadway.com/shows/wicked/")
            .athomeTripLink("https://www.athometrip.com/product/wicked/")
            .tamiceLink("https://www.tamice.com/musicals/wicked")
            .pureunTourLink("https://pureuntour.com/nyc/wicked")
            .build(),

        Musical.builder()
            .id("ALADDIN").name("알라딘").category("뮤지컬").recommendationScore("높음")
            .ticketMasterPrice(160).cheapestSitePrice(145).athomeTripPrice(139).tamicePrice(155).pureunTourPrice(140)
            .ticketMasterLink("https://www.ticketmaster.com/aladdin-tickets/artist/1858715")
            .cheapestSiteLink("https://www.broadway.com/shows/aladdin/")
            .athomeTripLink("https://www.athometrip.com/product/aladdin/")
            .tamiceLink("https://www.tamice.com/musicals/aladdin")
            .pureunTourLink("https://pureuntour.com/nyc/aladdin")
            .build(),

        Musical.builder()
            .id("HAMILTON").name("해밀턴").category("뮤지컬").recommendationScore("매우 높음")
            .ticketMasterPrice(200).cheapestSitePrice(180).athomeTripPrice(185).tamicePrice(195).pureunTourPrice(190)
            .ticketMasterLink("https://www.ticketmaster.com/hamilton-new-york/event/")
            .cheapestSiteLink("https://www.broadway.com/shows/hamilton/")
            .athomeTripLink("https://www.athometrip.com/product/hamilton/")
            .tamiceLink("https://www.tamice.com/musicals/hamilton")
            .pureunTourLink("https://pureuntour.com/nyc/hamilton")
            .build(),

        Musical.builder()
            .id("CHICAGO").name("시카고").category("뮤지컬").recommendationScore("보통")
            .ticketMasterPrice(100).cheapestSitePrice(90).athomeTripPrice(95).tamicePrice(98).pureunTourPrice(102)
            .ticketMasterLink("https://www.ticketmaster.com/chicago-the-musical-new-york/event/")
            .cheapestSiteLink("https://www.broadway.com/shows/chicago/")
            .athomeTripLink("https://www.athometrip.com/product/chicago/")
            .tamiceLink("https://www.tamice.com/musicals/chicago")
            .pureunTourLink("https://pureuntour.com/nyc/chicago")
            .build(),

        Musical.builder()
            .id("PHANTOM").name("오페라의 유령 (리바이벌)").category("뮤지컬").recommendationScore("높음")
            .ticketMasterPrice(130).cheapestSitePrice(120).athomeTripPrice(125).tamicePrice(128).pureunTourPrice(132)
            .ticketMasterLink("https://www.ticketmaster.com/the-phantom-of-the-opera-new-york/event/")
            .cheapestSiteLink("https://www.broadway.com/shows/phantom-of-the-opera/")
            .athomeTripLink("https://www.athometrip.com/product/phantom/")
            .tamiceLink("https://www.tamice.com/musicals/phantom")
            .pureunTourLink("https://pureuntour.com/nyc/phantom")
            .build()
    );

    public List<Musical> getAllMusicals() {
        return allMusicals;
    }

    public List<ComparisonResult> comparePricesAndAnalyze(String musicalId) {
        Musical musical = allMusicals.stream()
                .filter(m -> m.getId().equals(musicalId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Musical ID: " + musicalId));

        // 실시간 스크래핑으로 가격 조회 (실패 시 기준가 폴백)
        ScrapedPrice tmPrice     = scraper.getTicketMasterPrice(musicalId, musical.getTicketMasterLink());
        ScrapedPrice athomePrice = scraper.getAthomeTripPrice(musicalId, musical.getAthomeTripLink());
        ScrapedPrice tamicePrice = scraper.getTamicePrice(musicalId, musical.getTamiceLink());
        ScrapedPrice pureunPrice = scraper.getPureunTourPrice(musicalId, musical.getPureunTourLink());

        // cheapestSite는 스크래퍼 없이 기준가 사용
        ScrapedPrice cheapestPrice = new ScrapedPrice(musical.getCheapestSitePrice(), false);

        Map<String, ScrapedPrice> priceMap = new LinkedHashMap<>();
        priceMap.put("티켓마스터(공식)", tmPrice);
        priceMap.put("브로드웨이닷컴", cheapestPrice);
        priceMap.put("앳홈트립", athomePrice);
        priceMap.put("타미스", tamicePrice);
        priceMap.put("푸른투어", pureunPrice);

        Map<String, String> linkMap = new LinkedHashMap<>();
        linkMap.put("티켓마스터(공식)", musical.getTicketMasterLink());
        linkMap.put("브로드웨이닷컴", musical.getCheapestSiteLink());
        linkMap.put("앳홈트립", musical.getAthomeTripLink());
        linkMap.put("타미스", musical.getTamiceLink());
        linkMap.put("푸른투어", musical.getPureunTourLink());

        int bestPrice = priceMap.values().stream()
                .mapToInt(ScrapedPrice::getPrice)
                .filter(p -> p > 0)
                .min().orElse(0);

        int worstPrice = priceMap.values().stream()
                .mapToInt(ScrapedPrice::getPrice)
                .filter(p -> p > 0)
                .max().orElse(bestPrice);

        // 스크래핑 시각 (가장 최신 실시간 항목 기준, 없으면 기준가 항목)
        String lastUpdated = priceMap.values().stream()
                .filter(ScrapedPrice::isRealtime)
                .findFirst()
                .map(ScrapedPrice::getFormattedTime)
                .orElseGet(() -> priceMap.values().stream()
                        .findFirst().map(ScrapedPrice::getFormattedTime).orElse("-"));

        List<ComparisonResult> results = new ArrayList<>();

        for (Map.Entry<String, ScrapedPrice> entry : priceMap.entrySet()) {
            String vendorName = entry.getKey();
            ScrapedPrice sp = entry.getValue();
            int price = sp.getPrice();

            if (price <= 0) continue;

            int savings = worstPrice - price;
            boolean isBest = price == bestPrice;
            String realtimeTag = sp.isRealtime() ? " [실시간]" : " [기준가]";
            String recText = getRecommendationText(vendorName) + realtimeTag;
            if (isBest) recText += " (최저가! $" + savings + " 절약 가능)";

            results.add(ComparisonResult.builder()
                    .optionName(vendorName)
                    .finalPrice(price)
                    .savings(savings)
                    .isBestDeal(isBest)
                    .purchaseLink(linkMap.get(vendorName))
                    .recommendationText(recText)
                    .lastUpdated(lastUpdated)
                    .build());
        }

        return results.stream()
                .sorted(Comparator.comparing(ComparisonResult::getFinalPrice))
                .collect(Collectors.toList());
    }

    private String getRecommendationText(String vendor) {
        return switch (vendor) {
            case "티켓마스터(공식)" -> "공식 사이트로 가장 안전하게 발권. 가격은 다소 높을 수 있습니다.";
            case "브로드웨이닷컴"  -> "다양한 뮤지컬의 최저가 티켓 취급. (취소/환불 정책 확인 필수)";
            case "앳홈트립"       -> "한국 여행객 특화 서비스 · 현지 지원 · 가격 비교 추천!";
            case "타미스"         -> "타임스퀘어 라운지 등 추가 혜택 제공. 종종 할인 이벤트 진행.";
            case "푸른투어"       -> "단체 여행객 대상 안정적인 티켓 제공.";
            default               -> "가격과 링크를 확인해 보세요.";
        };
    }
}
