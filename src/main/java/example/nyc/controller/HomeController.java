package example.nyc.controller;

import example.nyc.model.Attraction;
import example.nyc.service.PriceCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private PriceCalculatorService calculatorService;

    @GetMapping("/")
    public String home(Model model) {
        List<Attraction> attractions = calculatorService.getAllAttractions();

        // 카테고리 목록 생성
        Set<String> categories = attractions.stream()
                .map(Attraction::getCategory)
                .collect(Collectors.toSet());

        // 추천 여행지 (점수 높은 상위 3개)
        List<Attraction> recommended = calculatorService.getRecommendedAttractions(3);

        model.addAttribute("attractions", attractions);
        model.addAttribute("categories", categories);
        model.addAttribute("recommended", recommended);

        return "index";
    }
}