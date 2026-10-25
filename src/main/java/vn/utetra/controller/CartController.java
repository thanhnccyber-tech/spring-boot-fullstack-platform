package vn.utetra.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.dto.CartItem;
import vn.utetra.entity.*;
import vn.utetra.security.SafeRedirect;
import vn.utetra.service.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

	private static final String CART = "CART";
	private static final int MAX_QTY = 50;
	private static final int MAX_CART_ITEMS = 30;

	/** Ép số lượng về 1..MAX_QTY (chống số âm / số quá lớn làm sai tổng tiền). */
	private static int clampQty(Integer q) {
		if (q == null) return 1;
		return Math.max(1, Math.min(q, MAX_QTY));
	}

	@Autowired
	private ProductService productService;
	@Autowired
	private ToppingService toppingService;
	@Autowired
	private SizeService sizeService;
	@Autowired
	private SugarService sugarService;
	@Autowired
	private IceService iceService;

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
		BigDecimal total = cart.stream().map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		model.addAttribute("cart", cart);
		model.addAttribute("total", total);
		return "user/cart";
	}

	@PostMapping("/add")
	public String addToCart(@RequestParam Long productId, @RequestParam(defaultValue = "1") Integer quantity,
			@RequestParam(required = false) Long sizeId, @RequestParam(required = false) Long sugarId,
			@RequestParam(required = false) Long iceId, @RequestParam(required = false) List<Long> toppingIds,
			HttpSession session, RedirectAttributes ra) {

		Product p = productService.get(productId);

		if (!Boolean.TRUE.equals(p.getActive())) {
			ra.addFlashAttribute("error", "Sản phẩm này hiện không còn bán.");
			return "redirect:/products";
		}
		if (getCart(session).size() >= MAX_CART_ITEMS) {
			ra.addFlashAttribute("error", "Giỏ hàng tối đa " + MAX_CART_ITEMS + " món.");
			return "redirect:/cart";
		}

		// Chặn thêm sản phẩm hết hàng vào giỏ
		if (Boolean.TRUE.equals(p.getOutOfStock())) {
			ra.addFlashAttribute("error",
					"Sản phẩm \"" + p.getName() + "\" hiện đang hết hàng, vui lòng chọn món khác.");
			return "redirect:/product/" + p.getId();
		}

		CartItem item = new CartItem();
		item.setProductId(p.getId());
		item.setProductName(p.getName());
		item.setImageUrl(p.getImageUrl());
		item.setBasePrice(p.getBasePrice());
		item.setQuantity(clampQty(quantity));

		BigDecimal unit = p.getBasePrice();

		if (Boolean.TRUE.equals(p.getAllowSize())) {
			SizeOption chosen = null;
			if (sizeId != null) {
				chosen = sizeService.active().stream().filter(x -> x.getId().equals(sizeId)).findFirst().orElse(null);
			}
			if (chosen == null && p.getDefaultSize() != null && Boolean.TRUE.equals(p.getDefaultSize().getActive())) {
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

		if (Boolean.TRUE.equals(p.getAllowSugar())) {
			SugarLevel chosen = null;
			if (sugarId != null) {
				chosen = sugarService.active().stream().filter(x -> x.getId().equals(sugarId)).findFirst().orElse(null);
			}
			if (chosen == null && p.getDefaultSugar() != null && Boolean.TRUE.equals(p.getDefaultSugar().getActive())) {
				chosen = p.getDefaultSugar();
			}
			if (chosen != null) {
				item.setSugarId(chosen.getId());
				item.setSugarName(chosen.getName());
			}
		}

		if (Boolean.TRUE.equals(p.getAllowIce())) {
			IceLevel chosen = null;
			if (iceId != null) {
				chosen = iceService.active().stream().filter(x -> x.getId().equals(iceId)).findFirst().orElse(null);
			}
			if (chosen == null && p.getDefaultIce() != null && Boolean.TRUE.equals(p.getDefaultIce().getActive())) {
				chosen = p.getDefaultIce();
			}
			if (chosen != null) {
				item.setIceId(chosen.getId());
				item.setIceName(chosen.getName());
			}
		}

		if (toppingIds != null && !toppingIds.isEmpty()) {
			List<Topping> tops = toppingService.activeInStock().stream().filter(t -> toppingIds.contains(t.getId()))
					.toList();
			if (tops.size() != toppingIds.size()) {
				ra.addFlashAttribute("error", "Một số topping đã hết hàng hoặc không còn bán. Vui lòng chọn lại.");
				return "redirect:/product/" + p.getId();
			}
			item.setToppingIds(toppingIds);
			item.setToppingNames(tops.stream().map(Topping::getName).toList());
			BigDecimal tSum = tops.stream().map(Topping::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
			item.setToppingPrice(tSum);
			unit = unit.add(tSum);
		}

		item.setUnitPrice(unit);
		getCart(session).add(item);
		ra.addFlashAttribute("success", "Đã thêm \"" + p.getName() + "\" vào giỏ hàng.");
		return "redirect:/cart";
	}

	/** Xoá món (POST để không bị link từ trang khác tự xoá giỏ). back = trang quay lại (chỉ đường dẫn nội bộ). */
	@PostMapping("/remove")
	public String remove(@RequestParam int index,
			@RequestParam(required = false) String back, HttpSession session) {
		List<CartItem> cart = getCart(session);
		if (index >= 0 && index < cart.size())
			cart.remove(index);
		return "redirect:" + SafeRedirect.localPath(back, "/cart");
	}

	@PostMapping("/update")
	public String update(@RequestParam int index, @RequestParam int quantity,
			@RequestParam(required = false) String back, HttpSession session) {
		List<CartItem> cart = getCart(session);
		if (index >= 0 && index < cart.size()) {
			if (quantity < 1) {
				cart.remove(index); // bấm "−" khi còn 1 ly = bỏ món
			} else {
				cart.get(index).setQuantity(clampQty(quantity));
			}
		}
		return "redirect:" + SafeRedirect.localPath(back, "/cart");
	}
}