package model;

/**
 * OOP Concept: Abstraction & Encapsulation
 * Abstract base class representing a general user in the CampusRide system.
 */
public abstract class User {

    // Encapsulation: Private member variables
    private String name;
    private String studentId;
    private String email;

    // Default constructor
    public User() {
    }

    // Parameterized constructor
    public User(String name, String studentId, String email) {
        this.name = name;
        this.studentId = studentId;
        this.email = email;
    }

    // OOP Concept: Abstraction - abstract method to be implemented by subclasses
    public abstract void displayProfile();

    // Concrete method shared by all subclasses
    public void displayBasicInfo() {
        System.out.println("Name: " + name + " | Student ID: " + studentId + " | Email: " + email);
    }

    // Getters and Setters (Encapsulation)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Encapsulation: Database attribute aliases
    public String getFullName() {
        return name;
    }

    public void setFullName(String fullName) {
        this.name = fullName;
    }

    public String getRegistrationNumber() {
        return studentId;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.studentId = registrationNumber;
    }
}
