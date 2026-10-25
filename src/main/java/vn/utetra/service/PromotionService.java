package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.entity.Promotion;
import vn.utetra.repository.PromotionRepository;

import java.util.List;

@Service
public class PromotionService {

    @Autowired private PromotionRepository repo;

    public List<Promotion> active() { return repo.findByActiveTrueOrderByCreatedAtDescIdDesc(); }
    public List<Promotion> all()    { return repo.findAllByOrderByCreatedAtDescIdDesc(); }
    public Promotion get(Long id)   { return repo.findById(id).orElseThrow(); }

    /** Xem bài viết công khai: chỉ bài đang hiển thị, đồng thời tăng lượt xem. */
    @Transactional
    public Promotion view(Long id) {
        Promotion p = repo.findById(id).filter(x -> Boolean.TRUE.equals(x.getActive())).orElseThrow();
        repo.increaseViews(id);
        return p;
    }

    @Transactional
    public void save(Promotion p, String title, String summary, String content,
                     String imageUrl, String voucherCode, boolean active) {
        String t = title == null ? "" : title.trim();
        if (t.isEmpty() || t.length() > 200)
            throw new IllegalArgumentException("Tiêu đề bắt buộc, tối đa 200 ký tự.");
        if (summary != null && summary.length() > 500)
            throw new IllegalArgumentException("Tóm tắt tối đa 500 ký tự.");
        if (content != null && content.length() > 20000)
            throw new IllegalArgumentException("Nội dung quá dài.");
        String img = imageUrl == null ? "" : imageUrl.trim();
        if (!img.isEmpty() && !(img.startsWith("https://") || img.startsWith("http://") || img.startsWith("/")))
            throw new IllegalArgumentException("Đường dẫn ảnh không hợp lệ.");
        String vc = voucherCode == null ? "" : voucherCode.trim().toUpperCase();
        if (!vc.isEmpty() && !vc.matches("[A-Z0-9_-]{3,30}"))
            throw new IllegalArgumentException("Mã giảm giá đính kèm không hợp lệ.");
        p.setTitle(t);
        p.setSummary(summary == null || summary.isBlank() ? null : summary.trim());
        p.setContent(content == null || content.isBlank() ? null : content.trim());
        p.setImageUrl(img.isEmpty() ? null : img);
        p.setVoucherCode(vc.isEmpty() ? null : vc);
        p.setActive(active);
        repo.save(p);
    }

    @Transactional
    public void toggle(Long id) {
        Promotion p = repo.findById(id).orElseThrow();
        p.setActive(!Boolean.TRUE.equals(p.getActive()));
        repo.save(p);
    }

    @Transactional
    public void delete(Long id) { repo.deleteById(id); }
}
