package example.nyc.controller;

import example.nyc.model.ComparisonResult;
import example.nyc.model.Musical;
import example.nyc.service.ExchangeRateService;
import example.nyc.service.MusicalPriceCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MusicalCompareController {

    @Autowired private MusicalPriceCalculatorService musicalPriceCalculatorService;
    @Autowired private ExchangeRateService exchangeRateService;

    @GetMapping("/compare/musical")
    public String compareMusicalPrices(@RequestParam("id") String musicalId, Model model) {
        List<ComparisonResult> results = musicalPriceCalculatorService.comparePricesAndAnalyze(musicalId);

        Musical musical = musicalPriceCalculatorService.getAllMusicals().stream()
                .filter(m -> m.getId().equals(musicalId))
                .findFirst().orElse(null);

        model.addAttribute("musicalName", musical != null ? musical.getName() : musicalId);
        model.addAttribute("results", results);
        model.addAttribute("exchangeRate", exchangeRateService.getUsdToKrwRate());
        model.addAttribute("exchangeRateRealtime", exchangeRateService.isRealtime());
        model.addAttribute("exchangeRateFormatted", exchangeRateService.getFormattedRate());

        return "musical_result";
    }
}