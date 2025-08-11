package com.stou.demo5_jpaproject.weather.weather;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/", ""})
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    // List page: GET /  (effective URL: /weather/ because of context-path)
    @GetMapping
    public String index(Model model) {
        model.addAttribute("weather", weatherService.getWeather());
        return "index";
    }

    // Form page: GET /weatherform
    @GetMapping("weatherform")
    public String showForm(Model model) {
        model.addAttribute("weather", new Weather());
        return "weatherform";
    }

    // Handle POST from form
    @PostMapping("weatherform")
    public String save(@ModelAttribute("weather") Weather weather, RedirectAttributes ra) {
        weatherService.addWeather(weather);
        ra.addFlashAttribute("message", "Saved successfully");
        return "redirect:/";
    }
}
