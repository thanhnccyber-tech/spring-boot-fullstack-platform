package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.utetra.entity.*;
import vn.utetra.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepo;
    @Autowired private CategoryRepository categoryRepo;
    @Autowired private ToppingRepository toppingRepo;
    @Autowired private SizeOptionRepository sizeRepo;
    @Autowired private SugarLevelRepository sugarRepo;
    @Autowired private IceLevelRepository iceRepo;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private FileValidator fileValidator;
    @Autowired private ReviewRepository reviewRepo;
    @Autowired private BranchProductStockRepository branchProductStockRepo;

    public List<Product> featured() { return productRepo.findByFeaturedTrueAndActiveTrue(); }
    public List<Product> newArrivals() { return productRepo.findByIsNewTrueAndActiveTrue(); }

    public List<Product> findActiveInOrder(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        java.util.Map<Long, Product> map = new java.util.HashMap<>();
        for (Product p : productRepo.findAllById(ids)) {
            if (Boolean.TRUE.equals(p.getActive())) map.put(p.getId(), p);
        }
        List<Product> result = new java.util.ArrayList<>(ids.size());
        for (Long id : ids) {
            Product p = map.get(id);
            if (p != null) result.add(p);
        }
        return result;
    }

    public Page<Product> search(String kw, Long catId, BigDecimal min, BigDecimal max,
                                int page, int size, String sort) {
        String kwP = (kw == null || kw.isBlank()) ? null : kw.trim();
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : size;
        String s = sort == null ? "newest" : sort;

        switch (s) {
            case "bestSeller":
                return productRepo.searchBestSeller(kwP, catId, min, max,
                        PageRequest.of(safePage, safeSize));
            case "topRated":
                return productRepo.searchTopRated(kwP, catId, min, max,
                        PageRequest.of(safePage, safeSize));
            case "mostFavorite":
                return productRepo.searchMostFavorite(kwP, catId, min, max,
                        PageRequest.of(safePage, safeSize));
            default:
                return productRepo.search(kwP, catId, min, max,
                        PageRequest.of(safePage, safeSize, resolveSort(s)));
        }
    }

    public Page<Product> searchForAdmin(String kw, Long catId, Boolean active,
                                        BigDecimal min, BigDecimal max,
                                        int page, int size, String sort) {
        Sort sortBy = resolveSort(sort);
        return productRepo.searchForAdmin(
                (kw == null || kw.isBlank()) ? null : kw.trim(),
                catId, active, min, max,
                PageRequest.of(Math.max(page, 0), size, sortBy));
    }

    private Sort resolveSort(String sort) {
        Sort tie = Sort.by("id").descending();
        if ("priceAsc".equals(sort))  return Sort.by("basePrice").ascending().and(tie);
        if ("priceDesc".equals(sort)) return Sort.by("basePrice").descending().and(tie);
        if ("nameAsc".equals(sort))   return Sort.by("name").ascending().and(tie);
        if ("nameDesc".equals(sort))  return Sort.by("name").descending().and(tie);
        return Sort.by("createdAt").descending().and(tie);
    }

    public Product get(Long id) {
        return productRepo.findByIdWithDefaults(id).orElseThrow();
    }

    public Product getWithDetails(Long id) {
        return productRepo.findByIdWithDetails(id).orElseThrow();
    }

    public List<Product> allWithCategory() {
        return productRepo.findAllWithCategory();
    }

    @Transactional
    public Product save(Product product,
                        Long categoryId,
                        List<Long> toppingIds,
                        List<Long> sizeIds,
                        List<Long> sugarIds,
                        List<Long> iceIds,
                        Long defaultSizeId,
                        Long defaultSugarId,
                        Long defaultIceId,
                        boolean allowSize,
                        boolean allowSugar,
                        boolean allowIce,
                        MultipartFile imageFile) {

        if (imageFile != null && !imageFile.isEmpty()) {
            fileValidator.validateImage(imageFile);
        }

        if (categoryId != null)
            product.setCategory(categoryRepo.findById(categoryId).orElse(null));

        product.setToppings(toppingIds != null && !toppingIds.isEmpty()
                ? new LinkedHashSet<>(toppingRepo.findAllById(toppingIds))
                : new LinkedHashSet<>());

        product.setAllowSize(allowSize);
        product.setAllowSugar(allowSugar);
        product.setAllowIce(allowIce);

        product.setSizes(allowSize && sizeIds != null && !sizeIds.isEmpty()
                ? new LinkedHashSet<>(sizeRepo.findAllById(sizeIds))
                : new LinkedHashSet<>());

        product.setSugars(allowSugar && sugarIds != null && !sugarIds.isEmpty()
                ? new LinkedHashSet<>(sugarRepo.findAllById(sugarIds))
                : new LinkedHashSet<>());

        product.setIces(allowIce && iceIds != null && !iceIds.isEmpty()
                ? new LinkedHashSet<>(iceRepo.findAllById(iceIds))
                : new LinkedHashSet<>());

        product.setDefaultSize(allowSize && defaultSizeId != null
                ? sizeRepo.findById(defaultSizeId).orElse(null) : null);
        product.setDefaultSugar(allowSugar && defaultSugarId != null
                ? sugarRepo.findById(defaultSugarId).orElse(null) : null);
        product.setDefaultIce(allowIce && defaultIceId != null
                ? iceRepo.findById(defaultIceId).orElse(null) : null);

        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImageUrl(cloudinaryService.upload(imageFile, "products"));
        }

        return productRepo.save(product);
    }

    @Transactional
    public void toggle(Long id) {
        Product p = productRepo.findById(id).orElseThrow();
        p.setActive(!Boolean.TRUE.equals(p.getActive()));
        productRepo.save(p);
    }

    @Transactional
    public void toggleOutOfStock(Long id) {
        Product p = productRepo.findById(id).orElseThrow();
        p.setOutOfStock(!Boolean.TRUE.equals(p.getOutOfStock()));
        productRepo.save(p);
    }

    /* ==================== TỒN KHO THEO CHI NHÁNH ==================== */

    @Transactional
    public void toggleOutOfStockForBranch(Long productId, Long branchId) {
        if (branchId == null)
            throw new IllegalArgumentException("Tài khoản chưa được gán chi nhánh.");
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại."));
        BranchProductStock s = branchProductStockRepo
                .findByBranchIdAndProductId(branchId, productId)
                .orElseGet(() -> {
                    BranchProductStock n = new BranchProductStock();
                    n.setBranchId(branchId);
                    n.setProductId(productId);
                    n.setOutOfStock(Boolean.TRUE.equals(p.getOutOfStock()));
                    return n;
                });
        s.setOutOfStock(!Boolean.TRUE.equals(s.getOutOfStock()));
        s.setUpdatedAt(LocalDateTime.now());
        branchProductStockRepo.save(s);
    }

    @Transactional(readOnly = true)
    public Map<Long, Boolean> branchStockMap(Long branchId, Collection<Long> productIds) {
        if (branchId == null || productIds == null || productIds.isEmpty()) return Map.of();
        Map<Long, Boolean> m = new HashMap<>();
        for (BranchProductStock s : branchProductStockRepo
                .findByBranchIdAndProductIdIn(branchId, productIds)) {
            m.put(s.getProductId(), s.getOutOfStock());
        }
        return m;
    }

    /**
     * Map "productId → đang hết hàng" đã KẾT HỢP toàn hệ thống + chi nhánh.
     * Chỉ chứa các sản phẩm đang hết hàng, dùng để hiển thị badge trên danh sách.
     */
    @Transactional(readOnly = true)
    public Map<Long, Boolean> effectiveOOSMap(Long branchId, Collection<Product> products) {
        Map<Long, Boolean> result = new HashMap<>();
        if (products == null || products.isEmpty()) return result;

        List<Long> idsToCheck = products.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getOutOfStock()))
                .map(Product::getId)
                .toList();

        for (Product p : products) {
            if (Boolean.TRUE.equals(p.getOutOfStock())) result.put(p.getId(), true);
        }
        if (branchId != null && !idsToCheck.isEmpty()) {
            for (BranchProductStock s : branchProductStockRepo
                    .findByBranchIdAndProductIdIn(branchId, idsToCheck)) {
                if (Boolean.TRUE.equals(s.getOutOfStock())) {
                    result.put(s.getProductId(), true);
                }
            }
        }
        return result;
    }

    @Transactional
    public void delete(Long id) {
        reviewRepo.deleteByProductId(id);
        productRepo.deleteById(id);
    }
}