package interfaces;

import model.Ride;

/**
 * OOP Concept: Interface & Method Overloading
 * Contract defining operations for offering, searching, and viewing rides.
 */
public interface RideOperations {

    // Post a new ride
    void offerRide(Ride ride);

    // OOP Concept: Method Overloading (Signature 1: Source and Destination)
    void searchRide(String source, String destination);

    // OOP Concept: Method Overloading (Signature 2: Source, Destination, and Travel Date)
    void searchRide(String source, String destination, String date);

    // Display all available rides
    void displayAllRides();
}
