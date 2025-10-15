package example.nyc.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Attraction {
    private String id;
    private String name;
    private int individualPrice;    // 개별 구매 시 성인 기준 가격 ($)
    private String category;        // 카테고리 (예: 전망대, 박물관, 투어)
    private String officialLink;    // 공식 홈페이지 티켓 구매 링크
    private int recommendationScore; // 추천 점수 (0~100, 높을수록 일반적 선호도 높음)
}