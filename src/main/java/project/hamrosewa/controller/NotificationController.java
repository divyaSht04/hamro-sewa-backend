package project.hamrosewa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.hamrosewa.dto.NotificationDTO;
import project.hamrosewa.model.User;
import project.hamrosewa.model.UserType;
import project.hamrosewa.repository.UserRepository;
import project.hamrosewa.service.NotificationService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(
            Authentication authentication,
            @RequestParam UserType userType) {
        
        // Get email from authentication and find user
        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Long userId = Long.valueOf(userOpt.get().getId());
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, userType));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            Authentication authentication,
            @RequestParam UserType userType) {
        
        // Get email from authentication and find user
        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Long userId = Long.valueOf(userOpt.get().getId());
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId, userType));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            Authentication authentication,
            @RequestParam UserType userType) {

        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Long userId = Long.valueOf(userOpt.get().getId());
        return ResponseEntity.ok(notificationService.getUnreadCount(userId, userType));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            Authentication authentication,
            @RequestParam UserType userType) {

        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Long userId = Long.valueOf(userOpt.get().getId());
        notificationService.markAllAsRead(userId, userType);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications(
            Authentication authentication,
            @RequestParam UserType userType) {
        
        // Get email from authentication and find user
        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Long userId = Long.valueOf(userOpt.get().getId());
        notificationService.deleteAllNotifications(userId, userType);
        return ResponseEntity.ok().build();
    }
}
