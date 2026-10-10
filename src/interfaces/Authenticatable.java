package interfaces;

/**
 * OOP Concept: Interface
 * Defines the contract for authenticatable entities within the system.
 */
public interface Authenticatable {

    boolean login(String email, String password);

    void logout();
}
