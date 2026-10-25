package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.Branch;
import vn.utetra.repository.BranchRepository;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class BranchService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[0-9]{9,10}$");

    @Autowired private BranchRepository repo;

    public List<Branch> active() { return repo.findByActiveTrue(); }
    public List<Branch> all()    { return repo.findAll(); }
    public Branch get(Long id)   { return repo.findById(id).orElseThrow(); }

    public Page<Branch> searchForAdmin(String kw, Boolean active, int page, int size) {
        String kwP = (kw == null || kw.isBlank()) ? null : kw.trim();
        Sort sort = Sort.by("id").ascending();
        return repo.searchForAdmin(kwP, active,
                PageRequest.of(Math.max(page, 0), size, sort));
    }

    @Transactional
    public Branch save(Branch b, boolean active) {
        String name    = b.getName()    == null ? "" : b.getName().trim();
        String address = b.getAddress() == null ? "" : b.getAddress().trim();
        String phone   = b.getPhone()   == null ? "" : b.getPhone().trim();

        if (name.isEmpty())
            throw new IllegalArgumentException("Tên chi nhánh không được để trống.");
        if (name.length() > 150)
            throw new IllegalArgumentException("Tên chi nhánh không được vượt quá 150 ký tự.");
        if (address.isEmpty())
            throw new IllegalArgumentException("Địa chỉ không được để trống.");
        if (address.length() > 255)
            throw new IllegalArgumentException("Địa chỉ không được vượt quá 255 ký tự.");
        if (phone.isEmpty())
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        if (!PHONE_PATTERN.matcher(phone).matches())
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (phải bắt đầu bằng 0, từ 10 đến 11 chữ số).");

        // Toạ độ: nhập cả hai hoặc bỏ trống cả hai
        if ((b.getLatitude() == null) != (b.getLongitude() == null))
            throw new IllegalArgumentException("Hãy nhập đủ cả vĩ độ và kinh độ (hoặc để trống cả hai).");
        if (b.getLatitude() != null && (!vn.utetra.util.Geo.validLat(b.getLatitude())
                || !vn.utetra.util.Geo.validLon(b.getLongitude())))
            throw new IllegalArgumentException("Toạ độ không hợp lệ (vĩ độ -90..90, kinh độ -180..180).");

        repo.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (b.getId() == null || !existing.getId().equals(b.getId()))
                throw new IllegalArgumentException("Tên chi nhánh đã tồn tại.");
        });

        b.setName(name);
        b.setAddress(address);
        b.setPhone(phone);
        b.setActive(active);
        return repo.save(b);
    }

    @Transactional
    public void toggleActive(Long id) {
        Branch b = repo.findById(id).orElseThrow();
        b.setActive(!Boolean.TRUE.equals(b.getActive()));
        repo.save(b);
    }

    @Transactional
    public void lock(Long id) {
        Branch b = repo.findById(id).orElseThrow();
        b.setActive(false);
        repo.save(b);
    }

    @Transactional
    public void unlock(Long id) {
        Branch b = repo.findById(id).orElseThrow();
        b.setActive(true);
        repo.save(b);
    }
}