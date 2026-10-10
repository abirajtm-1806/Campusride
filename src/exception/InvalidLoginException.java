package exception;

/**
 * OOP Concept: Exception Handling (Custom Checked Exception)
 * Thrown when student authentication fails due to incorrect credentials.
 */
public class InvalidLoginException extends Exception {

    public InvalidLoginException(String message) {
        super(message);
    }
}
