package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.Category;
import vn.utetra.repository.CategoryRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryService {

    @Autowired private CategoryRepository repo;

    public List<Category> active() {
        return repo.findByActiveTrueOrderBySortOrderAscIdAsc();
    }

    public List<Category> all() {
        return repo.findAllByOrderBySortOrderAscIdAsc();
    }

    public Page<Category> searchForAdmin(String kw, Boolean active, int page, int size) {
        String kwP = (kw == null || kw.isBlank()) ? null : kw.trim();
        Sort sort = Sort.by("sortOrder").ascending().and(Sort.by("id").ascending());
        return repo.searchForAdmin(kwP, active,
                PageRequest.of(Math.max(page, 0), size, sort));
    }

    public Category get(Long id) {
        return repo.findById(id).orElseThrow();
    }

    @Transactional
    public Category save(Category c) {
        String name = c.getName() == null ? "" : c.getName().trim();
        if (name.isEmpty())
            throw new IllegalArgumentException("Tên danh mục không được để trống.");
        if (name.length() > 100)
            throw new IllegalArgumentException("Tên danh mục tối đa 100 ký tự.");
        c.setName(name);

        if (c.getDescription() != null && c.getDescription().length() > 2000)
            throw new IllegalArgumentException("Mô tả tối đa 2000 ký tự.");

        // sortOrder không còn nhập tay: tạo mới → cuối danh sách, sửa → giữ nguyên thứ tự cũ
        if (c.getId() == null) {
            List<Category> all = repo.findAllByOrderBySortOrderAscIdAsc();
            int max = all.stream()
                    .mapToInt(x -> x.getSortOrder() == null ? 0 : x.getSortOrder())
                    .max().orElse(-1);
            c.setSortOrder(max + 1);
        } else {
            Category existing = repo.findById(c.getId()).orElseThrow();
            c.setSortOrder(existing.getSortOrder() == null ? 0 : existing.getSortOrder());
        }
        if (c.getActive() == null) c.setActive(true);

        return repo.save(c);
    }

    /**
     * Di chuyển danh mục lên (direction = -1) hoặc xuống (direction = 1) một bậc,
     * sau đó đánh lại sortOrder = 0,1,2,... cho toàn bộ danh sách để không bao giờ bị trùng.
     */
    @Transactional
    public void move(Long id, int direction) {
        List<Category> all = repo.findAllByOrderBySortOrderAscIdAsc();
        int index = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(id)) { index = i; break; }
        }
        if (index < 0) return;
        int target = index + direction;
        if (target < 0 || target >= all.size()) return;

        Collections.swap(all, index, target);
        for (int i = 0; i < all.size(); i++) all.get(i).setSortOrder(i);
        repo.saveAll(all);
    }

    @Transactional
    public void toggleActive(Long id) {
        Category c = repo.findById(id).orElseThrow();
        c.setActive(!Boolean.TRUE.equals(c.getActive()));
        repo.save(c);
    }

    @Transactional
    public void delete(Long id) {
        long count = repo.countProductsByCategory(id);
        if (count > 0)
            throw new IllegalStateException(
                    "Không thể xóa: danh mục còn " + count + " sản phẩm. " +
                    "Hãy ẩn danh mục hoặc chuyển sản phẩm sang danh mục khác.");
        repo.deleteById(id);
    }

    public Map<Long, Long> countProductsByCategoryIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : repo.countProductsByCategoryIds(ids)) {
            map.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return map;
    }
}