package example.nyc.controller;

import example.nyc.model.ComparisonResult;
import example.nyc.service.ExchangeRateService;
import example.nyc.service.PriceCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CompareController {

    @Autowired private PriceCalculatorService calculatorService;
    @Autowired private ExchangeRateService exchangeRateService;

    @PostMapping("/compare")
    public String compare(
            @RequestParam(value = "selectedAttractions", required = false) List<String> selectedIds,
            Model model) {

        if (selectedIds == null || selectedIds.isEmpty()) {
            return "redirect:/";
        }

        List<ComparisonResult> results = calculatorService.comparePricesAndAnalyze(selectedIds);

        model.addAttribute("comparisonResults", results);
        model.addAttribute("selectedCount", selectedIds.size());
        model.addAttribute("exchangeRate", exchangeRateService.getUsdToKrwRate());
        model.addAttribute("exchangeRateRealtime", exchangeRateService.isRealtime());
        model.addAttribute("exchangeRateFormatted", exchangeRateService.getFormattedRate());

        return "result";
    }
}