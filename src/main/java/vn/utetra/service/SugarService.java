package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.SugarLevel;
import vn.utetra.repository.SugarLevelRepository;

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
        if (s.getSortOrder() == null) s.setSortOrder(0);

        s.setName(name);
        s.setActive(active);
        return repo.save(s);
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