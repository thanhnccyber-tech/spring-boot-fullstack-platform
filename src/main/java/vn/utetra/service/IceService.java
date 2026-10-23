package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.IceLevel;
import vn.utetra.repository.IceLevelRepository;

import java.util.List;

@Service
public class IceService {

    @Autowired private IceLevelRepository repo;

    public List<IceLevel> active() { return repo.findByActiveTrueOrderBySortOrderAscIdAsc(); }
    public List<IceLevel> all()    { return repo.findAllByOrderBySortOrderAscIdAsc(); }
    public IceLevel get(Long id)   { return repo.findById(id).orElseThrow(); }

    @Transactional
    public IceLevel save(IceLevel s, boolean active) {
        String name = s.getName() == null ? "" : s.getName().trim();
        if (name.isEmpty())
            throw new IllegalArgumentException("Tên mức đá không được để trống.");
        if (s.getPercent() == null || s.getPercent() < 0 || s.getPercent() > 100)
            throw new IllegalArgumentException("Phần trăm phải từ 0 đến 100.");
        if (s.getSortOrder() == null) s.setSortOrder(0);

        s.setName(name);
        s.setActive(active);
        return repo.save(s);
    }

    @Transactional
    public void lock(Long id) {
        IceLevel s = repo.findById(id).orElseThrow();
        s.setActive(false);
        repo.save(s);
    }

    @Transactional
    public void unlock(Long id) {
        IceLevel s = repo.findById(id).orElseThrow();
        s.setActive(true);
        repo.save(s);
    }
}