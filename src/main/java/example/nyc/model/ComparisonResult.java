package example.nyc.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ComparisonResult {
    private String optionName;          // 구매 옵션 이름
    private int finalPrice;             // 최종 계산 가격 (달러)
    private int savings;                // 개별 구매 대비 절약된 금액
    private String recommendationText;  // 이 옵션에 대한 맞춤 추천 메시지
    private boolean isBestDeal;         // 최저가 여부
    private String purchaseLink;        // 해당 옵션의 최종 구매 링크
}