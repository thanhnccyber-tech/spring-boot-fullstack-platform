package vn.utetra.dto;

/** Điểm đánh giá trung bình + số lượt đánh giá của một sản phẩm (dùng cho JSP). */
public class RatingSummary {

    public static final RatingSummary EMPTY = new RatingSummary(0, 0);

    private final double average;
    private final long count;

    public RatingSummary(double average, long count) {
        this.average = average;
        this.count = count;
    }

    public double getAverage() { return average; }
    public long getCount()     { return count; }

    /** Số sao làm tròn (0..5) để tô sao trong JSP */
    public int getRounded() { return (int) Math.round(average); }
}
