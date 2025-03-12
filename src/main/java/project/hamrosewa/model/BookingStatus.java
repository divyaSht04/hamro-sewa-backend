package project.hamrosewa.model;

/**
 * Enum representing the possible statuses of a service booking
 */
public enum BookingStatus {
    PENDING,    // Booking is waiting for service provider approval
    CONFIRMED,  // Booking has been confirmed by service provider
    COMPLETED,  // Service has been completed
    CANCELLED,  // Booking was cancelled
    REJECTED    // Booking was rejected by service provider
}
