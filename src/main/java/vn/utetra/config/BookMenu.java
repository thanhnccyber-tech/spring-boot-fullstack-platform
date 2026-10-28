package vn.utetra.config;

import java.util.List;

/**
 * 7 món được giới thiệu trong "Sổ thực đơn" ở trang chủ (ảnh nằm ở static/images/tea-book).
 * Mô tả chỉ nói những gì nhìn thấy trong ảnh món, không hứa hẹn thành phần chưa được xác nhận.
 * Giá chỉ là giá khởi tạo: sau khi món đã có trong cơ sở dữ liệu, admin sửa giá/mô tả ở trang quản trị
 * và sổ thực đơn sẽ hiển thị đúng giá trị mới.
 */
public final class BookMenu {

    public record Item(String name, String description, String price, String image, boolean fruitTea, boolean milkTea) {
        public String imageUrl() { return "/static/images/tea-book/" + image; }
    }

    public static final List<Item> ITEMS = List.of(
        new Item("Trà bí đao đường đen",
            "Trà lạnh màu hổ phách, vị ngọt thanh của bí đao quyện với hương mật của đường đen.",
            "32000", "bi-dao-duong-den.jpg", true, false),
        new Item("Trà chanh xoài",
            "Xoài chín cắt miếng và chanh vàng trong ly trà lạnh, tươi mát, thơm dịu mùi trái cây.",
            "42000", "chanh-xoai.jpg", true, false),
        new Item("Trà sữa kem cháy",
            "Lớp kem sữa khò cháy nhẹ, rắc đường nâu, đặt trên nền trà sữa màu hổ phách. Thơm ngậy, béo vừa.",
            "45000", "tra-sua-kem-chay.jpg", false, true),
        new Item("Hồng trà kem sữa",
            "Hồng trà đậm màu hổ phách, phủ lớp kem sữa mịn và điểm hai lá trà. Uống lạnh để nghe rõ hương trà.",
            "38000", "hong-tra-kem-sua.jpg", false, true),
        new Item("Trà ổi hồng",
            "Ổi hồng cắt miếng cùng lát chanh xanh trong ly trà lạnh có đá viên, sắc hồng nhạt, vị thanh mát.",
            "40000", "tra-oi-hong.jpg", true, false),
        new Item("Trà sữa khoai môn",
            "Trà sữa màu tím nhạt, phủ những khối khoai môn bên trên, ăn kèm trân châu tím. Béo dịu, thơm mùi khoai.",
            "42000", "tra-sua-khoai-mon.jpg", false, true),
        new Item("Trà sữa trân châu đường đen",
            "Sữa trà trắng ngà viền những vệt đường đen sánh, trân châu đen dai mềm xếp đầy phía trên.",
            "38000", "tra-sua-tran-chau-duong-den.jpg", false, true)
    );

    private BookMenu() { }
}
