package project.hamrosewa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.hamrosewa.model.Notification;
import project.hamrosewa.model.UserType;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByRecipientIdAndRecipientType(Long recipientId, UserType recipientType);
    
    List<Notification> findByRecipientIdAndRecipientTypeAndIsReadIsFalse(Long recipientId, UserType recipientType);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.recipientType = :recipientType ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("recipientId") Long recipientId, @Param("recipientType") UserType recipientType);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.recipientType = :recipientType AND n.isRead = false")
    Long countUnreadNotifications(@Param("recipientId") Long recipientId, @Param("recipientType") UserType recipientType);
}
