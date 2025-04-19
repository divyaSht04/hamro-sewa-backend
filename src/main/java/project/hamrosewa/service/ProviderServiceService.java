package project.hamrosewa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.hamrosewa.dto.ProviderServiceDTO;
import project.hamrosewa.exceptions.ProviderServiceException;
import project.hamrosewa.model.*;
import project.hamrosewa.model.Notification.NotificationType;
import project.hamrosewa.repository.AdminRepository;
import project.hamrosewa.repository.ProviderServiceRepository;
import project.hamrosewa.repository.ServiceProviderRepository;
import project.hamrosewa.repository.ReviewRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class ProviderServiceService {

    @Autowired
    private ProviderServiceRepository providerServiceRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private PdfService pdfService;
    
    @Autowired
    private ImageService imageService;

    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AdminRepository adminRepository;

    public ProviderService createService(ProviderServiceDTO serviceDTO) throws IOException {
        ServiceProvider provider = serviceProviderRepository.findById(serviceDTO.getServiceProviderId())
                .orElseThrow(() -> new ProviderServiceException("Service provider not found"));

        ProviderService service = new ProviderService();
        updateServiceFromDTO(service, serviceDTO, provider);

        // Handle PDF upload
        if (serviceDTO.getPdf() != null && !serviceDTO.getPdf().isEmpty()) {
            String pdfPath = pdfService.savePdf(serviceDTO.getPdf());
            service.setPdfPath(pdfPath);
        }

        if (serviceDTO.getImage() != null && !serviceDTO.getImage().isEmpty()) {
            String imagePath = imageService.saveImage(serviceDTO.getImage());
            service.setImagePath(imagePath);
        }
        
        ProviderService savedService = providerServiceRepository.save(service);
        if (savedService.getStatus() == ServiceStatus.PENDING) {
            List<Admin> admins = adminRepository.findAll();
            for (Admin admin : admins) {
                try {
                    Long adminId = (long) admin.getId();
                        notificationService.createNotification(
                            "New service pending approval: '" + savedService.getServiceName() + "' by " +
                            provider.getBusinessName() + " (" + provider.getUsername() + ")",
                            NotificationType.SERVICE_PENDING,
                            "/admin/services/pending",
                            adminId,
                            UserType.ADMIN
                        );
                } catch (Exception e) {
                    System.err.println("Could not create notification for admin ID: " + admin.getId() + " - " + e.getMessage());
                }
                break;
            }
        }
        
        return savedService;
    }

    public ProviderService updateService(Long serviceId, ProviderServiceDTO serviceDTO) throws IOException {
        ProviderService existingService = providerServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ProviderServiceException("Service not found"));

        ServiceProvider provider = serviceProviderRepository.findById(serviceDTO.getServiceProviderId())
                .orElseThrow(() -> new ProviderServiceException("Service provider not found"));

        // Handle PDF update
        if (serviceDTO.getPdf() != null && !serviceDTO.getPdf().isEmpty() && existingService.getPdfPath() != null) {
            pdfService.deletePdf(existingService.getPdfPath());
        }
        
        // Handle image update
        if (serviceDTO.getImage() != null && !serviceDTO.getImage().isEmpty() && existingService.getImagePath() != null) {
            imageService.deleteImage(existingService.getImagePath());
        }

        updateServiceFromDTO(existingService, serviceDTO, provider);

        // Save new PDF if provided
        if (serviceDTO.getPdf() != null && !serviceDTO.getPdf().isEmpty()) {
            String pdfPath = pdfService.savePdf(serviceDTO.getPdf());
            existingService.setPdfPath(pdfPath);
        }
        
        // Save new image if provided
        if (serviceDTO.getImage() != null && !serviceDTO.getImage().isEmpty()) {
            String imagePath = imageService.saveImage(serviceDTO.getImage());
            existingService.setImagePath(imagePath);
        }

        return providerServiceRepository.save(existingService);
    }

    private void updateServiceFromDTO(ProviderService service, ProviderServiceDTO dto, ServiceProvider provider) {
        if (dto.getServiceName() != null) service.setServiceName(dto.getServiceName());
        if (dto.getDescription() != null) service.setDescription(dto.getDescription());
        if (dto.getPrice() != null) service.setPrice(dto.getPrice());
        service.setServiceProvider(provider);
        if (dto.getCategory() != null) service.setCategory(dto.getCategory());

        if (service.getId() == null) {
            service.setStatus(ServiceStatus.PENDING);
        }
    }

    @Transactional
    public void deleteService(Long serviceId) {
        ProviderService service = providerServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ProviderServiceException("Service not found"));

        if (service.getReviews() != null && !service.getReviews().isEmpty()) {
            List<Review> reviewsToDelete = new ArrayList<>(service.getReviews());

            for (Review review : reviewsToDelete) {
                try {
                    ServiceBooking booking = review.getBooking();
                    if (booking != null) {
                        booking.setReview(null);
                    }
                    review.setBooking(null);
                    review.setProviderService(null);
                    review.setCustomer(null);

                    reviewRepository.delete(review);
                } catch (Exception e) {
                    throw new ProviderServiceException("Error deleting reviews for service: " + e.getMessage());
                }
            }
            service.getReviews().clear();
        }

        if (service.getPdfPath() != null) {
            pdfService.deletePdf(service.getPdfPath());
        }

        if (service.getImagePath() != null) {
            imageService.deleteImage(service.getImagePath());
        }
        
        // Store service provider info before deleting the service
        ServiceProvider serviceProvider = service.getServiceProvider();
        String serviceName = service.getServiceName();
        
        // Delete the service
        providerServiceRepository.delete(service);
        
        // Notify service provider about the service deletion
        notificationService.createNotification(
            "Your service '" + serviceName + "' has been deleted by an administrator. "
            + "Please contact support if you have any questions.",
            NotificationType.SERVICE_REJECTED,
            "/provider/services",
            Long.valueOf(serviceProvider.getId()),
            UserType.SERVICE_PROVIDER
        );
    }

    public ProviderService getServiceById(Long serviceId) {
        return providerServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ProviderServiceException("Service not found"));
    }
    public byte[] getServiceImage(String imagePath) throws IOException {
        if (imagePath == null || imagePath.isEmpty()) {
            throw new ProviderServiceException("Image path is null or empty");
        }
        return imageService.getImage(imagePath);
    }

    public byte[] getServicePdf(String pdfPath) throws IOException {
        if (pdfPath == null || pdfPath.isEmpty()) {
            throw new ProviderServiceException("PDF path is null or empty");
        }
        
        Path path = pdfService.getPdfPath(pdfPath);
        return Files.readAllBytes(path);
    }

    public ProviderService approveService(Long serviceId, String feedback) {
        ProviderService service = providerServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ProviderServiceException("Service not found"));
        
        service.setStatus(ServiceStatus.APPROVED);
        if (feedback != null && !feedback.trim().isEmpty()) {
            service.setAdminFeedback(feedback);
        }
        
        ProviderService savedService = providerServiceRepository.save(service);
        
        // Send notification to the service provider about the service approval
        notificationService.createNotification(
            "Your service '" + service.getServiceName() + "' has been approved" + 
            (feedback != null && !feedback.trim().isEmpty() ? " with feedback: " + feedback : "") +
            ". Your service is now live and available for bookings. Remember that customers can earn a loyalty discount after 4 completed bookings of your services.",
            NotificationType.SERVICE_APPROVED,
            "/provider/services",
            Long.valueOf(service.getServiceProvider().getId()),
            UserType.SERVICE_PROVIDER
        );
        
        return savedService;
    }

    public ProviderService rejectService(Long serviceId, String feedback) {
        ProviderService service = providerServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ProviderServiceException("Service not found"));
        
        service.setStatus(ServiceStatus.REJECTED);
        service.setAdminFeedback(feedback);
        ProviderService savedService = providerServiceRepository.save(service);
        
        // Log the rejection
        System.out.println("Service rejected: " + savedService.getServiceName() + ", ID: " + savedService.getId());
        
        // Send notification to the service provider about the service rejection
        notificationService.createNotification(
            "Your service '" + service.getServiceName() + "' has been rejected" + 
            (feedback != null && !feedback.trim().isEmpty() ? " with reason: " + feedback : "") +
            ". Please review the feedback and resubmit your service with the necessary changes.",
            NotificationType.SERVICE_REJECTED,
            "/provider/services",
            Long.valueOf(service.getServiceProvider().getId()),
            UserType.SERVICE_PROVIDER
        );
        
        return savedService;
    }

    public List<ProviderService> getServicesByStatus(ServiceStatus status) {
        return providerServiceRepository.findByStatus(status);
    }

    public List<ProviderService> getAllServices() {
        return providerServiceRepository.findAll();
    }
}
