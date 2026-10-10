package service;

import exception.InvalidRideException;
import exception.RideNotFoundException;
import interfaces.RideOperations;
import model.Ride;
import java.util.ArrayList;

/**
 * Service managing ride operations.
 *
 * OOP Concepts Demonstrated:
 * 1. Interface Implementation & Polymorphism: Implements the RideOperations interface contract.
 * 2. Method Overriding: Overrides methods defined in RideOperations (@Override).
 * 3. Method Overloading: Two searchRide() methods with differing parameter lists.
 * 4. Collections Framework: Uses ArrayList<Ride> to dynamically store and manage rides in memory.
 * 5. Exception Handling: Employs custom checked exceptions (InvalidRideException, RideNotFoundException).
 */
public class RideService implements RideOperations {

    // Collections Framework: Dynamic list for storing active Ride objects
    private ArrayList<Ride> rides;

    // Supabase Repository & Integration
    private supabase.SupabaseClient supabaseClient;
    private supabase.SupabaseRideRepository rideRepo;

    // Constructor initializing the in-memory ArrayList
    public RideService() {
        this.rides = new ArrayList<>();
        this.supabaseClient = new supabase.SupabaseClient();
        this.rideRepo = new supabase.SupabaseRideRepository(supabaseClient);
    }

    /**
     * Validates ride details prior to adding to the system.
     *
     * OOP Concept: Exception Handling (throws InvalidRideException on invalid data)
     */
    private void validateRide(Ride ride) throws InvalidRideException {
        if (ride == null) {
            throw new InvalidRideException("Ride cannot be null.");
        }
        if (ride.getRideId() == null || ride.getRideId().trim().isEmpty()) {
            throw new InvalidRideException("Ride ID cannot be empty.");
        }
        if (ride.getStartingLocation() == null || ride.getStartingLocation().trim().isEmpty()) {
            throw new InvalidRideException("Starting location cannot be empty.");
        }
        if (ride.getDestination() == null || ride.getDestination().trim().isEmpty()) {
            throw new InvalidRideException("Destination cannot be empty.");
        }
        if (ride.getAvailableSeats() <= 0) {
            throw new InvalidRideException("Available seats must be greater than 0.");
        }
        if (ride.getCostPerPerson() < 0) {
            throw new InvalidRideException("Cost per person cannot be negative.");
        }

        // Duplicate Check: Prevent posting two rides with the identical ID
        for (Ride existingRide : rides) {
            if (existingRide.getRideId() != null && existingRide.getRideId().equalsIgnoreCase(ride.getRideId().trim())) {
                throw new InvalidRideException("A ride with ID '" + ride.getRideId() + "' already exists.");
            }
        }
    }

    /**
     * Posts a new ride into the system.
     *
     * OOP Concept: Method Overriding (from RideOperations interface)
     * OOP Concept: Exception Handling (catches InvalidRideException to provide friendly feedback)
     * OOP Concept: Collections (adds element to ArrayList)
     */
    @Override
    public void offerRide(Ride ride) {
        try {
            validateRide(ride);
            // Collections Framework: add() appends the ride to the ArrayList
            rides.add(ride);
            // Persist to Supabase if configured
            if (supabase.SupabaseConfig.isConfigured()) {
                rideRepo.saveRide(ride);
            }
            System.out.println("Ride offered successfully! (Ride ID: " + ride.getRideId() + ")");
        } catch (InvalidRideException e) {
            // Exception Handling: Catching custom checked exception
            System.out.println("Failed to offer ride: " + e.getMessage());
        }
    }

    /**
     * Searches for rides matching source and destination.
     *
     * OOP Concept: Method Overloading (Signature 1: 2 parameters - source and destination)
     * OOP Concept: Method Overriding (implements RideOperations contract)
     * OOP Concept: Collections (iterates through ArrayList<Ride>)
     */
    @Override
    public void searchRide(String source, String destination) {
        if (source == null || destination == null || source.trim().isEmpty() || destination.trim().isEmpty()) {
            System.out.println("Search failed: Source and destination must be provided.");
            return;
        }

        System.out.println("\n--- Search Results for Route: " + source + " -> " + destination + " ---");
        boolean found = false;

        // Collections Framework: Enhanced for-loop traversing ArrayList<Ride>
        for (Ride ride : rides) {
            if (ride.getStartingLocation() != null && ride.getStartingLocation().equalsIgnoreCase(source.trim())
                    && ride.getDestination() != null && ride.getDestination().equalsIgnoreCase(destination.trim())) {
                printRideSummary(ride);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No rides found for route from '" + source + "' to '" + destination + "'.");
        }
    }

    /**
     * Searches for rides matching source, destination, and date.
     *
     * OOP Concept: Method Overloading (Signature 2: 3 parameters - source, destination, and date)
     * OOP Concept: Method Overriding (implements RideOperations contract)
     * OOP Concept: Collections (iterates through ArrayList<Ride>)
     */
    @Override
    public void searchRide(String source, String destination, String date) {
        if (source == null || destination == null || date == null
                || source.trim().isEmpty() || destination.trim().isEmpty() || date.trim().isEmpty()) {
            System.out.println("Search failed: Source, destination, and date must all be provided.");
            return;
        }

        System.out.println("\n--- Search Results for Route: " + source + " -> " + destination + " on " + date + " ---");
        boolean found = false;

        // Collections Framework: Enhanced for-loop traversing ArrayList<Ride>
        for (Ride ride : rides) {
            if (ride.getStartingLocation() != null && ride.getStartingLocation().equalsIgnoreCase(source.trim())
                    && ride.getDestination() != null && ride.getDestination().equalsIgnoreCase(destination.trim())
                    && ride.getDate() != null && ride.getDate().equalsIgnoreCase(date.trim())) {
                printRideSummary(ride);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No rides found from '" + source + "' to '" + destination + "' on " + date + ".");
        }
    }

    /**
     * Displays all available rides in the system.
     *
     * OOP Concept: Method Overriding (implements RideOperations contract)
     * OOP Concept: Collections (checks size and iterates through ArrayList)
     */
    @Override
    public void displayAllRides() {
        System.out.println("\n--- All Available Rides ---");
        if (rides.isEmpty()) {
            System.out.println("No rides are currently available.");
            return;
        }

        // Collections Framework: Loop through all items in ArrayList
        for (Ride ride : rides) {
            printRideSummary(ride);
        }
    }

    /**
     * Finds a ride by its unique ride ID.
     *
     * OOP Concept: Exception Handling (throws custom checked RideNotFoundException)
     * OOP Concept: Collections (linear search over ArrayList<Ride>)
     *
     * @param rideId The ID of the ride to find.
     * @return The found Ride object.
     * @throws RideNotFoundException if no ride matches the given ID.
     */
    public Ride findRideById(String rideId) throws RideNotFoundException {
        if (rideId == null || rideId.trim().isEmpty()) {
            throw new RideNotFoundException("Search failed: Ride ID cannot be empty.");
        }

        // Collections Framework: Linear scan through the ArrayList
        for (Ride ride : rides) {
            if (ride.getRideId() != null && ride.getRideId().equalsIgnoreCase(rideId.trim())) {
                return ride;
            }
        }

        // Exception Handling: Throw checked exception when element is absent
        throw new RideNotFoundException("Ride with ID '" + rideId + "' not found.");
    }

    /**
     * Books a seat on a ride.
     *
     * OOP Concept: Exception Handling (propagates RideNotFoundException, throws InvalidRideException)
     * OOP Concept: Encapsulation (updates availableSeats via setter)
     *
     * @param rideId The ID of the ride to book.
     * @return true if booking succeeded.
     * @throws RideNotFoundException if the ride does not exist.
     * @throws InvalidRideException  if no seats are available.
     */
    public boolean bookRide(String rideId) throws RideNotFoundException, InvalidRideException {
        // Find the ride (throws RideNotFoundException if missing)
        Ride ride = findRideById(rideId);

        // Exception Handling: Validate seat availability
        if (ride.getAvailableSeats() <= 0) {
            throw new InvalidRideException("Booking failed: No seats available for Ride ID: " + rideId);
        }

        // Encapsulation: Deduct seat through setter method
        ride.setAvailableSeats(ride.getAvailableSeats() - 1);
        if (supabase.SupabaseConfig.isConfigured()) {
            rideRepo.updateAvailableSeats(rideId, ride.getAvailableSeats());
        }
        System.out.println("Seat booked successfully on Ride ID: " + rideId + ". Seats remaining: " + ride.getAvailableSeats());
        return true;
    }

    /**
     * Helper method to print formatted ride information to the console.
     */
    private void printRideSummary(Ride ride) {
        String driverName = (ride.getDriver() != null) ? ride.getDriver().getName() : "N/A";
        System.out.println("----------------------------------------");
        System.out.println("Ride ID   : " + ride.getRideId());
        System.out.println("Driver    : " + driverName);
        System.out.println("Route     : " + ride.getStartingLocation() + " -> " + ride.getDestination());
        System.out.println("Date/Time : " + ride.getDate() + " at " + ride.getDepartureTime());
        System.out.println("Seats Left: " + ride.getAvailableSeats());
        System.out.println("Cost      : $" + String.format("%.2f", ride.getCostPerPerson()) + " per person");
    }

    // Search helper returning ArrayList for Swing UI table binding
    public ArrayList<Ride> findRides(String source, String destination) {
        ArrayList<Ride> results = new ArrayList<>();
        if (source == null || destination == null) return results;

        for (Ride ride : rides) {
            if (ride.getStartingLocation() != null && ride.getStartingLocation().equalsIgnoreCase(source.trim())
                    && ride.getDestination() != null && ride.getDestination().equalsIgnoreCase(destination.trim())) {
                results.add(ride);
            }
        }
        return results;
    }

    public ArrayList<Ride> findRides(String source, String destination, String date) {
        ArrayList<Ride> results = new ArrayList<>();
        if (source == null || destination == null || date == null) return results;

        for (Ride ride : rides) {
            if (ride.getStartingLocation() != null && ride.getStartingLocation().equalsIgnoreCase(source.trim())
                    && ride.getDestination() != null && ride.getDestination().equalsIgnoreCase(destination.trim())
                    && ride.getDate() != null && ride.getDate().equalsIgnoreCase(date.trim())) {
                results.add(ride);
            }
        }
        return results;
    }

    public ArrayList<Ride> getRidesByDriver(String studentId) {
        ArrayList<Ride> results = new ArrayList<>();
        if (studentId == null) return results;

        for (Ride ride : rides) {
            if (ride.getDriver() != null && studentId.equalsIgnoreCase(ride.getDriver().getStudentId())) {
                results.add(ride);
            }
        }
        return results;
    }

    public supabase.SupabaseRideRepository getRideRepository() {
        return rideRepo;
    }

    // Getters and Setters
    public ArrayList<Ride> getRides() {
        return rides;
    }

    public void setRides(ArrayList<Ride> rides) {
        this.rides = rides;
    }
}
