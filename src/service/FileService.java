package service;

import model.Driver;
import model.Ride;
import model.Student;
import model.Vehicle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Service handling persistence to and from plain text files.
 *
 * NOTE ON ARCHITECTURE & ACADEMIC REQUIREMENTS:
 * FileService = demonstrates Java File Handling (academic OOP requirement).
 * Supabase Repository = actual application persistence.
 *
 * OOP Concepts Demonstrated:
 * 1. File Handling: Using File, FileWriter, FileReader, BufferedReader, and BufferedWriter.
 * 2. Exception Handling: Proper try-catch blocks managing checked IOException and NumberFormatException.
 * 3. Collections Framework: Loading data into ArrayList<Student> and ArrayList<Ride>.
 * 4. Object Graph Reconstruction: Parsing flat text into composite Ride, Driver, and Vehicle objects.
 *
 * Storage Format for Rides (data/rides.txt):
 * rideId,startingLocation,destination,date,departureTime,availableSeats,costPerPerson,driverName,driverId,driverEmail,driverPassword,vehicleType,vehicleNumber,vehicleCapacity
 * Example:
 * RIDE-101,North Campus,City Center,2026-10-15,09:30 AM,3,15.50,Bob Vance,STU102,bob@college.edu,pass123,Car,ABC-1234,4
 *
 * Storage Format for Students (data/students.txt):
 * name,studentId,email,password
 * Example:
 * Alice Smith,STU101,alice@college.edu,pass123
 */
public class FileService {

    private String studentsFilePath;
    private String ridesFilePath;

    // Default constructor pointing to standard data directory
    public FileService() {
        this.studentsFilePath = "data/students.txt";
        this.ridesFilePath = "data/rides.txt";
    }

    // Parameterized constructor allowing custom file locations
    public FileService(String studentsFilePath, String ridesFilePath) {
        this.studentsFilePath = studentsFilePath;
        this.ridesFilePath = ridesFilePath;
    }

    /**
     * FILE HANDLING & EXCEPTION HANDLING:
     * Helper method to ensure parent directory ("data") and the text file exist.
     * Automatically creates them if they are missing.
     */
    private void ensureFileExists(File file) {
        try {
            File parentDir = file.getParentFile();
            // Create "data" directory if it does not exist
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            // Create target file if it does not exist
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            // EXCEPTION HANDLING: Catch and report file creation errors
            System.out.println("Error initializing file '" + file.getPath() + "': " + e.getMessage());
        }
    }

    /**
     * Persists a single student record to the students text file.
     *
     * FILE HANDLING: Uses FileWriter in append mode (true) wrapped inside BufferedWriter.
     * EXCEPTION HANDLING: try-catch-resources to automatically close writer and handle IOException.
     *
     * @param student The Student object to save.
     */
    public void saveStudent(Student student) {
        if (student == null) {
            System.out.println("Cannot save null student record.");
            return;
        }

        // FILE HANDLING: Target file
        File file = new File(studentsFilePath);
        ensureFileExists(file);

        // FILE HANDLING & EXCEPTION HANDLING: try-with-resources handles closing streams automatically
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            String record = student.getName() + ","
                          + student.getStudentId() + ","
                          + student.getEmail() + ","
                          + student.getPassword();
            writer.write(record);
            writer.newLine(); // Move to next line
            System.out.println("Student record persisted to file: " + student.getName());
        } catch (IOException e) {
            // EXCEPTION HANDLING: Catches I/O write failures
            System.out.println("Error saving student to file: " + e.getMessage());
        }
    }

    /**
     * Persists a single ride record to the rides text file.
     * Flattens Ride, Driver, and Vehicle attributes into a single delimited record.
     *
     * FILE HANDLING: Uses FileWriter in append mode (true) wrapped inside BufferedWriter.
     * EXCEPTION HANDLING: try-catch handles IOException gracefully without crashing.
     *
     * @param ride The Ride object to save.
     */
    public void saveRide(Ride ride) {
        if (ride == null) {
            System.out.println("Cannot save null ride record.");
            return;
        }

        // FILE HANDLING: Target file
        File file = new File(ridesFilePath);
        ensureFileExists(file);

        // Extract Driver and Vehicle properties safely
        String driverName = "Unknown Driver";
        String driverId = "N/A";
        String driverEmail = "N/A";
        String driverPassword = "N/A";
        String vehicleType = "Car";
        String vehicleNumber = "N/A";
        int vehicleCapacity = ride.getAvailableSeats() > 0 ? ride.getAvailableSeats() : 1;

        if (ride.getDriver() != null) {
            Driver driver = ride.getDriver();
            if (driver.getName() != null) driverName = driver.getName();
            if (driver.getStudentId() != null) driverId = driver.getStudentId();
            if (driver.getEmail() != null) driverEmail = driver.getEmail();
            if (driver.getPassword() != null) driverPassword = driver.getPassword();

            if (driver.getVehicle() != null) {
                Vehicle vehicle = driver.getVehicle();
                if (vehicle.getVehicleType() != null) vehicleType = vehicle.getVehicleType();
                if (vehicle.getVehicleNumber() != null) vehicleNumber = vehicle.getVehicleNumber();
                if (vehicle.getSeatingCapacity() > 0) vehicleCapacity = vehicle.getSeatingCapacity();
            }
        }

        // FILE HANDLING & EXCEPTION HANDLING: Write delimited line
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            String record = ride.getRideId() + ","
                    + ride.getStartingLocation() + ","
                    + ride.getDestination() + ","
                    + ride.getDate() + ","
                    + ride.getDepartureTime() + ","
                    + ride.getAvailableSeats() + ","
                    + ride.getCostPerPerson() + ","
                    + driverName + ","
                    + driverId + ","
                    + driverEmail + ","
                    + driverPassword + ","
                    + vehicleType + ","
                    + vehicleNumber + ","
                    + vehicleCapacity;
            writer.write(record);
            writer.newLine();
            System.out.println("Ride record persisted to file: " + ride.getRideId());
        } catch (IOException e) {
            // EXCEPTION HANDLING: Catches I/O write failures
            System.out.println("Error saving ride to file: " + e.getMessage());
        }
    }

    /**
     * Reads all student records from students.txt into an ArrayList.
     *
     * FILE HANDLING: Uses FileReader wrapped inside BufferedReader for line-by-line reading.
     * EXCEPTION HANDLING: try-catch catches IOException and safely returns list loaded so far.
     *
     * @return ArrayList<Student> containing all students found in the file.
     */
    public ArrayList<Student> loadStudents() {
        ArrayList<Student> students = new ArrayList<>();

        // FILE HANDLING: Target file
        File file = new File(studentsFilePath);
        ensureFileExists(file);

        // FILE HANDLING & EXCEPTION HANDLING: Read text file line by line
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comment lines (starting with #)
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Parse comma-separated fields: name,studentId,email,password
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String name = parts[0].trim();
                    String studentId = parts[1].trim();
                    String email = parts[2].trim();
                    String password = parts[3].trim();

                    Student student = new Student(name, studentId, email, password);
                    students.add(student);
                }
            }
            System.out.println("Loaded " + students.size() + " student(s) from file.");
        } catch (IOException e) {
            // EXCEPTION HANDLING: Catch file reading errors
            System.out.println("Error reading students from file: " + e.getMessage());
        }

        return students;
    }

    /**
     * Reads all ride records from rides.txt into an ArrayList.
     * Reconstructs Ride, Driver, and Vehicle objects from the stored text fields.
     *
     * FILE HANDLING: Uses FileReader wrapped inside BufferedReader.
     * EXCEPTION HANDLING: Catches IOException for reading and NumberFormatException for numeric parsing.
     *
     * @return ArrayList<Ride> containing all rides parsed from the file.
     */
    public ArrayList<Ride> loadRides() {
        ArrayList<Ride> rides = new ArrayList<>();

        // FILE HANDLING: Target file
        File file = new File(ridesFilePath);
        ensureFileExists(file);

        // FILE HANDLING & EXCEPTION HANDLING: Read line by line
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comment lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(",");
                try {
                    if (parts.length >= 14) {
                        // Full 14-field format with Driver & Vehicle composition
                        String rideId = parts[0].trim();
                        String startingLocation = parts[1].trim();
                        String destination = parts[2].trim();
                        String date = parts[3].trim();
                        String departureTime = parts[4].trim();
                        int availableSeats = Integer.parseInt(parts[5].trim());
                        double costPerPerson = Double.parseDouble(parts[6].trim());

                        String driverName = parts[7].trim();
                        String driverId = parts[8].trim();
                        String driverEmail = parts[9].trim();
                        String driverPassword = parts[10].trim();

                        String vehicleType = parts[11].trim();
                        String vehicleNumber = parts[12].trim();
                        int vehicleCapacity = Integer.parseInt(parts[13].trim());

                        // Reconstruct composite objects
                        Vehicle vehicle = new Vehicle(vehicleType, vehicleNumber, vehicleCapacity);
                        Driver driver = new Driver(driverName, driverId, driverEmail, driverPassword, vehicle);
                        Ride ride = new Ride(rideId, driver, startingLocation, destination, date, departureTime, availableSeats, costPerPerson);
                        rides.add(ride);
                    } else if (parts.length >= 8) {
                        // Fallback support for basic 8-column format
                        String rideId = parts[0].trim();
                        String driverId = parts[1].trim();
                        String startingLocation = parts[2].trim();
                        String destination = parts[3].trim();
                        String date = parts[4].trim();
                        String departureTime = parts[5].trim();
                        int availableSeats = Integer.parseInt(parts[6].trim());
                        double costPerPerson = Double.parseDouble(parts[7].trim());

                        Vehicle vehicle = new Vehicle("Car", "CAMPUS-01", availableSeats + 1);
                        Driver driver = new Driver("Student Driver", driverId, driverId.toLowerCase() + "@college.edu", "pass123", vehicle);
                        Ride ride = new Ride(rideId, driver, startingLocation, destination, date, departureTime, availableSeats, costPerPerson);
                        rides.add(ride);
                    }
                } catch (NumberFormatException nfe) {
                    // EXCEPTION HANDLING: Catch invalid numeric format without aborting entire file read
                    System.out.println("Warning: Skipping malformed ride line: " + line);
                }
            }
            System.out.println("Loaded " + rides.size() + " ride(s) from file.");
        } catch (IOException e) {
            // EXCEPTION HANDLING: Catch file reading errors
            System.out.println("Error reading rides from file: " + e.getMessage());
        }

        return rides;
    }

    /**
     * Helper method to overwrite all rides in rides.txt with updated list data.
     * Useful when a ride is booked and remaining seats must be persisted.
     */
    public void saveAllRides(ArrayList<Ride> ridesList) {
        if (ridesList == null) return;

        File file = new File(ridesFilePath);
        ensureFileExists(file);

        // false in FileWriter overwrites the file instead of appending
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write("# Format: RideID,StartingLocation,Destination,Date,DepartureTime,AvailableSeats,CostPerPerson,DriverName,DriverID,DriverEmail,DriverPassword,VehicleType,VehicleNumber,VehicleCapacity");
            writer.newLine();

            for (Ride ride : ridesList) {
                String driverName = "Unknown Driver";
                String driverId = "N/A";
                String driverEmail = "N/A";
                String driverPassword = "N/A";
                String vehicleType = "Car";
                String vehicleNumber = "N/A";
                int vehicleCapacity = ride.getAvailableSeats() > 0 ? ride.getAvailableSeats() : 1;

                if (ride.getDriver() != null) {
                    Driver driver = ride.getDriver();
                    if (driver.getName() != null) driverName = driver.getName();
                    if (driver.getStudentId() != null) driverId = driver.getStudentId();
                    if (driver.getEmail() != null) driverEmail = driver.getEmail();
                    if (driver.getPassword() != null) driverPassword = driver.getPassword();

                    if (driver.getVehicle() != null) {
                        Vehicle vehicle = driver.getVehicle();
                        if (vehicle.getVehicleType() != null) vehicleType = vehicle.getVehicleType();
                        if (vehicle.getVehicleNumber() != null) vehicleNumber = vehicle.getVehicleNumber();
                        if (vehicle.getSeatingCapacity() > 0) vehicleCapacity = vehicle.getSeatingCapacity();
                    }
                }

                String record = ride.getRideId() + ","
                        + ride.getStartingLocation() + ","
                        + ride.getDestination() + ","
                        + ride.getDate() + ","
                        + ride.getDepartureTime() + ","
                        + ride.getAvailableSeats() + ","
                        + ride.getCostPerPerson() + ","
                        + driverName + ","
                        + driverId + ","
                        + driverEmail + ","
                        + driverPassword + ","
                        + vehicleType + ","
                        + vehicleNumber + ","
                        + vehicleCapacity;
                writer.write(record);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving all rides to file: " + e.getMessage());
        }
    }

    /**
     * Helper method to overwrite all students in students.txt with updated list data.
     */
    public void saveAllStudents(ArrayList<Student> studentsList) {
        if (studentsList == null) return;

        File file = new File(studentsFilePath);
        ensureFileExists(file);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write("# Format: Name,StudentID,Email,Password");
            writer.newLine();
            for (Student student : studentsList) {
                String record = student.getName() + ","
                              + student.getStudentId() + ","
                              + student.getEmail() + ","
                              + student.getPassword();
                writer.write(record);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving all students to file: " + e.getMessage());
        }
    }

    // Getters and Setters
    public String getStudentsFilePath() {
        return studentsFilePath;
    }

    public void setStudentsFilePath(String studentsFilePath) {
        this.studentsFilePath = studentsFilePath;
    }

    public String getRidesFilePath() {
        return ridesFilePath;
    }

    public void setRidesFilePath(String ridesFilePath) {
        this.ridesFilePath = ridesFilePath;
    }
}
