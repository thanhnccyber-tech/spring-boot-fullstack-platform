package vn.utetra.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.utetra.dto.CartItem;
import vn.utetra.entity.*;
import vn.utetra.service.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private static final String CART = "CART";

    @Autowired private ProductService productService;
    @Autowired private ToppingService toppingService;
    @Autowired private SizeService sizeService;
    @Autowired private SugarService sugarService;
    @Autowired private IceService iceService;

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
                            @RequestParam(required = false) Long sizeId,
                            @RequestParam(required = false) Long sugarId,
                            @RequestParam(required = false) Long iceId,
                            @RequestParam(required = false) List<Long> toppingIds,
                            HttpSession session) {

        Product p = productService.get(productId);
        CartItem item = new CartItem();
        item.setProductId(p.getId());
        item.setProductName(p.getName());
        item.setImageUrl(p.getImageUrl());
        item.setBasePrice(p.getBasePrice());
        item.setQuantity(quantity);

        BigDecimal unit = p.getBasePrice();

        // ===== Size =====
        if (Boolean.TRUE.equals(p.getAllowSize())) {
            SizeOption chosen = null;
            if (sizeId != null) {
                chosen = sizeService.active().stream()
                        .filter(x -> x.getId().equals(sizeId))
                        .findFirst().orElse(null);
            }
            if (chosen == null && p.getDefaultSize() != null
                    && Boolean.TRUE.equals(p.getDefaultSize().getActive())) {
                chosen = p.getDefaultSize();
            }
            if (chosen != null) {
                item.setSizeId(chosen.getId());
                item.setSizeName(chosen.getName());
                BigDecimal extra = chosen.getExtraPrice() == null ? BigDecimal.ZERO : chosen.getExtraPrice();
                item.setSizeExtraPrice(extra);
                unit = unit.add(extra);
            }
        }

        // ===== Sugar =====
        if (Boolean.TRUE.equals(p.getAllowSugar())) {
            SugarLevel chosen = null;
            if (sugarId != null) {
                chosen = sugarService.active().stream()
                        .filter(x -> x.getId().equals(sugarId))
                        .findFirst().orElse(null);
            }
            if (chosen == null && p.getDefaultSugar() != null
                    && Boolean.TRUE.equals(p.getDefaultSugar().getActive())) {
                chosen = p.getDefaultSugar();
            }
            if (chosen != null) {
                item.setSugarId(chosen.getId());
                item.setSugarName(chosen.getName());
            }
        }

        // ===== Ice =====
        if (Boolean.TRUE.equals(p.getAllowIce())) {
            IceLevel chosen = null;
            if (iceId != null) {
                chosen = iceService.active().stream()
                        .filter(x -> x.getId().equals(iceId))
                        .findFirst().orElse(null);
            }
            if (chosen == null && p.getDefaultIce() != null
                    && Boolean.TRUE.equals(p.getDefaultIce().getActive())) {
                chosen = p.getDefaultIce();
            }
            if (chosen != null) {
                item.setIceId(chosen.getId());
                item.setIceName(chosen.getName());
            }
        }

        // ===== Topping =====
        if (toppingIds != null && !toppingIds.isEmpty()) {
            List<Topping> tops = toppingService.all().stream()
                    .filter(t -> toppingIds.contains(t.getId())).toList();
            item.setToppingIds(toppingIds);
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