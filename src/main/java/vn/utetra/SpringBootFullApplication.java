package vn.utetra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class SpringBootFullApplication {
    public static void main(String[] args) {
        // Mọi LocalDateTime.now() (giờ đặt đơn, thống kê "hôm nay") tính theo giờ Việt Nam dù server đặt ở đâu
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SpringApplication.run(SpringBootFullApplication.class, args);
    }
}