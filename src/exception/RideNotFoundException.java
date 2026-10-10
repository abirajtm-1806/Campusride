package exception;

/**
 * OOP Concept: Exception Handling (Custom Checked Exception)
 * Thrown when a specified ride cannot be found in the system.
 */
public class RideNotFoundException extends Exception {

    public RideNotFoundException(String message) {
        super(message);
    }
}
