package project.hamrosewa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.hamrosewa.dto.ProviderServiceDTO;
import project.hamrosewa.exceptions.ProviderServiceException;
import project.hamrosewa.model.ProviderService;
import project.hamrosewa.model.ServiceProvider;
import project.hamrosewa.repository.ProviderServiceRepository;
import project.hamrosewa.repository.ServiceProviderRepository;

import java.io.IOException;

@Service
public class ProviderServiceService {

    @Autowired
    private ProviderServiceRepository providerServiceRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private PdfService pdfService;

    public ProviderService createService(ProviderServiceDTO serviceDTO) throws IOException {
        ServiceProvider provider = serviceProviderRepository.findById(serviceDTO.getServiceProviderId())
                .orElseThrow(() -> new ProviderServiceException("Service provider not found"));

        ProviderService service = new ProviderService();
        updateServiceFromDTO(service, serviceDTO, provider);

        if (serviceDTO.getPdf() != null && !serviceDTO.getPdf().isEmpty()) {
            String pdfPath = pdfService.savePdf(serviceDTO.getPdf());
            service.setPdfPath(pdfPath);
        }

        return providerServiceRepository.save(service);
    }

    public ProviderService updateService(Long serviceId, ProviderServiceDTO serviceDTO) throws IOException {
        ProviderService existingService = providerServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ProviderServiceException("Service not found"));

        ServiceProvider provider = serviceProviderRepository.findById(serviceDTO.getServiceProviderId())
                .orElseThrow(() -> new ProviderServiceException("Service provider not found"));

        if (serviceDTO.getPdf() != null && !serviceDTO.getPdf().isEmpty() && existingService.getPdfPath() != null) {
            pdfService.deletePdf(existingService.getPdfPath());
        }

        updateServiceFromDTO(existingService, serviceDTO, provider);

        if (serviceDTO.getPdf() != null && !serviceDTO.getPdf().isEmpty()) {
            String pdfPath = pdfService.savePdf(serviceDTO.getPdf());
            existingService.setPdfPath(pdfPath);
        }

        return providerServiceRepository.save(existingService);
    }

    private void updateServiceFromDTO(ProviderService service, ProviderServiceDTO dto, ServiceProvider provider) {
        if (dto.getServiceName() != null) service.setServiceName(dto.getServiceName());
        if (dto.getDescription() != null) service.setDescription(dto.getDescription());
        if (dto.getPrice() != null) service.setPrice(dto.getPrice());
        service.setServiceProvider(provider);
        if (dto.getCategory() != null) service.setCategory(dto.getCategory());
    }

    public void deleteService(Long serviceId) {
        ProviderService service = providerServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ProviderServiceException("Service not found"));

        if (service.getPdfPath() != null) {
            pdfService.deletePdf(service.getPdfPath());
        }

        providerServiceRepository.delete(service);
    }

    public ProviderService getServiceById(Long serviceId) {
        return providerServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ProviderServiceException("Service not found"));
    }
}
