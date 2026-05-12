package example.nyc.controller;

import example.nyc.service.MusicalPriceCalculatorService;
import example.nyc.service.PriceCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private PriceCalculatorService calculatorService;

    @Autowired
    private MusicalPriceCalculatorService musicalService;

    @GetMapping("/")
    public String home(@RequestParam(value = "category", defaultValue = "ticket") String category, Model model) {
        model.addAttribute("currentCategory", category);

        if ("musical".equals(category)) {
            model.addAttribute("items", musicalService.getAllMusicals());
        } else {
            model.addAttribute("items", calculatorService.getAllAttractions());
            model.addAttribute("recommendedItems", calculatorService.getRecommendedAttractions(3));
        }

        return "index";
    }
}