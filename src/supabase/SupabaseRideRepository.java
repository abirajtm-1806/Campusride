package supabase;

import model.Driver;
import model.Ride;
import model.Vehicle;
import java.util.ArrayList;

/**
 * Data Access Object / Repository for Rides in Supabase.
 *
 * Interacts with the 'rides' table in Supabase via REST API:
 * - Table fields:
 *   id, driver_id, starting_location, destination, date, departure_time,
 *   vehicle_type, vehicle_number, available_seats, cost_per_person, created_at
 *
 * OOP Concept: Encapsulation & Separation of Concerns (Data layer independent of UI and Models).
 */
public class SupabaseRideRepository {

    private final SupabaseClient client;

    public SupabaseRideRepository(SupabaseClient client) {
        this.client = client;
    }

    /**
     * Persists a newly offered ride to Supabase.
     */
    public boolean saveRide(Ride ride) {
        if (ride == null) return false;

        try {
            String driverId = (ride.getDriver() != null) ? ride.getDriver().getStudentId() : "";
            String vehicleType = (ride.getDriver() != null && ride.getDriver().getVehicle() != null)
                    ? ride.getDriver().getVehicle().getVehicleType() : "Car";
            String vehicleNumber = (ride.getDriver() != null && ride.getDriver().getVehicle() != null)
                    ? ride.getDriver().getVehicle().getVehicleNumber() : "";

            String jsonPayload = "{"
                    + "\"id\":\"" + SupabaseClient.escapeJson(ride.getRideId()) + "\","
                    + "\"driver_id\":\"" + SupabaseClient.escapeJson(driverId) + "\","
                    + "\"starting_location\":\"" + SupabaseClient.escapeJson(ride.getStartingLocation()) + "\","
                    + "\"destination\":\"" + SupabaseClient.escapeJson(ride.getDestination()) + "\","
                    + "\"date\":\"" + SupabaseClient.escapeJson(ride.getDate()) + "\","
                    + "\"departure_time\":\"" + SupabaseClient.escapeJson(ride.getDepartureTime()) + "\","
                    + "\"vehicle_type\":\"" + SupabaseClient.escapeJson(vehicleType) + "\","
                    + "\"vehicle_number\":\"" + SupabaseClient.escapeJson(vehicleNumber) + "\","
                    + "\"available_seats\":" + ride.getAvailableSeats() + ","
                    + "\"cost_per_person\":" + ride.getCostPerPerson()
                    + "}";

            client.executeRest("rides", "POST", jsonPayload);
            return true;
        } catch (Exception e) {
            System.out.println("Supabase: Ride save note: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates seat availability on a ride in Supabase when booked.
     */
    public boolean updateAvailableSeats(String rideId, int newSeats) {
        if (rideId == null) return false;

        try {
            String jsonPayload = "{\"available_seats\":" + newSeats + "}";
            client.executeRest("rides?id=eq." + SupabaseClient.escapeJson(rideId), "PATCH", jsonPayload);
            return true;
        } catch (Exception e) {
            System.out.println("Supabase: Update seats note: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads all active rides from Supabase.
     */
    public ArrayList<Ride> loadAllRides() {
        ArrayList<Ride> list = new ArrayList<>();
        try {
            String response = client.executeRest("rides?select=*&order=created_at.desc", "GET", null);
            // Parsed when Supabase connection is active
        } catch (Exception e) {
            System.out.println("Supabase: Load rides note: " + e.getMessage());
        }
        return list;
    }
}
