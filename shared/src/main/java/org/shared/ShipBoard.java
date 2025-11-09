package org.shared;

import static org.shared.Constants.NUM_SHIPS;
import static org.shared.Constants.BOARD_WIDTH;

import static org.shared.Utils.printError;
import static org.shared.Utils.promptInt;

import java.util.Scanner;

import org.shared.Ship.Orientation;

public class ShipBoard {
    private final Ship[] ships;

    private ShipBoard() {
        this.ships = new Ship[NUM_SHIPS];
    }

    /**
     * @param x coordinate
     * @param y coordinate
     * @return a boolean indicating whether a ship was hit.
     */
    public boolean registerHit(int x, int y) {
        for (var ship : ships) {
            if (ship.registerHit(x, y))
                return true;
        }
        return false;
    }

    private static boolean hasShipAt(Ship[] ships, int x, int y) {
        for (var ship : ships) {
            if (ship != null && ship.isAt(x, y)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasShipAt(int x, int y) {
        return hasShipAt(ships, x, y);
    }

    public Ship getShipAt(int x, int y) {
        for (var ship : ships) {
            if (ship != null && ship.isAt(x, y)) {
                return ship;
            }
        }
        return null;
    }

    public boolean hasAliveShips() {
        for (var ship : ships) {
            if (ship.isAlive())
                return true;
        }
        return false;
    }

    private static Ship.Orientation promptOrientation(Scanner scanner) {
        while (true) {
            System.out.print("Enter orientation (vertical=v/horizontal=h): ");
            switch (scanner.nextLine().trim()) {
                case "v", "vertical" -> {
                    return Ship.Orientation.Vertical;
                }
                case "h", "horizontal" -> {
                    return Ship.Orientation.Horizontal;
                }
                default ->
                    printError("Not a valid orientation.");
            }
        }
    }

    /**
     * @return Returns true if the given ship would overlap a ship that is already
     *         on the board.
     */

    private static boolean hasOverlap(Ship[] ships, int x, int y, int length, Orientation o) {
        for (int i = 0; i < length; i++) {
            switch (o) {
                // PERF: isShipAt loops over ships every call
                case Vertical -> {
                    if (hasShipAt(ships, x, y + i))
                        return true;
                }
                // PERF: isShipAt loops over ships every call
                case Horizontal -> {
                    if (hasShipAt(ships, x + i, y))
                        return true;
                }
            }
        }
        return false;
    }

    private static boolean hasOverlap(Ship[] ships, Ship ship) {
        return hasOverlap(
                ships,
                ship.getX(),
                ship.getY(),
                ship.getLength(),
                ship.getOrientation());
    }

    /**
     * Checks whether the ship fits into the board.
     */
    private static boolean isWithinBounds(Ship ship) {
        return isWithinBounds(
                ship.getX(),
                ship.getY(),
                ship.getLength(),
                ship.getOrientation());
    }

    private static boolean isWithinBounds(int x, int y, int length, Ship.Orientation o) {
        return switch (o) {
            case Ship.Orientation.Vertical ->
                0 <= x && x < BOARD_WIDTH
                        && 0 <= y && (y + length) <= BOARD_WIDTH;
            case Ship.Orientation.Horizontal ->
                0 <= y && y < BOARD_WIDTH
                        && 0 <= x && (x + length) <= BOARD_WIDTH;
        };
    }

    /**
     * Checks that all ships in the given array are valid.
     *
     * @throws IllegalArgumentException if the ships are not valid.
     */
    private static void isValidShipArrayOrThrow(Ship[] ships) throws IllegalArgumentException {
        if (ships.length != NUM_SHIPS)
            throw new IllegalArgumentException(
                    "Array length did not match `org.shared.Constants.NUM_SHIPS` constant");

        // cpy is just to validate ship placement
        var cpy = new Ship[ships.length];
        for (int i = 0; i < ships.length; i++) {
            var ship = ships[i];
            if (!isWithinBounds(ship)) {
                throw new IllegalArgumentException("Ship is not within bounds of board.");
            } else if (hasOverlap(cpy, ship)) {
                throw new IllegalArgumentException("A ship overlaps with another ship.");
            }
            cpy[i] = ship;
        }
    }

    /**
     * @param ships Must be an array of exactly
     *              {@link org.shared.Constants.NUM_SHIPS}
     *              elements.
     *
     * @throws IllegalArgumentException when the encoded board is invalid.
     */
    public static ShipBoard fromArray(Ship[] ships) throws IllegalArgumentException {
        isValidShipArrayOrThrow(ships);

        var board = new ShipBoard();
        System.arraycopy(ships, 0, board.ships, 0, board.ships.length);
        return board;
    }

    public static ShipBoard fromUserInput(Scanner scanner) {
        while (true) {
            var board = new ShipBoard();
            for (int i = 0; i < NUM_SHIPS; i++) {
                board.printBoard();
                int length = i + 1;
                System.out.printf("Place ship (length: %d):\n", length);

                int posX = promptInt(scanner, "Enter column: ") - 1;
                int posY = promptInt(scanner, "Enter row: ") - 1;

                var o = promptOrientation(scanner);

                if (isWithinBounds(posX, posY, length, o)
                        && !hasOverlap(board.ships, posX, posY, length, o)) {

                    board.ships[i] = new Ship(
                            posX,
                            posY,
                            length,
                            o);
                } else {
                    printError("Ship is not within bounds and/or overlaps with another, placed ship.");
                    i--;
                }
            }
            board.printBoard();

            System.out.println("Continue with this board? (Y/n)");

            var a = scanner.nextLine().trim();
            if (a.equalsIgnoreCase("y") || a.equalsIgnoreCase("yes") || a.isEmpty()) {
                return board;
            }
        }
    }

    public static ShipBoard decode(String encoded) throws IllegalArgumentException {
        var ships = new Ship[NUM_SHIPS];
        var segments = encoded.split(";");

        if (segments.length != ships.length)
            throw new IllegalArgumentException("Received less/more ships than allowed.");

        for (int i = 0; i < segments.length; i++)
            ships[i] = Ship.decode(segments[i]);

        return ShipBoard.fromArray(ships);
    }

    public String encode() {
        var builder = new StringBuilder();

        for (var ship : ships) {
            builder.append(ship.encode() + ";");
        }

        return builder.toString();
    }

    public void printBoard() {
        System.out.println(this.toString());
    }

    @Override
    public String toString() {
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
                Ship s = this.getShipAt(x, y);
                if (s != null) {
                    var cell = s.getCellAt(x, y);
                    boardStr.append(" %c |".formatted(cell));
                } else
                    boardStr.append("   |");
            }
            boardStr.append('\n');
            boardStr.append("  +" + "---+".repeat(BOARD_WIDTH) + '\n');
        }
        return boardStr.toString();
    }

}
