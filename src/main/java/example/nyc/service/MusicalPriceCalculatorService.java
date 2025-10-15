package example.nyc.service;

import example.nyc.model.Musical;
import example.nyc.model.ComparisonResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MusicalPriceCalculatorService {

    // 1. 초기 데이터: 뮤지컬 목록 (알라딘 데이터 포함)
    private final List<Musical> allMusicals = Arrays.asList(
            new Musical("LION_KING", "라이온 킹", "뮤지컬", "매우 높음",
                    150, 140, 135, 145, 155, // 가격 순서: 티켓마스터, 최저가사이트, 앳홈트립, 타미스, 푸른투어
                    "https://tm.link/lionking", "https://cheapest.link/lionking",
                    "https://athome.link/lionking", "https://tamice.link/lionking", "https://pureun.link/lionking"),

            new Musical("WICKED", "위키드", "뮤지컬", "높음",
                    140, 130, 138, 125, 130, // 가격 순서: 티켓마스터, 최저가사이트, 앳홈트립, 타미스, 푸른투어
                    "https://tm.link/wicked", "https://cheapest.link/wicked",
                    "https://athome.link/wicked", "https://tamice.link/wicked", "https://pureun.link/wicked"),

            new Musical("ALADDIN", "알라딘", "뮤지컬", "높음",
                    160, 145, 139, 155, 140, // 가격 순서: 티켓마스터, 최저가사이트, 앳홈트립, 타미스, 푸른투어
                    "https://www.ticketmaster.com/aladdin-tickets/artist/1858715?ac_link=broadway_guide_aladdin", // 티켓마스터
                    "https://broadway.link/cheapest/aladdin", // 최저가 사이트 (임시)
                    "https://athometrip.com/product/aladdin/", // 앳홈트립
                    "https://www.tamice.com/musicals/aladdin", // 타미스
                    "https://pureun.link/aladdin") // 푸른투어 (임시)
    );

    // 컨트롤러에서 전체 뮤지컬 목록을 가져갈 Getter
    public List<Musical> getAllMusicals() {
        return allMusicals;
    }

    /**
     * 특정 뮤지컬의 모든 판매처 가격을 비교하고 결과를 반환합니다.
     * @param musicalId 사용자가 선택한 뮤지컬 ID
     * @return 각 판매처별 가격 및 최저가 여부를 담은 ComparisonResult 리스트
     */
    public List<ComparisonResult> comparePricesAndAnalyze(String musicalId) {

        // 1. 선택한 뮤지컬 데이터 찾기
        Musical selectedMusical = allMusicals.stream()
                .filter(m -> m.getId().equals(musicalId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Musical ID: " + musicalId));

        // 2. 가격 비교 대상 목록 생성
        List<ComparisonResult> results = new ArrayList<>();

        // 가격 및 링크를 Map으로 정리
        Map<String, Integer> priceMap = new LinkedHashMap<>();
        priceMap.put("티켓마스터(공식)", selectedMusical.getTicketMasterPrice());
        priceMap.put("최저가 발권 사이트", selectedMusical.getCheapestSitePrice());
        priceMap.put("앳홈트립", selectedMusical.getAthomeTripPrice());
        priceMap.put("타미스", selectedMusical.getTamicePrice());
        priceMap.put("푸른투어", selectedMusical.getPureunTourPrice());

        Map<String, String> linkMap = new LinkedHashMap<>();
        linkMap.put("티켓마스터(공식)", selectedMusical.getTicketMasterLink());
        linkMap.put("최저가 발권 사이트", selectedMusical.getCheapestSiteLink());
        linkMap.put("앳홈트립", selectedMusical.getAthomeTripLink());
        linkMap.put("타미스", selectedMusical.getTamiceLink());
        linkMap.put("푸른투어", selectedMusical.getPureunTourLink());

        // 3. 최저 가격 찾기 (0은 유효하지 않은 가격으로 간주하고 제외)
        Optional<Integer> minPrice = priceMap.values().stream()
                .filter(price -> price > 0)
                .min(Comparator.naturalOrder());

        // 최고 가격 찾기 (절약 금액 계산을 위한 기준)
        Optional<Integer> maxPrice = priceMap.values().stream()
                .filter(price -> price > 0)
                .max(Comparator.naturalOrder());

        int bestPrice = minPrice.orElse(0);
        int worstPrice = maxPrice.orElse(bestPrice); // 최고가가 없으면 최저가를 기준

        // 4. ComparisonResult 리스트 생성
        for (Map.Entry<String, Integer> entry : priceMap.entrySet()) {
            String optionName = entry.getKey();
            int currentPrice = entry.getValue();
            String purchaseLink = linkMap.get(optionName);

            if (currentPrice > 0) { // 유효한 가격만 포함
                int savingsFromWorst = worstPrice - currentPrice;

                results.add(ComparisonResult.builder()
                        .optionName(optionName)
                        .finalPrice(currentPrice)
                        .savings(savingsFromWorst)
                        .isBestDeal(currentPrice == bestPrice && bestPrice > 0)
                        .purchaseLink(purchaseLink)
                        .recommendationText(getRecommendationText(optionName))
                        .build());
            }
        }

        // 5. 최저가 옵션 식별 및 절약액 최종 반영 (추천 메시지 업데이트)
        results.replaceAll(res -> {
            String recText = res.isBestDeal()
                    ? (res.getRecommendationText() + " (최저가! $" + res.getSavings() + " 절약 가능)")
                    : res.getRecommendationText();

            return ComparisonResult.builder()
                    .optionName(res.getOptionName())
                    .finalPrice(res.getFinalPrice())
                    .savings(res.getSavings())
                    .isBestDeal(res.isBestDeal())
                    .purchaseLink(res.getPurchaseLink())
                    .recommendationText(recText)
                    .build();
        });


        return results.stream()
                .sorted(Comparator.comparing(ComparisonResult::getFinalPrice)) // 가격 낮은 순으로 정렬
                .collect(Collectors.toList());
    }

    // 판매처별 추천 메시지 생성 로직
    private String getRecommendationText(String vendor) {
        switch (vendor) {
            case "티켓마스터(공식)":
                return "공식 사이트로 가장 안전하게 발권할 수 있습니다. 가격은 다소 높을 수 있습니다.";
            case "최저가 발권 사이트":
                return "다양한 뮤지컬의 최저가 티켓을 취급합니다. (취소/환불 정책 확인 필수)";
            case "앳홈트립":
                return "한국 여행객 특화 서비스와 현지 지원이 장점입니다. 가격 비교 추천!";
            case "타미스":
                return "현지 라운지 이용 등 추가 혜택을 제공하며, 종종 할인 이벤트를 진행합니다.";
            case "푸른투어":
                return "주로 단체 여행객을 위한 안정적인 티켓을 제공합니다.";
            default:
                return "가격과 링크를 확인해 보세요.";
        }
    }
}