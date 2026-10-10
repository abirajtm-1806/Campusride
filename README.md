# CampusRide – College Student Ride Sharing System

CampusRide is an academic Java Object-Oriented Programming (OOP) mini-project designed as a college-exclusive ride-sharing console application. It enables students to register, authenticate, offer rides, search for rides, and manage their trips while enforcing clear separation between the presentation layer, business services, data models, and persistence.

---

## Project Structure

```text
CampusRide/
│
├── src/
│   ├── model/
│   │   ├── User.java                     # Abstract base class
│   │   ├── Student.java                  # Extends User, implements Authenticatable
│   │   ├── Driver.java                   # Extends Student (Multilevel inheritance)
│   │   ├── Ride.java                     # Ride entity (Composition/Aggregation)
│   │   └── Vehicle.java                  # Vehicle entity (Encapsulation)
│   │
│   ├── service/
│   │   ├── AuthService.java              # User registration and session management
│   │   ├── RideService.java              # Ride publishing, searching, and booking
│   │   └── FileService.java              # Persistence using BufferedReader/BufferedWriter
│   │
│   ├── exception/
│   │   ├── InvalidLoginException.java    # Custom checked exception for auth failures
│   │   ├── InvalidRideException.java     # Custom checked exception for invalid ride data
│   │   └── RideNotFoundException.java    # Custom checked exception for missing rides
│   │
│   ├── interfaces/
│   │   ├── Authenticatable.java          # Authentication contract
│   │   └── RideOperations.java           # Ride management and search contracts
│   │
│   ├── ui/
│   │   └── CampusRideUI.java             # Console presentation layer using Scanner
│   │
│   └── Main.java                         # Application entry point
│
├── data/
│   ├── students.txt                      # Flat-file storage for student records
│   └── rides.txt                         # Flat-file storage for ride postings
│
└── README.md
```

---

## Architectural Flow

```text
+-------------------+
|   CampusRideUI    |  (Handles Scanner input, menus, printing)
+-------------------+
          |
          v calls
+------------------------------------------------+
|   AuthService   |   RideService   | FileService|  (Business logic & operations)
+------------------------------------------------+
          |
          v manipulates
+------------------------------------------------+
|     User / Student / Driver / Vehicle / Ride   |  (Data models & state)
+------------------------------------------------+
```

---

## OOP Concepts Mapping

| # | OOP Concept | Where It Appears in CampusRide | Implementation Details |
|---|-------------|--------------------------------|-------------------------|
| 1 | **Classes and Objects** | All `.java` files | Domain entities (`User`, `Student`, `Driver`, `Vehicle`, `Ride`) instantiated at runtime. |
| 2 | **Encapsulation** | All model and service classes | Private fields with public getters and setters controlling state access and data validation. |
| 3 | **Inheritance** | `Student extends User`<br>`Driver extends Student` | Single inheritance (`Student -> User`) and multilevel inheritance (`Driver -> Student -> User`). |
| 4 | **Polymorphism** | `displayProfile()` overriding<br>`RideOperations` interface references | Dynamic method dispatch when invoking polymorphic profile views or operating on `User` references. |
| 5 | **Abstraction** | `User` abstract class<br>`displayProfile()` abstract method | Hides implementation details while enforcing subclasses to provide specific profile display logic. |
| 6 | **Interfaces** | `Authenticatable`, `RideOperations` | Contracts defining decoupled capabilities (`login`/`logout`, `offerRide`/`searchRide`). |
| 7 | **Constructors** | Default and parameterized in all models | Constructor overloading and constructor chaining via `super()` in subclasses. |
| 8 | **Method Overloading** | `RideOperations` & `RideService` | `searchRide(source, destination)` and `searchRide(source, destination, date)` with differing parameter lists. |
| 9 | **Method Overriding** | `Student`, `Driver`, `RideService` | Subclasses and service implementations overriding methods using `@Override`. |
| 10 | **Exception Handling** | `exception/` package | Custom checked exceptions (`InvalidLoginException`, `InvalidRideException`, `RideNotFoundException`) with try-catch/throws. |
| 11 | **Collections Framework** | `AuthService`, `RideService` | Dynamic in-memory data structures using `ArrayList<Student>` and `ArrayList<Ride>`. |
| 12 | **File Handling** | `FileService` | Persistence to flat files (`students.txt`, `rides.txt`) via `FileReader`, `FileWriter`, `BufferedReader`, and `BufferedWriter`. |

---

## How to Compile and Run

From the `CampusRide` root directory:

### 1. Compile All Sources
```bash
javac -d bin src/interfaces/*.java src/model/*.java src/exception/*.java src/service/*.java src/ui/*.java src/Main.java
```

### 2. Run the Application
```bash
java -cp bin Main
```
