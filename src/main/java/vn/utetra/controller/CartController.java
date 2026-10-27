package vn.utetra.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.utetra.dto.CartItem;
import vn.utetra.entity.Product;
import vn.utetra.security.SafeRedirect;
import vn.utetra.service.CartService;
import vn.utetra.service.ProductService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Giỏ hàng lưu trong phiên (HttpSession) nên khách CHƯA đăng nhập vẫn thêm món được;
 * khi đăng nhập, phiên được giữ nguyên (chỉ đổi id) nên giỏ tự "gộp" sang tài khoản.
 * Bước thanh toán (/order/**) mới bắt buộc đăng nhập.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

	private static final String CART = "CART";

	@Autowired private ProductService productService;
	@Autowired private CartService cartService;
	@Autowired private vn.utetra.service.PersistentCartService persistentCartService;

	@SuppressWarnings("unchecked")
	private List<CartItem> sessionCart(HttpSession session) {
		Object o = session.getAttribute(CART);
		if (o == null) {
			List<CartItem> cart = new ArrayList<>();
			session.setAttribute(CART, cart);
			return cart;
		}
		return (List<CartItem>) o;
	}

	private static Long userId(HttpSession session) {
		Object id = session.getAttribute("userId");
		return id instanceof Long value ? value : null;
	}

	private List<CartItem> currentCart(HttpSession session) {
		Long userId = userId(session);
		if (userId == null) return sessionCart(session);
		List<CartItem> cart = persistentCartService.load(userId);
		session.setAttribute(CART, cart);
		return cart;
	}

	private void saveIfLoggedIn(HttpSession session, List<CartItem> cart) {
		Long userId = userId(session);
		if (userId != null) persistentCartService.save(userId, cart);
	}

	@GetMapping
	public String viewCart(HttpSession session, Model model) {
		List<CartItem> cart = currentCart(session);
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
		try {
			List<CartItem> cart = currentCart(session);
			CartItem newItem = cartService.build(p, quantity, sizeId, sugarId, iceId, toppingIds);
			if (!CartService.addOrMerge(cart, newItem)) {
				ra.addFlashAttribute("error", "Giỏ hàng tối đa " + CartService.MAX_CART_ITEMS + " món.");
				return "redirect:/cart";
			}
			saveIfLoggedIn(session, cart);
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/product/" + p.getId();
		}
		ra.addFlashAttribute("success", "Đã thêm \"" + p.getName() + "\" vào giỏ hàng.");
		return "redirect:/cart";
	}

	/** Trang sửa lựa chọn (size / đường / đá / topping / số lượng) của một món đã có trong giỏ. */
	@GetMapping("/edit")
	public String editPage(@RequestParam int index, HttpSession session, Model model, RedirectAttributes ra) {
		List<CartItem> cart = currentCart(session);
		if (index < 0 || index >= cart.size()) return "redirect:/cart";
		CartItem item = cart.get(index);
		Product p;
		try {
			p = productService.get(item.getProductId());
		} catch (Exception e) {
			ra.addFlashAttribute("error", "Món này không còn trong thực đơn.");
			return "redirect:/cart";
		}
		model.addAttribute("index", index);
		model.addAttribute("item", item);
		model.addAttribute("product", p);
		model.addAttribute("sizes", Boolean.TRUE.equals(p.getAllowSize()) ? cartService.sizesFor(p) : List.of());
		model.addAttribute("sugars", Boolean.TRUE.equals(p.getAllowSugar()) ? cartService.sugarsFor(p) : List.of());
		model.addAttribute("ices", Boolean.TRUE.equals(p.getAllowIce()) ? cartService.icesFor(p) : List.of());
		model.addAttribute("toppings", cartService.toppings());
		return "user/cart-edit";
	}

	@PostMapping("/edit")
	public String editSave(@RequestParam int index, @RequestParam(defaultValue = "1") Integer quantity,
			@RequestParam(required = false) Long sizeId, @RequestParam(required = false) Long sugarId,
			@RequestParam(required = false) Long iceId, @RequestParam(required = false) List<Long> toppingIds,
			@RequestParam(required = false) String back,
			HttpSession session, RedirectAttributes ra) {
		List<CartItem> cart = currentCart(session);
		if (index < 0 || index >= cart.size()) return "redirect:/cart";
		try {
			Product p = productService.get(cart.get(index).getProductId());
			CartItem edited = cartService.build(p, quantity, sizeId, sugarId, iceId, toppingIds);
			cart.set(index, edited);
			// Sửa xong mà trùng với một dòng khác thì gộp lại thành một dòng
			for (int j = 0; j < cart.size(); j++) {
				if (j != index && CartService.sameLine(cart.get(j), edited)) {
					cart.get(j).setQuantity(CartService.clampQty(cart.get(j).getQuantity() + edited.getQuantity()));
					cart.remove(index);
					break;
				}
			}
			saveIfLoggedIn(session, cart);
			ra.addFlashAttribute("success", "Đã cập nhật món trong giỏ.");
		} catch (IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/cart/edit?index=" + index;
		} catch (RuntimeException e) {
			ra.addFlashAttribute("error", "Không cập nhật được món này (có thể đã ngừng bán).");
		}
		return "redirect:" + SafeRedirect.localPath(back, "/cart");
	}

	/** Xoá món (POST để không bị link từ trang khác tự xoá giỏ). back = trang quay lại (chỉ đường dẫn nội bộ). */
	@PostMapping("/remove")
	public String remove(@RequestParam int index,
			@RequestParam(required = false) String back, HttpSession session) {
		List<CartItem> cart = currentCart(session);
		if (index >= 0 && index < cart.size())
			cart.remove(index);
		saveIfLoggedIn(session, cart);
		return "redirect:" + SafeRedirect.localPath(back, "/cart");
	}

	@PostMapping("/update")
	public String update(@RequestParam int index, @RequestParam int quantity,
			@RequestParam(required = false) String back, HttpSession session) {
		List<CartItem> cart = currentCart(session);
		if (index >= 0 && index < cart.size()) {
			if (quantity < 1) {
				cart.remove(index); // bấm "−" khi còn 1 ly = bỏ món
			} else {
				cart.get(index).setQuantity(CartService.clampQty(quantity));
			}
		}
		saveIfLoggedIn(session, cart);
		return "redirect:" + SafeRedirect.localPath(back, "/cart");
	}
}