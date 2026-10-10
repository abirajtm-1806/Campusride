import ui.CampusRideUI;

/**
 * Main application launcher for CampusRide.
 * Acts solely as the entry point to instantiate and start the UI layer.
 */
public class Main {

    public static void main(String[] args) {
        CampusRideUI ui = new CampusRideUI();
        ui.start();
    }
}
