package project.hamrosewa.exceptions;

import project.hamrosewa.model.BookingStatus;

public class InvalidBookingStatusException extends RuntimeException {
    public InvalidBookingStatusException(String message) {
        super(message);
    }
    
    public InvalidBookingStatusException(BookingStatus currentStatus, BookingStatus newStatus) {
        super("Cannot change booking status from " + currentStatus + " to " + newStatus);
    }
}
