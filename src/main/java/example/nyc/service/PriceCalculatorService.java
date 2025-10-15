package example.nyc.service;

import example.nyc.model.Attraction;
import example.nyc.model.ComparisonResult;
import example.nyc.model.PassOption;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PriceCalculatorService {

    // 1. 초기 데이터: 관광지 목록 (실제 DB 역할을 대신합니다)
    private final List<Attraction> allAttractions = Arrays.asList(
            // 추천 점수와 링크를 포함하도록 데이터 수정
            new Attraction("ESB", "엠파이어 스테이트 빌딩", 44, "전망대", "https://esb.link", 85),
            new Attraction("TOR", "탑 오브 더 락", 42, "전망대", "https://tor.link", 70),
            new Attraction("EDGE", "엣지 전망대", 38, "전망대", "https://edge.link", 65),
            new Attraction("MOMA", "MoMA (현대미술관)", 30, "박물관", "https://moma.link", 95),
            new Attraction("AMNH", "자연사 박물관", 23, "박물관", "https://amnh.link", 90),
            new Attraction("LIBERTY", "자유의 여신상 페리", 32, "크루즈", "https://liberty.link", 75)
    );

    // 2. 초기 데이터: 패스 옵션 목록
    private final List<PassOption> allPassOptions = Arrays.asList(
            // 빅애플 패스 (Tamice)
            // KidsPrice (임시로 0) 추가
            new PassOption("BIGAPPLE_3", "빅애플패스 3개권", "Tamice", 3, 105, 0, "타임스퀘어 라운지 제공", "https://tamice.link/buy/3"),
            new PassOption("BIGAPPLE_5", "빅애플패스 5개권", "Tamice", 5, 150, 0, "타임스퀘어 라운지 제공", "https://tamice.link/buy/5"),
            // 픽패스 (AthomeTrip)
            // KidsPrice (임시로 0) 추가
            new PassOption("PICK_3", "픽패스 3개권", "AthomeTrip", 3, 100, 0, "365일 환불 가능", "https://athome.link/buy/3"),
            new PassOption("PICK_5", "픽패스 5개권", "AthomeTrip", 5, 145, 0, "365일 환불 가능", "https://athome.link/buy/5")
    );

    // 3. 컨트롤러에서 사용할 데이터 Getter
    public List<Attraction> getAllAttractions() {
        return allAttractions;
    }

    public List<Attraction> getRecommendedAttractions(int limit) {
        return allAttractions.stream()
                .sorted(Comparator.comparing(Attraction::getRecommendationScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 4. 핵심 로직: 가격 비교 및 결과 반환
    public List<ComparisonResult> comparePricesAndAnalyze(List<String> selectedIds) {
        int selectedCount = selectedIds.size();

        // 4.1. 개별 구매 가격 계산
        int individualTotal = calculateIndividualTotal(selectedIds);
        List<ComparisonResult> results = new ArrayList<>();

        // 개별 구매 결과 추가 (기준점)
        results.add(ComparisonResult.builder()
                .optionName("개별 티켓 구매")
                .finalPrice(individualTotal)
                .savings(0)
                .isBestDeal(false) // 아직 최저가인지 모름
                .purchaseLink(null) // 개별 구매는 여러 링크이므로 null 처리
                .recommendationText("선택한 모든 관광지를 공식 홈페이지에서 직접 구매하는 비용입니다.")
                .build());

        // 4.2. 패스별 가격 비교 및 결과 추가
        for (PassOption option : allPassOptions) {
            // 선택한 개수를 커버할 수 있는 패스만 고려
            if (option.getItemCount() >= selectedCount) {
                int savings = individualTotal - option.getAdultPrice();

                results.add(ComparisonResult.builder()
                        .optionName(option.getName())
                        .finalPrice(option.getAdultPrice())
                        .savings(savings)
                        .isBestDeal(false) // 아직 최저가인지 모름
                        .purchaseLink(option.getPurchaseLink())
                        .recommendationText(getPassRecommendationText(option, savings))
                        .build());
            }
        }

        // 4.3. 최저가 옵션 식별
        Optional<ComparisonResult> bestResult = results.stream()
                .min(Comparator.comparing(ComparisonResult::getFinalPrice));

        bestResult.ifPresent(r -> {
            results.replaceAll(res -> {
                if (res.getOptionName().equals(r.getOptionName())) {
                    return ComparisonResult.builder()
                            .optionName(res.getOptionName())
                            .finalPrice(res.getFinalPrice())
                            .savings(res.getSavings())
                            .isBestDeal(true)
                            .purchaseLink(res.getPurchaseLink())
                            .recommendationText(res.getRecommendationText() + " (최저가! $ " + res.getSavings() + " 절약)")
                            .build();
                }
                return res;
            });
        });

        return results;
    }

    // 개별 구매 가격 총합 계산
    private int calculateIndividualTotal(List<String> selectedIds) {
        return allAttractions.stream()
                .filter(a -> selectedIds.contains(a.getId()))
                .mapToInt(Attraction::getIndividualPrice)
                .sum();
    }

    // 패스별 추천 메시지 생성 로직
    private String getPassRecommendationText(PassOption option, int savings) {
        String base = String.format("개별 구매 대비 **$%d** 절약 효과가 있습니다.", savings);

        if (option.getVendor().equals("Tamice")) {
            return base + " 타미스는 **타임스퀘어 라운지** 등 한국인 특화 서비스와 " + option.getKeyBenefit() + "가 장점입니다.";
        } else if (option.getVendor().equals("AthomeTrip")) {
            return base + " 앳홈트립은 **가장 저렴한 가격대**를 형성하며 " + option.getKeyBenefit() + "가 장점입니다.";
        }
        return base;
    }

}