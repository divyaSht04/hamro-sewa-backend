package project.hamrosewa.model;

/**
 * Enum representing the possible statuses of a provider service
 */
public enum ServiceStatus {
    PENDING,    // Service is waiting for admin approval
    APPROVED,   // Service has been approved by admin
    REJECTED    // Service has been rejected by admin
}
