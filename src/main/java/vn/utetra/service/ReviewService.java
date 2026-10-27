package vn.utetra.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.utetra.dto.RatingSummary;
import vn.utetra.entity.Product;
import vn.utetra.entity.Review;
import vn.utetra.entity.User;
import vn.utetra.repository.OrderDetailRepository;
import vn.utetra.repository.ProductRepository;
import vn.utetra.repository.ReviewRepository;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired private ReviewRepository reviewRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private OrderDetailRepository orderDetailRepo;
    @Autowired private FileValidator fileValidator;
    @Autowired private CloudinaryService cloudinaryService;

    /** true = chỉ khách đã nhận hàng (đơn COMPLETED) mới được đánh giá. */
    @Value("${review.require-purchase:true}")
    private boolean requirePurchase;

    public boolean isRequirePurchase() { return requirePurchase; }

    public List<Review> listByProduct(Long productId) {
        return reviewRepo.findByProductIdWithUser(productId);
    }

    public Optional<Review> findMine(Long userId, Long productId) {
        return reviewRepo.findByUserAndProduct(userId, productId);
    }

    public boolean hasPurchased(Long userId, Long productId) {
        return orderDetailRepo.hasPurchased(userId, productId);
    }

    /** Khách này có được phép gửi đánh giá cho sản phẩm không. */
    public boolean canReview(Long userId, Long productId) {
        return !requirePurchase || hasPurchased(userId, productId);
    }

    public RatingSummary summary(Long productId) {
        return summaries(List.of(productId)).getOrDefault(productId, RatingSummary.EMPTY);
    }

    /** Điểm trung bình của nhiều sản phẩm trong 1 truy vấn (dùng cho danh sách sản phẩm). */
    public Map<Long, RatingSummary> summaries(Collection<Long> productIds) {
        Map<Long, RatingSummary> result = new HashMap<>();
        if (productIds == null || productIds.isEmpty()) return result;
        for (Object[] row : reviewRepo.summarize(productIds)) {
            Long pid = ((Number) row[0]).longValue();
            double avg = ((Number) row[1]).doubleValue();
            long cnt = ((Number) row[2]).longValue();
            result.put(pid, new RatingSummary(avg, cnt));
        }
        return result;
    }

    /** Thêm mới hoặc cập nhật đánh giá của khách cho sản phẩm. */
    @Transactional
    public Review submit(User user, Long productId, Integer rating, String comment, MultipartFile media) {
        if (rating == null || rating < 1 || rating > 5)
            throw new IllegalArgumentException("Vui lòng chọn số sao từ 1 đến 5.");

        // Nội dung cảm nhận không bắt buộc và không giới hạn số ký tự; chỉ cần chọn số sao.
        String text = comment == null ? "" : comment.trim();

        fileValidator.validateReviewMedia(media);
        Product product = productRepo.findById(productId)
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã ngừng bán."));

        if (!canReview(user.getId(), productId))
            throw new IllegalArgumentException("Bạn cần mua và nhận sản phẩm này trước khi đánh giá.");

        Review r = reviewRepo.findByUserAndProduct(user.getId(), productId).orElseGet(() -> {
            Review nr = new Review();
            nr.setUser(user);
            nr.setProduct(product);
            return nr;
        });
        r.setRating(rating);
        r.setComment(text);
        if (media != null && !media.isEmpty()) {
            r.setMediaUrl(cloudinaryService.uploadReviewMedia(media, "reviews"));
            r.setMediaType(media.getContentType());
        }
        r.setUpdatedAt(LocalDateTime.now());
        return reviewRepo.save(r);
    }

    /**
     * Xoá đánh giá. Chủ đánh giá hoặc ADMIN mới được xoá.
     * @return id sản phẩm để controller redirect về đúng trang
     */
    @Transactional
    public Long delete(Long reviewId, String requesterEmail, boolean isAdmin) {
        Review r = reviewRepo.findByIdWithProduct(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Đánh giá không tồn tại."));
        boolean owner = r.getUser() != null && requesterEmail != null
                && requesterEmail.equalsIgnoreCase(r.getUser().getEmail());
        if (!owner && !isAdmin)
            throw new IllegalArgumentException("Bạn không có quyền xoá đánh giá này.");
        Long productId = r.getProduct().getId();
        reviewRepo.delete(r);
        return productId;
    }
}
