package project.hamrosewa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_seq")
    @SequenceGenerator(name = "notification_seq", sequenceName = "notification_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Column(name = "notification_type")
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String url;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;
    
    @Column(name = "recipient_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType recipientType;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        isRead = false;
    }
    
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
    
    public enum NotificationType {
        BOOKING_CREATED,
        BOOKING_CONFIRMED,
        BOOKING_COMPLETED,
        BOOKING_CANCELLED,
        SERVICE_APPROVED,
        SERVICE_REJECTED,
        NEW_REVIEW,
        REVIEW_UPDATED,
        REVIEW_DELETED,
        NEW_MESSAGE,
        ACCOUNT_VERIFIED,
        ACCOUNT_CREATED,
        SERVICE_PENDING,
        LOYALTY_DISCOUNT
    }
}
