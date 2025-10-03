package org.client;

import org.client.errorhandling.*;

import org.shared.Ship;

import static org.shared.Constants.BOARD_WIDTH;
import static org.shared.Constants.NUM_SHIPS;

import java.io.IOException;
import java.util.logging.Level;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Run program with player name as argument.");
            System.exit(1);
        }

        LOG.setDebug(false);
        LOG.debug(Level.INFO, "Starting client");

        var ships = placeShips();
        try (var client = new GameClient(args[0], ships)) {
            client.connectToServer();
        } catch (ClientException e) {

            handleException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: Move to separate `ShipBoard` class
    // - Also add bound checks on placement
    private static Ship[] placeShips() {
        var ships = new Ship[5];
        try (var scanner = new Scanner(System.in)) {
            for (int i = 0; i < NUM_SHIPS; i++) {
                printBoard(ships);
                int length = i + 1;
                System.out.printf("Place ship (length: %d):\n", length);

                System.out.print("Enter column: ");
                int posX = scanner.nextInt() - 1;
                scanner.nextLine(); // trim remaining newline

                System.out.print("Enter row: ");
                int posY = scanner.nextInt() - 1;
                scanner.nextLine();

                Ship.Orientation o = null;

                while (o == null) {
                    System.out.print("Enter orientation (vertical=v/horizontal=h): ");
                     o = switch(scanner.nextLine().trim()) {
                        case "v", "vert", "vertical" -> Ship.Orientation.Vertical;
                        case "h", "hori", "horizontal" -> Ship.Orientation.Horizontal;
                        default -> {
                            System.out.println("Not a valid orientation.");
                            yield null;
                        }
                    };
                }

                ships[i] = new Ship(
                    posX,
                    posY,
                    length,
                    o
                );
            }
            printBoard(ships);

            System.out.println("Continue with this board? (Y/n)");

            var a = scanner.nextLine().trim();

            if (a.equalsIgnoreCase("y") || a.equalsIgnoreCase("yes") || a.isEmpty()) {
                return ships;
            } else {
                scanner.close();
                return placeShips();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    private static void printBoard(Ship[] ships) {
        final int colWidth = 3;
        var boardStr = new StringBuilder();

        boardStr.append("  ");
        for (int i = 0; i < BOARD_WIDTH; i++) {
            boardStr.append("  %d ".formatted(i + 1));
        }
        boardStr.append('\n');

        boardStr.append("  +" + "---+".repeat(BOARD_WIDTH) + '\n');
        for (int y = 0; y < BOARD_WIDTH; y++) {
            boardStr.append("%d |".formatted(y + 1));
            for (int x = 0; x < BOARD_WIDTH; x++) {
                boolean isShip = false;
                for (var ship : ships) {
                    if (ship != null && ship.isAt(x, y)) {
                        isShip = true;
                        break;
                    }
                }

                if (isShip)
                    boardStr.append(" X |");
                else
                    boardStr.append("   |");
            }
            boardStr.append('\n');
            boardStr.append("  +" + "---+".repeat(BOARD_WIDTH) + '\n');
        }
        System.out.println(boardStr.toString());
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
