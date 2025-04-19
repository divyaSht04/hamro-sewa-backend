package project.hamrosewa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.hamrosewa.dto.NotificationDTO;
import project.hamrosewa.model.Notification;
import project.hamrosewa.model.Notification.NotificationType;
import project.hamrosewa.model.UserType;
import project.hamrosewa.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;


    @Transactional
    public void createNotification(String message,
                                   NotificationType type,
                                   String url,
                                   Long recipientId,
                                   UserType recipientType) {
        // Create and save the notification
            Notification notification = Notification.builder()
                    .message(message)
                    .type(type)
                    .url(url)
                    .recipientId(recipientId)
                    .recipientType(recipientType)
                    .createdAt(LocalDateTime.now())
                    .isRead(false)
                    .build();
            
            notification = notificationRepository.save(notification);
            
            // Send to WebSocket
            NotificationDTO notificationDTO = NotificationDTO.fromEntity(notification);
            String destination = String.format("/user/%s-%d/notifications", recipientType.toString().toLowerCase(), recipientId);
            messagingTemplate.convertAndSend(destination, notificationDTO);
    }
    

    public List<NotificationDTO> getUserNotifications(Long userId, UserType userType) {
        return notificationRepository.findByRecipientIdAndRecipientType(userId, userType)
                .stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications(Long userId, UserType userType) {
        return notificationRepository.findByRecipientIdAndRecipientTypeAndIsReadIsFalse(userId, userType)
                .stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.markAsRead();
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId, UserType userType) {
        List<Notification> unreadNotifications = 
                notificationRepository.findByRecipientIdAndRecipientTypeAndIsReadIsFalse(userId, userType);
                
        unreadNotifications.forEach(notification -> {
            notification.markAsRead();
            notificationRepository.save(notification);
        });
    }

    public Long getUnreadCount(Long userId, UserType userType) {
        return notificationRepository.countUnreadNotifications(userId, userType);
    }
    
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    @Transactional
    public void deleteAllNotifications(Long userId, UserType userType) {
        List<Notification> notifications = notificationRepository.findByRecipientIdAndRecipientType(userId, userType);
        notificationRepository.deleteAll(notifications);
    }

    public void sendSystemNotificationToUserType(String message, NotificationType type, 
                                                String url, UserType recipientType) {
        NotificationDTO notification = NotificationDTO.builder()
                .message(message)
                .type(type.name())
                .url(url)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .recipientType(recipientType.name())
                .build();
                
        messagingTemplate.convertAndSend("/topic/" + recipientType.toString().toLowerCase(), notification);
    }
    
}
