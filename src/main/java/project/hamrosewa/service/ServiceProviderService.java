package project.hamrosewa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import project.hamrosewa.dto.ServiceProviderDashboardMetricsDTO;
import project.hamrosewa.dto.PerformanceMetricsDTO;
import project.hamrosewa.dto.ServiceProviderDTO;
import project.hamrosewa.exceptions.UserValidationException;
import project.hamrosewa.model.*;
import project.hamrosewa.model.Notification.NotificationType;
import project.hamrosewa.model.UserType;
import project.hamrosewa.repository.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class ServiceProviderService {
    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ImageService imageStorageService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private ProviderServiceRepository providerServiceRepository;
    
    @Autowired
    private ServiceBookingRepository serviceBookingRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;


    public void registerServiceProvider(ServiceProviderDTO serviceProviderDTO) throws IOException {
        boolean usernameExists = userRepository.findByUsername(serviceProviderDTO.getUsername()).isPresent();
        if (usernameExists) {
            throw new UserValidationException("Username already exists");
        }

        boolean emailExist = userRepository.findByEmail(serviceProviderDTO.getEmail()).isPresent();
        if (emailExist) {
            throw new UserValidationException("Email already exists");
        }

        boolean numberExists = userRepository.findByPhoneNumber(serviceProviderDTO.getPhoneNumber()).isPresent();
        if (numberExists) {
            throw new UserValidationException("Number already exists");
        }

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setUsername(serviceProviderDTO.getUsername());
        serviceProvider.setEmail(serviceProviderDTO.getEmail());
        serviceProvider.setPhoneNumber(serviceProviderDTO.getPhoneNumber());
        serviceProvider.setPassword(passwordEncoder.encode(serviceProviderDTO.getPassword()));
        serviceProvider.setBusinessName(serviceProviderDTO.getBusinessName());
        serviceProvider.setAddress(serviceProviderDTO.getAddress());

        if (serviceProviderDTO.getImage() != null && !serviceProviderDTO.getImage().isEmpty()) {
            String fileName = imageStorageService.saveImage(serviceProviderDTO.getImage());
            serviceProvider.setImage(fileName);
        } else {
            serviceProvider.setImage(null);
        }

        Role userRole = roleRepository.findByName("ROLE_SERVICE_PROVIDER");
        serviceProvider.setRole(userRole);
        serviceProvider = userRepository.save(serviceProvider);
        
        // Send notification to all admin users about the new service provider registration
        List<Admin> admins = adminRepository.findAll();
        for (Admin admin : admins) {
            notificationService.createNotification(
                "New service provider registered: " + serviceProvider.getBusinessName() + " (" + serviceProvider.getUsername() + ")",
                NotificationType.ACCOUNT_CREATED,
                "/admin/service-providers",
                Long.valueOf(admin.getId()),
                UserType.ADMIN
            );
        }
    }


    @Transactional
        public void updateServiceProvider(Long id, ServiceProviderDTO serviceProviderDTO) throws IOException {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service provider not found"));

        if (serviceProviderDTO.getUsername() != null && !serviceProviderDTO.getUsername().equals(serviceProvider.getUsername())) {
            boolean usernameExists = userRepository.findByUsername(serviceProviderDTO.getUsername()).isPresent();
            if (usernameExists) {
                throw new UserValidationException("Username already exists");
            }
            serviceProvider.setUsername(serviceProviderDTO.getUsername());
        }

        if (serviceProviderDTO.getEmail() != null && !serviceProviderDTO.getEmail().equals(serviceProvider.getEmail())) {
            boolean emailExists = userRepository.findByEmail(serviceProviderDTO.getEmail()).isPresent();
            if (emailExists) {
                throw new UserValidationException("Email already exists");
            }
            serviceProvider.setEmail(serviceProviderDTO.getEmail());
        }

        if (serviceProviderDTO.getPhoneNumber() != null && !serviceProviderDTO.getPhoneNumber().equals(serviceProvider.getPhoneNumber())) {
            boolean numberExists = userRepository.findByPhoneNumber(serviceProviderDTO.getPhoneNumber()).isPresent();
            if (numberExists) {
                throw new UserValidationException("Phone number already exists");
            }
            serviceProvider.setPhoneNumber(serviceProviderDTO.getPhoneNumber());
        }

        if (serviceProviderDTO.getPassword() != null && !serviceProviderDTO.getPassword().isEmpty()) {
            serviceProvider.setPassword(passwordEncoder.encode(serviceProviderDTO.getPassword()));
        }

        if (serviceProviderDTO.getBusinessName() != null) {
            serviceProvider.setBusinessName(serviceProviderDTO.getBusinessName());
        }

        if (serviceProviderDTO.getAddress() != null) {
            serviceProvider.setAddress(serviceProviderDTO.getAddress());
        }

        if (serviceProviderDTO.getImage() != null && !serviceProviderDTO.getImage().isEmpty()) {
            if (serviceProvider.getImage() != null) {
                imageStorageService.deleteImage(serviceProvider.getImage());
            }
            String fileName = imageStorageService.saveImage(serviceProviderDTO.getImage());
            serviceProvider.setImage(fileName);
        } else if (serviceProvider.getImage() != null) {
            imageStorageService.deleteImage(serviceProvider.getImage());
            serviceProvider.setImage(null);
        }

        userRepository.save(serviceProvider);
    }

    public byte[] getCustomerProfileImage(int userId) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getImage() == null) {
            throw new RuntimeException("User has no profile image");
        }

        return imageStorageService.getImage(user.getImage());
    }

    public ServiceProvider getServiceProviderById(Long id) {
        return serviceProviderRepository.findById(id).orElse(null);
    }

    public List<ProviderService> getProviderServices(Long providerId) {
        ServiceProvider serviceProvider = getServiceProviderById(providerId);
        if (serviceProvider == null) {
            throw new RuntimeException("Service provider not found");
        }
        return serviceProvider.getServices();
    }

    public void updateServiceProviderPhoto(long customerId, MultipartFile photo) throws IOException {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        if (serviceProvider.getImage() != null) {
            imageStorageService.deleteImage(serviceProvider.getImage());
        }
        String fileName = imageStorageService.saveImage(photo);
        serviceProvider.setImage(fileName);
        serviceProviderRepository.save(serviceProvider);
    }

    public List<ServiceProvider> getServiceProviderInfo(long id) {
        Optional<ServiceProvider> serviceProvider = serviceProviderRepository.findById(id);
        if (serviceProvider.isPresent()) {
            return serviceProvider.stream().toList();
        }
        return null;
    }


     
    public ServiceProviderDashboardMetricsDTO getDashboardMetrics(long providerId, LocalDate startDate, LocalDate endDate) {
        // Convert provider ID to int for compatibility with existing methods
        int providerIdInt = (int) providerId;
        
        // Get previous period dates for comparison (same duration as requested period)
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate prevPeriodEnd = startDate.minusDays(1);
        LocalDate prevPeriodStart = prevPeriodEnd.minusDays(daysBetween - 1);
        
        // Calculate current period metrics
        int totalServices = countProviderServices(providerIdInt);
        double totalRevenue = calculateTotalRevenue(providerIdInt);
        double averageRating = calculateAverageRating(providerIdInt);
        
        // Calculate previous period metrics for percent change
        int prevTotalServices = countProviderServices(providerIdInt, prevPeriodStart, prevPeriodEnd);
        double prevTotalRevenue = calculateTotalRevenue(providerIdInt, prevPeriodStart, prevPeriodEnd);
        double prevAverageRating = calculateAverageRating(providerIdInt, prevPeriodStart, prevPeriodEnd);
        
        // Calculate percent changes
        int percentChangeServices = calculatePercentChange(prevTotalServices, totalServices);
        int percentChangeRevenue = calculatePercentChange(prevTotalRevenue, totalRevenue);
        int percentChangeRating = calculatePercentChange(prevAverageRating, averageRating);
        
        return new ServiceProviderDashboardMetricsDTO(
            totalServices,
            totalRevenue,
            averageRating,
            percentChangeServices,
            percentChangeRevenue,
            percentChangeRating
        );
    }
    
    public PerformanceMetricsDTO getPerformanceMetrics(long providerId, LocalDate startDate, LocalDate endDate) {
        // Convert provider ID to int for compatibility with existing methods
        int providerIdInt = (int) providerId;
        
        // Convert dates to LocalDateTime for repository queries
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // Get all bookings for this provider in the date range
        List<ServiceBooking> bookings = serviceBookingRepository.findByServiceProviderId(providerIdInt);
        bookings = bookings.stream()
            .filter(b -> !b.getBookingDateTime().isBefore(startDateTime) && !b.getBookingDateTime().isAfter(endDateTime))
            .collect(Collectors.toList());
        
        // Calculate metrics
        int totalClients = countUniqueClients(bookings);
        int completedJobs = (int) bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
            .count();
        double totalEarnings = calculateTotalEarnings(bookings);
        int responseRate = calculateResponseRate(bookings);
        
        return new PerformanceMetricsDTO(
            totalClients,
            completedJobs,
            totalEarnings,
            responseRate
        );
    }

    public Map<String, Double> getMonthlyEarnings(long providerId, int year) {
        int providerIdInt = (int) providerId;

        Map<String, Double> monthlyEarnings = new LinkedHashMap<>();
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        // Initialize all months with 0.0
        for (String month : monthNames) {
            monthlyEarnings.put(month, 0.0);
        }

        List<ServiceBooking> bookings = serviceBookingRepository.findByServiceProviderId(providerIdInt);

        bookings.stream()
                .filter(b -> b != null && b.getStatus() == BookingStatus.COMPLETED)
                .filter(b -> b.getBookingDateTime() != null && b.getBookingDateTime().getYear() == year)
                .forEach(booking -> {
                    int monthIndex = booking.getBookingDateTime().getMonthValue() - 1; // 0-based index
                    if (monthIndex >= 0 && monthIndex < monthNames.length) {
                        String monthName = monthNames[monthIndex];

                        // Get the current earnings for this month
                        double currentEarnings = monthlyEarnings.get(monthName);

                        // Add the booking amount (use discounted price if applicable)
                        BigDecimal bookingAmount = booking.getDiscountApplied() ?
                                booking.getDiscountedPrice() : booking.getOriginalPrice();

                        // Convert BigDecimal to double safely, default to 0 if null
                        double amount = bookingAmount != null ? bookingAmount.doubleValue() : 0.0;

                        monthlyEarnings.put(monthName, currentEarnings + amount);
                    }
                });

        return monthlyEarnings;
    }

    public Map<String, Integer> getClientGrowthByMonth(long providerId, int year) {
        // Convert provider ID to int for compatibility with existing methods
        int providerIdInt = (int) providerId;
        
        Map<String, Integer> clientGrowth = new LinkedHashMap<>();
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        
        // Initialize all months with 0
        for (String month : monthNames) {
            clientGrowth.put(month, 0);
        }
        
        // Get all bookings for this provider
        List<ServiceBooking> bookings = serviceBookingRepository.findByServiceProviderId(providerIdInt);
        
        // Get all unique client IDs for each month in the specified year
        Map<Month, Set<Integer>> clientsByMonth = new HashMap<>();
        for (Month month : Month.values()) {
            clientsByMonth.put(month, new HashSet<>());
        }
        
        // Populate the sets with client IDs for each month
        bookings.stream()
            .filter(b -> b.getBookingDateTime().getYear() == year)
            .forEach(booking -> {
                Month month = booking.getBookingDateTime().getMonth();
                int clientId = booking.getCustomer().getId();
                clientsByMonth.get(month).add(clientId);
            });
            
        // Convert to the final map with month names and client counts
        for (int i = 0; i < 12; i++) {
            Month month = Month.of(i + 1);
            String monthName = monthNames[i];
            clientGrowth.put(monthName, clientsByMonth.get(month).size());
        }
        
        return clientGrowth;
    }
    
    public Map<String, Integer> getServicePopularity(long providerId) {
        // Convert provider ID to int for compatibility with existing methods
        int providerIdInt = (int) providerId;
        
        // Get all services for this provider
        List<ProviderService> services = providerServiceRepository.findByServiceProviderId(providerIdInt);
        
        // Get all bookings for this provider's services
        Map<String, Integer> servicePopularity = new HashMap<>();
        
        for (ProviderService service : services) {
            List<ServiceBooking> bookings = serviceBookingRepository.findByProviderServiceId(service.getId());
            int bookingCount = bookings.size();
            servicePopularity.put(service.getServiceName(), bookingCount);
        }
        
        // Sort by popularity (booking count) in descending order
        return servicePopularity.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5) // Top 5 most popular services
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    // Helper methods for calculations
    
    private int countProviderServices(int providerId) {
        List<ProviderService> services = providerServiceRepository.findByServiceProviderId(providerId);
        return services.size();
    }
    
    private int countProviderServices(int providerId, LocalDate startDate, LocalDate endDate) {
        List<ProviderService> services = providerServiceRepository.findByServiceProviderId(providerId);
        
        // Filter services created in the date range
        return (int) services.stream()
            .filter(s -> {
                LocalDate createdDate = s.getCreatedAt().toLocalDate();
                return !createdDate.isBefore(startDate) && !createdDate.isAfter(endDate);
            })
            .count();
    }
    
    private int countActiveBookings(int providerId, LocalDate startDate, LocalDate endDate) {
        List<ServiceBooking> bookings = serviceBookingRepository.findByServiceProviderId(providerId);
        
        // Filter active bookings (PENDING or CONFIRMED) in the date range
        return (int) bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.PENDING || b.getStatus() == BookingStatus.CONFIRMED)
            .filter(b -> {
                LocalDate bookingDate = b.getBookingDateTime().toLocalDate();
                return !bookingDate.isBefore(startDate) && !bookingDate.isAfter(endDate);
            })
            .count();
    }

    private double calculateRevenue(int providerId, LocalDate startDate, LocalDate endDate) {
        List<ServiceBooking> bookings = serviceBookingRepository.findByServiceProviderId(providerId);

        // Filter completed bookings in the date range and sum their prices
        return bookings.stream()
                .filter(b -> b != null && b.getStatus() == BookingStatus.COMPLETED)
                .filter(b -> b.getBookingDateTime() != null)
                .filter(b -> {
                    LocalDate bookingDate = b.getBookingDateTime().toLocalDate();
                    return !bookingDate.isBefore(startDate) && !bookingDate.isAfter(endDate);
                })
                .map(b -> b.getDiscountApplied() ?
                        (b.getDiscountedPrice() != null ? b.getDiscountedPrice() : BigDecimal.ZERO) :
                        (b.getOriginalPrice() != null ? b.getOriginalPrice() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }
    
    /**
     * Calculate total revenue from all completed bookings
     */
    private double calculateTotalRevenue(int providerId) {
        List<ServiceBooking> bookings = serviceBookingRepository.findByServiceProviderId(providerId);

        // Calculate total revenue from all completed bookings
        return bookings.stream()
                .filter(b -> b != null && b.getStatus() == BookingStatus.COMPLETED)
                .map(b -> b.getDiscountApplied() ?
                        (b.getDiscountedPrice() != null ? b.getDiscountedPrice() : BigDecimal.ZERO) :
                        (b.getOriginalPrice() != null ? b.getOriginalPrice() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }
    
    /**
     * Calculate total revenue from completed bookings in a specific date range
     */
    private double calculateTotalRevenue(int providerId, LocalDate startDate, LocalDate endDate) {
        // For date range comparison, we'll reuse the existing calculateRevenue method
        return calculateRevenue(providerId, startDate, endDate);
    }
    
    private double calculateAverageRating(int providerId) {
        List<Review> reviews = reviewRepository.findByServiceProviderId(providerId);
        
        if (reviews.isEmpty()) {
            return 0.0;
        }
        
        // Calculate average rating from all reviews
        return reviews.stream()
            .mapToDouble(Review::getRating)
            .average()
            .orElse(0.0);
    }
    
    private double calculateAverageRating(int providerId, LocalDate startDate, LocalDate endDate) {
        List<Review> reviews = reviewRepository.findByServiceProviderId(providerId);
        
        // Filter reviews created in the date range
        List<Review> filteredReviews = reviews.stream()
            .filter(r -> {
                LocalDate reviewDate = r.getCreatedAt().toLocalDate();
                return !reviewDate.isBefore(startDate) && !reviewDate.isAfter(endDate);
            })
            .collect(Collectors.toList());
        
        if (filteredReviews.isEmpty()) {
            return 0.0;
        }
        
        // Calculate average rating
        return filteredReviews.stream()
            .mapToDouble(Review::getRating)
            .average()
            .orElse(0.0);
    }
    
    private int calculatePercentChange(double oldValue, double newValue) {
        if (oldValue == 0) {
            return newValue > 0 ? 100 : 0; // 100% increase if old value was 0 and new value is positive
        }
        
        return (int) Math.round((newValue - oldValue) / oldValue * 100);
    }
    
    private int countUniqueClients(List<ServiceBooking> bookings) {
        return (int) bookings.stream()
            .map(b -> b.getCustomer().getId())
            .distinct()
            .count();
    }

    private double calculateTotalEarnings(List<ServiceBooking> bookings) {
        return bookings.stream()
                .filter(b -> b != null && b.getStatus() == BookingStatus.COMPLETED)
                .map(b -> b.getDiscountApplied() ?
                        (b.getDiscountedPrice() != null ? b.getDiscountedPrice() : BigDecimal.ZERO) :
                        (b.getOriginalPrice() != null ? b.getOriginalPrice() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }
    
    private int calculateResponseRate(List<ServiceBooking> bookings) {
        if (bookings.isEmpty()) {
            return 0;
        }
        
        // Count bookings that have been responded to (not in PENDING state)
        long respondedBookings = bookings.stream()
            .filter(b -> b.getStatus() != BookingStatus.PENDING)
            .count();
            
        return (int) Math.round((double) respondedBookings / bookings.size() * 100);
    }
    
    public byte[] getServiceProviderImage(long id) throws IOException {
        ServiceProvider serviceProvider = serviceProviderRepository.findById(id)
                .orElseThrow(() -> new UserValidationException("Service provider not found with id: " + id));

        if (serviceProvider.getImage() == null) {
            throw new UserValidationException("Service provider has no image");
        }

        return imageStorageService.getImage(serviceProvider.getImage());
    }
}
