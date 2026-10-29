package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.SugarLevel;
import vn.utetra.repository.SugarLevelRepository;

import java.util.Collections;
import java.util.List;

@Service
public class SugarService {

    @Autowired private SugarLevelRepository repo;

    public List<SugarLevel> active() { return repo.findByActiveTrueOrderBySortOrderAscIdAsc(); }
    public List<SugarLevel> all()    { return repo.findAllByOrderBySortOrderAscIdAsc(); }
    public SugarLevel get(Long id)   { return repo.findById(id).orElseThrow(); }

    @Transactional
    public SugarLevel save(SugarLevel s, boolean active) {
        String name = s.getName() == null ? "" : s.getName().trim();
        if (name.isEmpty())
            throw new IllegalArgumentException("Tên mức đường không được để trống.");
        if (s.getPercent() == null || s.getPercent() < 0 || s.getPercent() > 200)
            throw new IllegalArgumentException("Phần trăm phải từ 0 đến 200.");

        if (s.getId() == null) {
            List<SugarLevel> all = repo.findAllByOrderBySortOrderAscIdAsc();
            int max = all.stream()
                    .mapToInt(x -> x.getSortOrder() == null ? 0 : x.getSortOrder())
                    .max().orElse(-1);
            s.setSortOrder(max + 1);
        } else {
            SugarLevel existing = repo.findById(s.getId()).orElseThrow();
            s.setSortOrder(existing.getSortOrder() == null ? 0 : existing.getSortOrder());
        }

        s.setName(name);
        s.setActive(active);
        return repo.save(s);
    }

    /** Di chuyển lên/xuống 1 bậc rồi đánh lại sortOrder = 0,1,2,... để không trùng. */
    @Transactional
    public void move(Long id, int direction) {
        List<SugarLevel> all = repo.findAllByOrderBySortOrderAscIdAsc();
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
        SugarLevel s = repo.findById(id).orElseThrow();
        s.setActive(false);
        repo.save(s);
    }

    @Transactional
    public void unlock(Long id) {
        SugarLevel s = repo.findById(id).orElseThrow();
        s.setActive(true);
        repo.save(s);
    }
}