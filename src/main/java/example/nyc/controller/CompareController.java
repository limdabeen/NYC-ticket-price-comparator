package example.nyc.controller;

import example.nyc.model.ComparisonResult;
import example.nyc.service.PriceCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CompareController {

    private final PriceCalculatorService calculatorService;

    @Autowired
    public CompareController(PriceCalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @PostMapping("/compare")
    public String compare(
            @RequestParam(value = "selectedAttractions", required = false) List<String> selectedIds,
            Model model) {

        if (selectedIds == null || selectedIds.isEmpty()) {
            return "redirect:/";
        }

        // 1. 상세 가격 비교 및 분석 서비스 호출
        List<ComparisonResult> results = calculatorService.comparePricesAndAnalyze(selectedIds);

        model.addAttribute("comparisonResults", results);
        model.addAttribute("selectedCount", selectedIds.size());

        return "result"; // src/main/resources/templates/result.html
    }
}