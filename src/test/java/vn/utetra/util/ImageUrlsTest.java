package vn.utetra.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImageUrlsTest {

    @Test
    void insertsTransformationForCloudinaryImages() {
        String in = "https://res.cloudinary.com/demo/image/upload/v123/sample.jpg";
        assertEquals("https://res.cloudinary.com/demo/image/upload/f_auto,q_auto,w_480,c_limit/v123/sample.jpg",
                ImageUrls.optimize(in, 480));
    }

    @Test
    void keepsAlreadyOptimizedAndForeignUrls() {
        String done = "https://res.cloudinary.com/demo/image/upload/f_auto,q_auto/v1/a.jpg";
        assertEquals(done, ImageUrls.optimize(done, 480));
        assertEquals("https://example.com/a.jpg", ImageUrls.optimize("https://example.com/a.jpg", 480));
        assertNull(ImageUrls.optimize(null, 480));
    }
}
