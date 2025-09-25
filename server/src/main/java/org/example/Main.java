package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // wrapping it in order to catch any
        // exceptions.
        try (var server = new GameServer()) {
            server.start()
                    .ifError(Main::handleError);
        } catch (IOException e) {
            System.out.println("Could not start the game server.");
        }
    }

    private static void handleError(ServerError se) {
        switch (se) {
            case ServerError.InitError ie ->
                System.out.println("Could not initialize server socket: "
                        + ie.exception().getMessage());

            case ServerError.InvalidHandshake h ->
                System.out.println("Received an invalid handshake");

            case ServerError.ClientConnectError e ->
                System.out.println("Could not connect to client socket: "
                        + e.exception().getMessage());

            case ServerError.ClientPropertyError e ->
                System.out.println("Invalid properties received from Client");

            case ServerError.IOError e ->
                System.out.println("Communication error: "
                        + e.exception().getMessage());
        }
    }
}
