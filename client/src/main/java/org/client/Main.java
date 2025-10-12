package org.client;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;

import org.client.errorhandling.ClientError;
import org.client.errorhandling.ClientException;
import org.shared.LOG;
import org.shared.ShipBoard;

class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Run program with player name as argument.");
            System.exit(1);
        }

        LOG.setDebug(false);
        LOG.debug(Level.INFO, "Starting client");

        try {
            var scanner = new Scanner(System.in);
            var board = ShipBoard.fromUserInput(scanner);
            var client = new GameClient(args[0], board);

            client.connectToServer();

            scanner.close();
            client.close();
        } catch (ClientException e) {
            handleException(e);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            handleException(ClientException.of(new ClientError.UserIOError(e)));
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

            case ClientError.UserIOError u ->
                System.err.println("Error while reading user input from stdin" + u.e().getMessage());
        }
    }
}
