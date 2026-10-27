package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.utetra.dto.CartItem;
import vn.utetra.entity.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Dựng một dòng giỏ hàng từ món + lựa chọn của khách. Dùng chung cho: thêm vào giỏ,
 * sửa món trong giỏ và "đặt lại" đơn cũ, để giá luôn tính theo cùng một quy tắc (giá hiện tại ở server).
 */
@Service
public class CartService {

    public static final int MAX_QTY = 50;
    public static final int MAX_CART_ITEMS = 30;

    /** Hai dòng giỏ giống nhau khi cùng món, size, độ ngọt, đá và bộ topping (không phân biệt thứ tự). */
    public static boolean sameLine(vn.utetra.dto.CartItem a, vn.utetra.dto.CartItem b) {
        return java.util.Objects.equals(a.getProductId(), b.getProductId())
                && java.util.Objects.equals(a.getSizeId(), b.getSizeId())
                && java.util.Objects.equals(a.getSugarId(), b.getSugarId())
                && java.util.Objects.equals(a.getIceId(), b.getIceId())
                && toppingSet(a).equals(toppingSet(b));
    }

    private static java.util.Set<Long> toppingSet(vn.utetra.dto.CartItem c) {
        return c.getToppingIds() == null ? java.util.Set.of() : new java.util.HashSet<>(c.getToppingIds());
    }

    /** Thêm vào giỏ: nếu đã có dòng giống hệt thì cộng dồn số lượng, ngược lại thêm dòng mới. Trả về false nếu giỏ đầy. */
    public static boolean addOrMerge(java.util.List<vn.utetra.dto.CartItem> cart, vn.utetra.dto.CartItem item) {
        for (vn.utetra.dto.CartItem ex : cart) {
            if (sameLine(ex, item)) {
                int q = (ex.getQuantity() == null ? 0 : ex.getQuantity()) + (item.getQuantity() == null ? 1 : item.getQuantity());
                ex.setQuantity(clampQty(q));
                return true;
            }
        }
        if (cart.size() >= MAX_CART_ITEMS) return false;
        cart.add(item);
        return true;
    }

    @Autowired private ToppingService toppingService;
    @Autowired private SizeService sizeService;
    @Autowired private SugarService sugarService;
    @Autowired private IceService iceService;

    public static int clampQty(Integer q) {
        if (q == null) return 1;
        return Math.max(1, Math.min(q, MAX_QTY));
    }

    /** @throws IllegalArgumentException khi món/topping không còn bán (thông báo hiển thị được cho khách) */
    public CartItem build(Product p, Integer quantity, Long sizeId, Long sugarId, Long iceId,
                          List<Long> toppingIds) {
        if (!Boolean.TRUE.equals(p.getActive()))
            throw new IllegalArgumentException("Sản phẩm này hiện không còn bán.");
        if (Boolean.TRUE.equals(p.getOutOfStock()))
            throw new IllegalArgumentException("Sản phẩm \"" + p.getName() + "\" hiện đang hết hàng, vui lòng chọn món khác.");

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
            List<Topping> tops = toppingService.activeInStock().stream()
                    .filter(t -> toppingIds.contains(t.getId())).toList();
            if (tops.size() != toppingIds.size()) {
                throw new IllegalArgumentException("Một số topping đã hết hàng hoặc không còn bán. Vui lòng chọn lại.");
            }
            item.setToppingIds(toppingIds);
            item.setToppingNames(tops.stream().map(Topping::getName).toList());
            BigDecimal tSum = tops.stream().map(Topping::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setToppingPrice(tSum);
            unit = unit.add(tSum);
        }

        item.setUnitPrice(unit);
        return item;
    }

    /**
     * "Đặt lại" một dòng của đơn cũ: dò lại size/đường/đá theo tên đã lưu, topping còn bán thì giữ.
     * Trả null nếu món không còn bán.
     */
    public CartItem rebuild(OrderDetail d) {
        Product p = d.getProduct();
        if (p == null) return null;
        Long sizeId = null, sugarId = null, iceId = null;
        if (d.getSize() != null) {
            sizeId = sizeService.active().stream().filter(x -> d.getSize().equals(x.getName()))
                    .map(SizeOption::getId).findFirst().orElse(null);
        }
        if (d.getSugarLevel() != null) {
            sugarId = sugarService.active().stream().filter(x -> d.getSugarLevel().equals(x.getName()))
                    .map(SugarLevel::getId).findFirst().orElse(null);
        }
        if (d.getIceLevel() != null) {
            iceId = iceService.active().stream().filter(x -> d.getIceLevel().equals(x.getName()))
                    .map(IceLevel::getId).findFirst().orElse(null);
        }
        List<Long> tops = new ArrayList<>();
        if (d.getToppings() != null) {
            List<Long> available = toppingService.activeInStock().stream().map(Topping::getId).toList();
            for (Topping t : d.getToppings()) if (available.contains(t.getId())) tops.add(t.getId());
        }
        try {
            return build(p, d.getQuantity(), sizeId, sugarId, iceId, tops);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /* ---------- Danh sách lựa chọn cho trang sửa món ---------- */

    public List<SizeOption> sizesFor(Product p) {
        List<SizeOption> src = (p.getSizes() == null || p.getSizes().isEmpty())
                ? sizeService.active() : new ArrayList<>(p.getSizes());
        return src.stream().filter(SizeOption::getActive)
                .sorted(Comparator.comparing(SizeOption::getSortOrder).thenComparing(SizeOption::getId)).toList();
    }

    public List<SugarLevel> sugarsFor(Product p) {
        List<SugarLevel> src = (p.getSugars() == null || p.getSugars().isEmpty())
                ? sugarService.active() : new ArrayList<>(p.getSugars());
        return src.stream().filter(SugarLevel::getActive)
                .sorted(Comparator.comparing(SugarLevel::getSortOrder).thenComparing(SugarLevel::getId)).toList();
    }

    public List<IceLevel> icesFor(Product p) {
        List<IceLevel> src = (p.getIces() == null || p.getIces().isEmpty())
                ? iceService.active() : new ArrayList<>(p.getIces());
        return src.stream().filter(IceLevel::getActive)
                .sorted(Comparator.comparing(IceLevel::getSortOrder).thenComparing(IceLevel::getId)).toList();
    }

    public List<Topping> toppings() { return toppingService.activeInStock(); }
}
