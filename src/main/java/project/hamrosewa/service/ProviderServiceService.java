package project.hamrosewa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.hamrosewa.dto.ProviderServiceDTO;
import project.hamrosewa.exceptions.ProviderServiceException;
import project.hamrosewa.model.*;
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

        return providerServiceRepository.save(service);
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

        providerServiceRepository.delete(service);
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
        
        return providerServiceRepository.save(service);
    }

    public ProviderService rejectService(Long serviceId, String feedback) {
        ProviderService service = providerServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ProviderServiceException("Service not found"));
        
        service.setStatus(ServiceStatus.REJECTED);
        service.setAdminFeedback(feedback);
        
        return providerServiceRepository.save(service);
    }

    public List<ProviderService> getServicesByStatus(ServiceStatus status) {
        return providerServiceRepository.findByStatus(status);
    }

    public List<ProviderService> getAllServices() {
        return providerServiceRepository.findAll();
    }
}
