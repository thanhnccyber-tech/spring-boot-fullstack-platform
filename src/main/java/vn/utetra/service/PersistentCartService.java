package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.dto.CartItem;
import vn.utetra.entity.PersistentCartItem;
import vn.utetra.entity.Product;
import vn.utetra.entity.User;
import vn.utetra.repository.PersistentCartItemRepository;
import vn.utetra.repository.ProductRepository;
import vn.utetra.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class PersistentCartService {

    @Autowired private PersistentCartItemRepository cartItemRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private CartService cartService;

    @Transactional(readOnly = true)
    public List<CartItem> load(Long userId) {
        List<CartItem> items = new ArrayList<>();
        for (PersistentCartItem stored : cartItemRepo.findByUserIdWithProduct(userId)) {
            Product product = stored.getProduct();
            items.add(cartService.build(product, stored.getQuantity(), stored.getSizeId(),
                    stored.getSugarId(), stored.getIceId(), stored.getToppingIds()));
        }
        return items;
    }

    @Transactional
    public void save(Long userId, List<CartItem> items) {
        if (items.size() > CartService.MAX_CART_ITEMS) {
            throw new IllegalArgumentException("Giỏ hàng tối đa " + CartService.MAX_CART_ITEMS + " món.");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        cartItemRepo.deleteByUserId(userId);
        for (CartItem item : items) {
            Product product = productRepo.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Một sản phẩm trong giỏ không còn tồn tại."));
            PersistentCartItem stored = new PersistentCartItem();
            stored.setUser(user);
            stored.setProduct(product);
            stored.setQuantity(CartService.clampQty(item.getQuantity()));
            stored.setSizeId(item.getSizeId());
            stored.setSugarId(item.getSugarId());
            stored.setIceId(item.getIceId());
            stored.setToppingIds(item.getToppingIds() == null
                    ? new ArrayList<>() : new ArrayList<>(item.getToppingIds()));
            cartItemRepo.save(stored);
        }
    }

    @Transactional
    public void mergeGuestCart(Long userId, List<CartItem> guestItems) {
        if (guestItems == null || guestItems.isEmpty()) return;
        List<CartItem> items = load(userId);
        if (items.size() + guestItems.size() > CartService.MAX_CART_ITEMS) {
            throw new IllegalArgumentException(
                    "Tổng giỏ hàng vượt quá " + CartService.MAX_CART_ITEMS
                            + " món. Hãy giảm số món trong giỏ khách trước khi đăng nhập.");
        }
        for (CartItem guestItem : guestItems) {
            items.add(guestItem);
        }
        save(userId, items);
    }

    @Transactional
    public void clear(Long userId) {
        cartItemRepo.deleteByUserId(userId);
    }
}
