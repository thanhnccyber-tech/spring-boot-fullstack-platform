package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import vn.utetra.entity.Product;
import vn.utetra.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;

    public List<Product> featured() { return productRepository.findByFeaturedTrueAndActiveTrue(); }
    public List<Product> newArrivals() { return productRepository.findByIsNewTrueAndActiveTrue(); }

    public Page<Product> search(String kw, Long catId, BigDecimal min, BigDecimal max,
                                int page, int size, String sort) {
        Sort sortBy = "priceAsc".equals(sort) ? Sort.by("basePrice").ascending()
                    : "priceDesc".equals(sort) ? Sort.by("basePrice").descending()
                    : Sort.by("createdAt").descending();
        return productRepository.search(
                (kw == null || kw.isBlank()) ? null : kw.trim(),
                catId, min, max,
                PageRequest.of(page, size, sortBy));
    }

    public Product get(Long id) {
        return productRepository.findById(id).orElseThrow();
    }

    public Product save(Product p) { return productRepository.save(p); }
    public void delete(Long id) { productRepository.deleteById(id); }
    public List<Product> all() { return productRepository.findAll(); }
}