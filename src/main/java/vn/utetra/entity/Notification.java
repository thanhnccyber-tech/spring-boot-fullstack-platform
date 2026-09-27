package vn.utetra.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Notification {

    private static final DateTimeFormatter FMT_DATETIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String title;
    private String content;
    private String type; // ORDER, SYSTEM

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /* ==================== Derived getter cho JSP ==================== */

    public String getCreatedAtFormatted() {
        return createdAt == null ? "" : createdAt.format(FMT_DATETIME);
    }
}