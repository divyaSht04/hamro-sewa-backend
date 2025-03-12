package project.hamrosewa.exceptions;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String message) {
        super(message);
    }
    
    public BookingNotFoundException(Long id) {
        super("Booking not found with id: " + id);
    }
}
