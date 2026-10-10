package exception;

/**
 * OOP Concept: Exception Handling (Custom Checked Exception)
 * Thrown when ride data or booking parameters are invalid (e.g., negative seats/fare, full ride).
 */
public class InvalidRideException extends Exception {

    public InvalidRideException(String message) {
        super(message);
    }
}
