package org.server;

import java.io.IOException;

import org.shared.LOG;

public class Main {
    public static void main(String[] args) {
        LOG.setDebug(true);
        // wrapping it in order to catch any
        // exceptions.
        try (var server = new GameServer()) {
            server.start();
        } catch (IOException e) {
            System.out.println("Could not start the game server.");
        }
    }

}
