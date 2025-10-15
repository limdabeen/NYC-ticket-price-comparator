package example.nyc.controller;

import example.nyc.model.ComparisonResult;
import example.nyc.service.MusicalPriceCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MusicalCompareController {

    private final MusicalPriceCalculatorService musicalPriceCalculatorService;

    @Autowired
    public MusicalCompareController(MusicalPriceCalculatorService musicalPriceCalculatorService) {
        this.musicalPriceCalculatorService = musicalPriceCalculatorService;
    }

    // URL: /compare/musical?id=ALADDIN
    @GetMapping("/compare/musical")
    public String compareMusicalPrices(
            @RequestParam("id") String musicalId,
            Model model) {

        // 1. 서비스 로직 호출: ID에 해당하는 뮤지컬의 모든 판매처 가격 비교
        List<ComparisonResult> results = musicalPriceCalculatorService.comparePricesAndAnalyze(musicalId);

        // 2. 결과 데이터를 뷰에 전달
        model.addAttribute("musicalName", musicalId.replace("_", " ").toUpperCase()); // 제목 표시용
        model.addAttribute("results", results);

        // 3. 템플릿 반환
        return "musical_result"; // src/main/resources/templates/musical_result.html 로 이동
    }
}