package org.client;

import org.client.errorhandling.*;

import java.io.IOException;
import java.util.logging.Level;

class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Run program with player name as argument.");
            System.exit(1);
        }

        LOG.setDebug(true);
        LOG.debug(Level.INFO, "Starting client");

        try (var client = new GameClient(args[0])) {
            client.connectToServer();
            client.constructBoard();
        } catch (ClientException e) {

            handleException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void handleException(ClientException e) {
        switch (e.error) {
            case ClientError.InitError i ->
                e.printStackTrace();

            case ClientError.InvalidHandshake i ->
                System.err.println("Received invalid handshake.");

            case ClientError.ServerConnectError c ->
                System.err.println("Could not connect to server: " + c.e().getMessage());

            case ClientError.ExchangeError ex ->
                System.err.println("Error while sending properties: " + ex.e().getMessage());
        }
    }
}
