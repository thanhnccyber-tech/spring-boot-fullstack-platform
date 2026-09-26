package vn.utetra.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.utetra.entity.Product;
import vn.utetra.service.CategoryService;
import vn.utetra.service.ProductService;
import vn.utetra.service.ToppingService;

import java.math.BigDecimal;

@Controller
public class HomeController {

    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;
    @Autowired private ToppingService toppingService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("featured", productService.featured());
        model.addAttribute("newArrivals", productService.newArrivals());
        model.addAttribute("categories", categoryService.all());
        return "user/home";
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String kw,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false) BigDecimal minPrice,
                           @RequestParam(required = false) BigDecimal maxPrice,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "12") int size,
                           @RequestParam(defaultValue = "newest") String sort,
                           Model model) {
        Page<Product> p = productService.search(kw, categoryId, minPrice, maxPrice, page, size, sort);
        model.addAttribute("pageData", p);
        model.addAttribute("kw", kw);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        model.addAttribute("categories", categoryService.all());
        return "user/products";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product p = productService.get(id);
        model.addAttribute("product", p);
        model.addAttribute("toppings", toppingService.active());
        return "user/product-detail";
    }
}