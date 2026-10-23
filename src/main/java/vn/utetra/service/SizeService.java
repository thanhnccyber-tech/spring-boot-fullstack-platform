package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.SizeOption;
import vn.utetra.repository.SizeOptionRepository;

import java.math.BigDecimal;
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
        if (s.getSortOrder() == null) s.setSortOrder(0);

        s.setName(name);
        s.setActive(active);
        return repo.save(s);
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