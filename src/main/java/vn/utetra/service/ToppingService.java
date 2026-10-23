package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.Topping;
import vn.utetra.repository.ToppingRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ToppingService {

    private static final BigDecimal MAX_PRICE = new BigDecimal("10000000");

    @Autowired private ToppingRepository repo;

    public List<Topping> active() {
        return repo.findByActiveTrue();
    }

    public List<Topping> activeInStock() {
        return repo.findByActiveTrueAndOutOfStockFalse();
    }

    public List<Topping> all() {
        return repo.findAll();
    }

    public Page<Topping> searchForAdmin(String kw, Boolean active, Boolean outOfStock,
                                        int page, int size) {
        String kwP = (kw == null || kw.isBlank()) ? null : kw.trim();
        Sort sort = Sort.by("id").descending();
        return repo.searchForAdmin(kwP, active, outOfStock,
                PageRequest.of(Math.max(page, 0), size, sort));
    }

    public Topping get(Long id) {
        return repo.findById(id).orElseThrow();
    }

    @Transactional
    public Topping save(Topping t) {
        String name = t.getName() == null ? "" : t.getName().trim();
        if (name.isEmpty())
            throw new IllegalArgumentException("Tên topping không được để trống.");
        if (name.length() > 100)
            throw new IllegalArgumentException("Tên topping tối đa 100 ký tự.");

        if (t.getPrice() == null)
            throw new IllegalArgumentException("Giá topping không được để trống.");
        if (t.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Giá topping phải lớn hơn 0.");
        if (t.getPrice().compareTo(MAX_PRICE) > 0)
            throw new IllegalArgumentException("Giá topping không được vượt quá 10.000.000 ₫.");

        repo.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (t.getId() == null || !existing.getId().equals(t.getId()))
                throw new IllegalArgumentException("Tên topping đã tồn tại.");
        });

        t.setName(name);
        if (t.getActive() == null) t.setActive(true);
        if (t.getOutOfStock() == null) t.setOutOfStock(false);

        return repo.save(t);
    }

    @Transactional
    public void toggleActive(Long id) {
        Topping t = repo.findById(id).orElseThrow();
        t.setActive(!Boolean.TRUE.equals(t.getActive()));
        repo.save(t);
    }

    @Transactional
    public void toggleOutOfStock(Long id) {
        Topping t = repo.findById(id).orElseThrow();
        t.setOutOfStock(!Boolean.TRUE.equals(t.getOutOfStock()));
        repo.save(t);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}