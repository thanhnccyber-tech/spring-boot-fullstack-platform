package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.utetra.entity.*;
import vn.utetra.repository.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

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

    public List<Product> featured() { return productRepo.findByFeaturedTrueAndActiveTrue(); }
    public List<Product> newArrivals() { return productRepo.findByIsNewTrueAndActiveTrue(); }

    public Page<Product> search(String kw, Long catId, BigDecimal min, BigDecimal max,
                                int page, int size, String sort) {
        Sort sortBy = resolveSort(sort);
        return productRepo.search(
                (kw == null || kw.isBlank()) ? null : kw.trim(),
                catId, min, max,
                PageRequest.of(page, size, sortBy));
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
        if ("priceAsc".equals(sort))  return Sort.by("basePrice").ascending();
        if ("priceDesc".equals(sort)) return Sort.by("basePrice").descending();
        if ("nameAsc".equals(sort))   return Sort.by("name").ascending();
        if ("nameDesc".equals(sort))  return Sort.by("name").descending();
        return Sort.by("createdAt").descending();
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

    @Transactional
    public void delete(Long id) {
        productRepo.deleteById(id);
    }
}