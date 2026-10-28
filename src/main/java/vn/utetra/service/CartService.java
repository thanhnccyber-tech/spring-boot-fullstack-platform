package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.utetra.dto.CartItem;
import vn.utetra.entity.*;
import vn.utetra.repository.BranchProductStockRepository;
import vn.utetra.repository.BranchToppingStockRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CartService {

    public static final int MAX_QTY = 50;
    public static final int MAX_CART_ITEMS = 30;

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
    @Autowired private BranchProductStockRepository branchProductStockRepo;
    @Autowired private BranchToppingStockRepository branchToppingStockRepo;

    public static int clampQty(Integer q) {
        if (q == null) return 1;
        return Math.max(1, Math.min(q, MAX_QTY));
    }

    public CartItem build(Product p, Integer quantity, Long sizeId, Long sugarId, Long iceId,
                          List<Long> toppingIds) {
        return build(p, quantity, sizeId, sugarId, iceId, toppingIds, null);
    }

    public CartItem build(Product p, Integer quantity, Long sizeId, Long sugarId, Long iceId,
                          List<Long> toppingIds, Long branchId) {
        if (!Boolean.TRUE.equals(p.getActive()))
            throw new IllegalArgumentException("Sản phẩm này hiện không còn bán.");
        if (Boolean.TRUE.equals(p.getOutOfStock()))
            throw new IllegalArgumentException("Sản phẩm \"" + p.getName() + "\" hiện đang hết hàng, vui lòng chọn món khác.");

        if (branchId != null) {
            boolean branchOOS = branchProductStockRepo
                    .findByBranchIdAndProductId(branchId, p.getId())
                    .map(BranchProductStock::getOutOfStock)
                    .orElse(false);
            if (branchOOS)
                throw new IllegalArgumentException("Sản phẩm \"" + p.getName()
                        + "\" hiện đang hết hàng tại chi nhánh này, vui lòng chọn món khác.");
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
            Set<Long> branchOOSTops = branchId == null ? Set.of()
                    : branchToppingStockRepo.findByBranchIdAndToppingIdIn(branchId, toppingIds)
                            .stream()
                            .filter(s -> Boolean.TRUE.equals(s.getOutOfStock()))
                            .map(BranchToppingStock::getToppingId)
                            .collect(Collectors.toSet());

            List<Topping> tops = toppingService.activeInStock().stream()
                    .filter(t -> toppingIds.contains(t.getId()))
                    .filter(t -> !branchOOSTops.contains(t.getId()))
                    .toList();
            if (tops.size() != toppingIds.size()) {
                throw new IllegalArgumentException("Một số topping đã hết hàng hoặc không còn bán"
                        + (branchId != null ? " tại chi nhánh này" : "")
                        + ". Vui lòng chọn lại.");
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

    /**
     * Kiểm tra giỏ hàng so với chi nhánh đang chọn — trả về Map&lt;index, cảnh báo&gt;
     * để hiển thị cảnh báo trên từng dòng.
     */
    public Map<Integer, String> checkCartAgainstBranch(List<CartItem> cart, Long branchId) {
        if (branchId == null || cart == null || cart.isEmpty()) return Map.of();
        Map<Integer, String> warnings = new HashMap<>();

        Set<Long> pids = cart.stream()
                .map(CartItem::getProductId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> pOOS = new HashSet<>();
        for (BranchProductStock s : branchProductStockRepo.findByBranchIdAndProductIdIn(branchId, pids)) {
            if (Boolean.TRUE.equals(s.getOutOfStock())) pOOS.add(s.getProductId());
        }

        for (int i = 0; i < cart.size(); i++) {
            CartItem ci = cart.get(i);
            List<String> msgs = new ArrayList<>();
            if (ci.getProductId() != null && pOOS.contains(ci.getProductId())) {
                msgs.add("Sản phẩm đã hết hàng tại chi nhánh đã chọn");
            }
            if (ci.getToppingIds() != null && !ci.getToppingIds().isEmpty()) {
                Set<Long> branchOOS = branchToppingStockRepo
                        .findByBranchIdAndToppingIdIn(branchId, ci.getToppingIds())
                        .stream()
                        .filter(s -> Boolean.TRUE.equals(s.getOutOfStock()))
                        .map(BranchToppingStock::getToppingId)
                        .collect(Collectors.toSet());
                if (!branchOOS.isEmpty()) msgs.add("Một số topping đã hết hàng tại chi nhánh");
            }
            if (!msgs.isEmpty()) warnings.put(i, String.join(". ", msgs));
        }
        return warnings;
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