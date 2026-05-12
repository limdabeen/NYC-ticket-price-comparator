package example.nyc.service;

import example.nyc.model.Attraction;
import example.nyc.model.ComparisonResult;
import example.nyc.model.PassOption;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PriceCalculatorService {

    private final List<Attraction> allAttractions = Arrays.asList(
        new Attraction("ESB", "엠파이어 스테이트 빌딩", 44, "전망대",
            "https://www.esbnyc.com/buy-tickets", 85,
            "102층 높이 뉴욕 최고의 전망대. 1931년 건축된 아이코닉 랜드마크로 맨해튼 전경이 360도로 펼쳐집니다.",
            "08:00 ~ 02:00 (연중무휴)"),
        new Attraction("TOR", "탑 오브 더 락", 42, "전망대",
            "https://www.topoftherocknyc.com/plan-your-visit/tickets/", 70,
            "록펠러 센터 70층 옥상 전망대. 엠파이어 스테이트 빌딩을 정면으로 바라볼 수 있는 유일한 뷰포인트.",
            "09:00 ~ 23:00 (연중무휴)"),
        new Attraction("EDGE", "엣지 전망대", 38, "전망대",
            "https://www.edgenyc.com/en/tickets", 65,
            "허드슨 야드 100층에 위치한 뉴욕 최신 야외 전망대. 유리바닥과 기울어진 유리벽이 스릴을 더합니다.",
            "08:00 ~ 23:00 (연중무휴)"),
        new Attraction("MOMA", "MoMA (현대미술관)", 30, "박물관",
            "https://www.moma.org/tickets/", 95,
            "세계 최고의 현대미술관. 피카소·모네·달리·워홀 등 2만여 점의 작품 소장. 금요일 저녁 무료입장.",
            "10:30 ~ 17:30 (목·금 ~20:00, 화 휴관)"),
        new Attraction("AMNH", "자연사 박물관", 23, "박물관",
            "https://www.amnh.org/plan-your-visit/tickets", 90,
            "공룡 화석부터 우주탐험까지. 영화 '박물관이 살아있다' 촬영지. 어린이와 함께라면 필수 방문지.",
            "10:00 ~ 17:30 (연중무휴)"),
        new Attraction("LIBERTY", "자유의 여신상 페리", 32, "크루즈/투어",
            "https://www.statuecruises.com/statue-of-liberty-tickets", 75,
            "뉴욕의 상징 자유의 여신상 & 엘리스 아일랜드 탐방. 왕복 페리 포함. 크라운 입장은 별도 예약 필수.",
            "09:00 ~ 15:30 출발 (계절별 상이)")
    );

    private final List<PassOption> allPassOptions = Arrays.asList(
        new PassOption("BIGAPPLE_3", "빅애플패스 3개권", "Tamice", 3, 105, 75,
            "타임스퀘어 라운지 무료 이용", "https://www.tamice.com/passes/bigapple/3"),
        new PassOption("BIGAPPLE_5", "빅애플패스 5개권", "Tamice", 5, 150, 110,
            "타임스퀘어 라운지 무료 이용", "https://www.tamice.com/passes/bigapple/5"),
        new PassOption("PICK_3", "픽패스 3개권", "AthomeTrip", 3, 100, 72,
            "365일 환불 가능", "https://www.athometrip.com/product/pick-pass-3/"),
        new PassOption("PICK_5", "픽패스 5개권", "AthomeTrip", 5, 145, 105,
            "365일 환불 가능", "https://www.athometrip.com/product/pick-pass-5/")
    );

    public List<Attraction> getAllAttractions() {
        return allAttractions;
    }

    public List<Attraction> getRecommendedAttractions(int limit) {
        return allAttractions.stream()
                .sorted(Comparator.comparing(Attraction::getRecommendationScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<ComparisonResult> comparePricesAndAnalyze(List<String> selectedIds) {
        int selectedCount = selectedIds.size();
        int individualTotal = calculateIndividualTotal(selectedIds);
        List<ComparisonResult> results = new ArrayList<>();

        results.add(ComparisonResult.builder()
                .optionName("개별 티켓 구매")
                .finalPrice(individualTotal)
                .savings(0)
                .isBestDeal(false)
                .purchaseLink(null)
                .recommendationText("선택한 모든 관광지를 각 공식 홈페이지에서 직접 구매하는 비용입니다.")
                .build());

        for (PassOption option : allPassOptions) {
            if (option.getItemCount() >= selectedCount) {
                int savings = individualTotal - option.getAdultPrice();
                results.add(ComparisonResult.builder()
                        .optionName(option.getName())
                        .finalPrice(option.getAdultPrice())
                        .savings(savings)
                        .isBestDeal(false)
                        .purchaseLink(option.getPurchaseLink())
                        .recommendationText(getPassRecommendationText(option, savings))
                        .build());
            }
        }

        Optional<ComparisonResult> bestResult = results.stream()
                .min(Comparator.comparing(ComparisonResult::getFinalPrice));

        bestResult.ifPresent(best -> results.replaceAll(res -> {
            if (!res.getOptionName().equals(best.getOptionName())) return res;
            return ComparisonResult.builder()
                    .optionName(res.getOptionName())
                    .finalPrice(res.getFinalPrice())
                    .savings(res.getSavings())
                    .isBestDeal(true)
                    .purchaseLink(res.getPurchaseLink())
                    .recommendationText(res.getRecommendationText() + " (최저가! $" + res.getSavings() + " 절약)")
                    .build();
        }));

        return results;
    }

    private int calculateIndividualTotal(List<String> selectedIds) {
        return allAttractions.stream()
                .filter(a -> selectedIds.contains(a.getId()))
                .mapToInt(Attraction::getIndividualPrice)
                .sum();
    }

    private String getPassRecommendationText(PassOption option, int savings) {
        String base = String.format("개별 구매 대비 $%d 절약 효과.", savings);
        if ("Tamice".equals(option.getVendor())) {
            return base + " 타미스는 타임스퀘어 라운지 등 한국인 특화 서비스 · " + option.getKeyBenefit() + "가 장점입니다.";
        } else if ("AthomeTrip".equals(option.getVendor())) {
            return base + " 앳홈트립은 가장 저렴한 가격대를 형성하며 " + option.getKeyBenefit() + "가 장점입니다.";
        }
        return base;
    }
}
