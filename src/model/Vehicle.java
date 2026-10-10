package model;

/**
 * OOP Concept: Encapsulation
 * Represents vehicle details associated with a student driver.
 */
public class Vehicle {

    // Encapsulation: Private data fields
    private String vehicleType;     // e.g., "Bike", "Car"
    private String vehicleNumber;
    private int seatingCapacity;

    // Default constructor
    public Vehicle() {
    }

    // Parameterized constructor
    public Vehicle(String vehicleType, String vehicleNumber, int seatingCapacity) {
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.seatingCapacity = seatingCapacity;
    }

    // Displays vehicle details
    public void displayVehicleDetails() {
        System.out.println("Vehicle: " + vehicleType + " | Reg Number: " + vehicleNumber + " | Capacity: " + seatingCapacity);
    }

    // Getters and Setters
    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(int seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }
}
