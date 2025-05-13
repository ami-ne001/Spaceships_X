package game.DAO;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private static final String SAVE_FILE = "players.dat";
    private Map<String, Integer> playerScores;

    public DatabaseManager() {
        playerScores = new HashMap<>();
        loadPlayersFromFile();
    }

    private void loadPlayersFromFile() {
        File file = new File(SAVE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                playerScores = (Map<String, Integer>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                playerScores = new HashMap<>();
            }
        }
    }

    private void savePlayersToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(playerScores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean playerExists(String username) {
        return playerScores.containsKey(username);
    }

    public void createPlayer(String username, int score) {
        playerScores.put(username, score);
        savePlayersToFile();
    }

    public int getHighscore(String username) {
        return playerScores.getOrDefault(username, 0);
    }

    public void updateHighscore(String username, int score) {
        playerScores.put(username, score);
        savePlayersToFile();
    }
}