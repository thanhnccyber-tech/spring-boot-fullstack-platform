package vn.utetra.util;

/** Rút gọn ảnh Cloudinary: tự chọn định dạng (webp/avif) + nén + giới hạn chiều rộng => trang tải nhanh hơn. */
public final class ImageUrls {

    private static final String MARK = "/image/upload/";

    private ImageUrls() { }

    public static String optimize(String url, int width) {
        if (url == null || url.isBlank()) return url;
        if (!url.contains("res.cloudinary.com")) return url;
        int j = url.indexOf(MARK);
        if (j < 0) return url;
        String rest = url.substring(j + MARK.length());
        if (rest.startsWith("f_auto") || rest.startsWith("q_auto")) return url; // đã tối ưu
        String t = "f_auto,q_auto" + (width > 0 ? ",w_" + width + ",c_limit" : "");
        return url.substring(0, j + MARK.length()) + t + "/" + rest;
    }
}
