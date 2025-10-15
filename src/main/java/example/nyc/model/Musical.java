package example.nyc.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Musical {
    // 기본 정보
    private String id;              // 뮤지컬 식별자 (예: LION_KING)
    private String name;            // 뮤지컬 이름 (예: 라이온 킹)
    private String category;        // 카테고리 (예: 뮤지컬)
    private String recommendationScore; // 추천 점수 (선택 사항)

    // 가격 정보 (모두 달러 기준)
    private int ticketMasterPrice;  // 티켓마스터 (공식/일반 사이트) 가격
    private int cheapestSitePrice;  // 다른 발권 사이트 최저가
    private int athomeTripPrice;    // 앳홈트립 가격
    private int tamicePrice;        // 타미스 가격
    private int pureunTourPrice;    // 푸른투어 가격

    // 구매 링크
    private String ticketMasterLink;
    private String cheapestSiteLink;
    private String athomeTripLink;
    private String tamiceLink;
    private String pureunTourLink;
}