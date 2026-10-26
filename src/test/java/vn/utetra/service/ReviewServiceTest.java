package vn.utetra.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import vn.utetra.entity.Product;
import vn.utetra.entity.Review;
import vn.utetra.entity.User;
import vn.utetra.repository.OrderDetailRepository;
import vn.utetra.repository.ProductRepository;
import vn.utetra.repository.ReviewRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepo;
    @Mock private ProductRepository productRepo;
    @Mock private OrderDetailRepository orderDetailRepo;
    @Mock private FileValidator fileValidator;
    @Mock private CloudinaryService cloudinaryService;
    @InjectMocks private ReviewService service;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "requirePurchase", true);
        user = new User();
        user.setId(3L);
        product = new Product();
        product.setId(8L);
        product.setActive(true);
    }

    @Test
    void rejectsCommentShorterThanFiftyCharacters() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.submit(user, 8L, 5, "Chua du dai", null));

        assertTrue(error.getMessage().contains("50"));
        verifyNoInteractions(productRepo, reviewRepo, orderDetailRepo, fileValidator, cloudinaryService);
    }

    @Test
    void rejectsCommentLongerThanOneThousandCharacters() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.submit(user, 8L, 5, "a".repeat(1001), null));

        assertTrue(error.getMessage().contains("1000"));
        verifyNoInteractions(productRepo, reviewRepo, orderDetailRepo, fileValidator, cloudinaryService);
    }

    @Test
    void requiresCompletedPurchaseBeforeReview() {
        when(productRepo.findById(8L)).thenReturn(Optional.of(product));
        when(orderDetailRepo.hasPurchased(3L, 8L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(user, 8L, 4,
                        "Binh luan hop le tren nam muoi ky tu de danh gia san pham nay.", null));

        verifyNoInteractions(reviewRepo, cloudinaryService);
    }

    @Test
    void savesReviewAfterCompletedPurchase() {
        String comment = "a".repeat(50);
        when(productRepo.findById(8L)).thenReturn(Optional.of(product));
        when(orderDetailRepo.hasPurchased(3L, 8L)).thenReturn(true);
        when(reviewRepo.findByUserAndProduct(3L, 8L)).thenReturn(Optional.empty());
        when(reviewRepo.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Review saved = service.submit(user, 8L, 5, comment, null);

        assertEquals(5, saved.getRating());
        assertEquals(comment, saved.getComment());
        assertSame(user, saved.getUser());
        assertSame(product, saved.getProduct());
        verify(fileValidator).validateReviewMedia(null);
    }

    @Test
    void uploadsAndStoresReviewMedia() {
        MockMultipartFile media = new MockMultipartFile(
                "media", "review.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(productRepo.findById(8L)).thenReturn(Optional.of(product));
        when(orderDetailRepo.hasPurchased(3L, 8L)).thenReturn(true);
        when(reviewRepo.findByUserAndProduct(3L, 8L)).thenReturn(Optional.empty());
        when(cloudinaryService.uploadReviewMedia(media, "reviews"))
                .thenReturn("https://cdn.example.test/review.jpg");
        when(reviewRepo.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Review saved = service.submit(user, 8L, 5,
                "Binh luan hop le tren nam muoi ky tu de danh gia san pham nay.", media);

        assertEquals("https://cdn.example.test/review.jpg", saved.getMediaUrl());
        assertEquals("image/jpeg", saved.getMediaType());
        verify(cloudinaryService).uploadReviewMedia(media, "reviews");
    }

    @Test
    void rejectsVideoWhoseContentDoesNotMatchItsMimeType() {
        FileValidator validator = new FileValidator();
        MockMultipartFile media = new MockMultipartFile(
                "media", "review.mp4", "video/mp4", "not a video".getBytes());

        assertThrows(IllegalArgumentException.class, () -> validator.validateReviewMedia(media));
    }

    @Test
    void acceptsMp4WithValidFileSignature() {
        FileValidator validator = new FileValidator();
        byte[] mp4Header = {0, 0, 0, 24, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm'};
        MockMultipartFile media = new MockMultipartFile(
                "media", "review.mp4", "video/mp4", mp4Header);

        assertDoesNotThrow(() -> validator.validateReviewMedia(media));
    }
}
