package project.hamrosewa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.hamrosewa.model.Notification;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String message;
    private String type;
    private String url;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private boolean isRead;
    private Long recipientId;
    private String recipientType;
    
    public static NotificationDTO fromEntity(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .url(notification.getUrl())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .isRead(notification.isRead())
                .recipientId(notification.getRecipientId())
                .recipientType(notification.getRecipientType().name())
                .build();
    }
}
