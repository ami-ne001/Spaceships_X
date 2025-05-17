package game.managers;

import game.UI.Ship;

import java.util.ArrayList;
import java.util.List;

public class ShipManager {
    private final List<Ship> availableShips;

    public ShipManager() {
        this.availableShips = new ArrayList<>();
        initializeShips();
    }

    private void initializeShips() {
        // Add all ships with their stats and image paths
        availableShips.add(new Ship(
                "Flash",
                6,  // speed
                2,  // health
                "/player/playership1.png"  // Path in resources folder
        ));

        availableShips.add(new Ship(
                "Titan",
                3,  // slower but tankier
                5,
                "/player/playership2.png"
        ));

        availableShips.add(new Ship(
                "Balance",
                4,  // balanced
                3,
                "/player/playership3.png"
        ));
    }

    public List<Ship> getAvailableShips() { return availableShips; }
}