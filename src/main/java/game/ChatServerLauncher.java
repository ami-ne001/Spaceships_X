package game;

import game.network.ChatServer;

/**
 * Standalone launcher for the Chat Server
 * Run this before starting game instances to enable chat functionality
 */
public class ChatServerLauncher {
    public static void main(String[] args) {
        System.out.println("Starting Chat Server on port 5001...");
        
        // Start the chat server
        ChatServer.getInstance().start();
        
        System.out.println("Chat Server is now running!");
        System.out.println("Press Ctrl+C to stop the server");
        
        // Keep the main thread alive
        while(true) {
            try {
                Thread.sleep(10000);
            } catch(InterruptedException e) {
                System.out.println("Server interrupted, shutting down...");
                ChatServer.getInstance().stop();
                break;
            }
        }
    }
} 