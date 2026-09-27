package vn.utetra.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.utetra.dto.CartItem;
import vn.utetra.entity.Product;
import vn.utetra.entity.Topping;
import vn.utetra.service.BranchService;
import vn.utetra.service.ProductService;
import vn.utetra.service.ToppingService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private static final String CART = "CART";
    private static final BigDecimal SIZE_M_SURCHARGE = new BigDecimal("5000");
    private static final BigDecimal SIZE_L_SURCHARGE = new BigDecimal("10000");

    @Autowired private ProductService productService;
    @Autowired private ToppingService toppingService;
    @Autowired private BranchService branchService;

    @SuppressWarnings("unchecked")
    private List<CartItem> getCart(HttpSession session) {
        Object o = session.getAttribute(CART);
        if (o == null) {
            List<CartItem> cart = new ArrayList<>();
            session.setAttribute(CART, cart);
            return cart;
        }
        return (List<CartItem>) o;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        List<CartItem> cart = getCart(session);
        BigDecimal total = cart.stream().map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        return "user/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            @RequestParam(required = false) String size,
                            @RequestParam(required = false) String sugarLevel,
                            @RequestParam(required = false) String iceLevel,
                            @RequestParam(required = false) List<Long> toppingIds,
                            HttpSession session) {
        Product p = productService.get(productId);
        CartItem item = new CartItem();
        item.setProductId(p.getId());
        item.setProductName(p.getName());
        item.setImageUrl(p.getImageUrl());
        item.setBasePrice(p.getBasePrice());
        item.setQuantity(quantity);
        item.setSize(size);
        item.setSugarLevel(sugarLevel);
        item.setIceLevel(iceLevel);
        item.setToppingIds(toppingIds);

        BigDecimal unit = p.getBasePrice();

        // Phụ thu theo Size (S = 0, M = +5.000, L = +10.000)
        if ("M".equalsIgnoreCase(size)) {
            unit = unit.add(SIZE_M_SURCHARGE);
        } else if ("L".equalsIgnoreCase(size)) {
            unit = unit.add(SIZE_L_SURCHARGE);
        }

        // Cộng tiền Topping
        if (toppingIds != null && !toppingIds.isEmpty()) {
            List<Topping> tops = toppingService.all().stream()
                    .filter(t -> toppingIds.contains(t.getId())).toList();
            item.setToppingNames(tops.stream().map(Topping::getName).toList());
            BigDecimal tSum = tops.stream().map(Topping::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setToppingPrice(tSum);
            unit = unit.add(tSum);
        }
        item.setUnitPrice(unit);
        getCart(session).add(item);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{index}")
    public String remove(@PathVariable int index, HttpSession session) {
        List<CartItem> cart = getCart(session);
        if (index >= 0 && index < cart.size()) cart.remove(index);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String update(@RequestParam int index, @RequestParam int quantity,
                         HttpSession session) {
        List<CartItem> cart = getCart(session);
        if (index >= 0 && index < cart.size()) {
            cart.get(index).setQuantity(quantity);
        }
        return "redirect:/cart";
    }
}