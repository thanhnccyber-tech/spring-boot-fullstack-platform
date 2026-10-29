package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.SizeOption;
import vn.utetra.repository.SizeOptionRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
public class SizeService {

    @Autowired private SizeOptionRepository repo;

    public List<SizeOption> active()    { return repo.findByActiveTrueOrderBySortOrderAscIdAsc(); }
    public List<SizeOption> all()       { return repo.findAllByOrderBySortOrderAscIdAsc(); }
    public SizeOption get(Long id)      { return repo.findById(id).orElseThrow(); }

    @Transactional
    public SizeOption save(SizeOption s, boolean active) {
        String name = s.getName() == null ? "" : s.getName().trim();
        if (name.isEmpty())
            throw new IllegalArgumentException("Tên size không được để trống.");
        if (s.getExtraPrice() == null || s.getExtraPrice().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Phụ thu phải ≥ 0.");

        if (s.getId() == null) {
            List<SizeOption> all = repo.findAllByOrderBySortOrderAscIdAsc();
            int max = all.stream()
                    .mapToInt(x -> x.getSortOrder() == null ? 0 : x.getSortOrder())
                    .max().orElse(-1);
            s.setSortOrder(max + 1);
        } else {
            SizeOption existing = repo.findById(s.getId()).orElseThrow();
            s.setSortOrder(existing.getSortOrder() == null ? 0 : existing.getSortOrder());
        }

        s.setName(name);
        s.setActive(active);
        return repo.save(s);
    }

    /** Di chuyển lên/xuống 1 bậc rồi đánh lại sortOrder = 0,1,2,... để không trùng. */
    @Transactional
    public void move(Long id, int direction) {
        List<SizeOption> all = repo.findAllByOrderBySortOrderAscIdAsc();
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
    public void lock(Long id) {
        SizeOption s = repo.findById(id).orElseThrow();
        s.setActive(false);
        repo.save(s);
    }

    @Transactional
    public void unlock(Long id) {
        SizeOption s = repo.findById(id).orElseThrow();
        s.setActive(true);
        repo.save(s);
    }
}