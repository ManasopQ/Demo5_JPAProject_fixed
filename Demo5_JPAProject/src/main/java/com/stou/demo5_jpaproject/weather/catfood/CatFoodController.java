package com.stou.demo5_jpaproject.weather.catfood;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/", ""})
public class CatFoodController {

    private final CatFoodService catFoodService;

    public CatFoodController(CatFoodService catFoodService) {
        this.catFoodService = catFoodService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("products", catFoodService.getProducts());
        return "index";
    }

    @GetMapping("productform")
    public String showForm(Model model) {
        model.addAttribute("product", new CatFood());
        return "catfoodform";
    }

    @GetMapping("productform/{code}")
    public String editForm(@PathVariable String code, Model model) {
        model.addAttribute("product", catFoodService.getProduct(code));
        return "catfoodform";
    }

    @PostMapping("productform")
    public String save(@ModelAttribute("product") CatFood product, RedirectAttributes ra) {
        catFoodService.saveProduct(product);
        ra.addFlashAttribute("message", "Saved successfully");
        return "redirect:/";
    }

    @GetMapping("delete/{code}")
    public String delete(@PathVariable String code, RedirectAttributes ra) {
        catFoodService.deleteProduct(code);
        ra.addFlashAttribute("message", "Deleted successfully");
        return "redirect:/";
    }
}
