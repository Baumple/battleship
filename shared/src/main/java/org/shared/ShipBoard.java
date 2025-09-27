package org.shared;

public class ShipBoard {
    public static final int DEFAULT_BOARD_SIZE = 8;

    enum CellMarker {
        Empty,
        Miss,
        Hit,
    }

    private final int size;
    private final Ship[] ships;

    public ShipBoard(int size, Ship[] ships) {
        this.size = size;
        this.ships = ships;
    }

    private boolean isShipAt(int x, int y) {
        for (var ship : ships) {
            if (ship.isHit(x, y))
                return true;
        }
        return false;
    }

    public void printBoard() {
        char[][] board = new char[size][size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (isShipAt(x, y)) {
                    board[x][y] = 'X';
                } else {
                    board[x][y] = ' ';
                }
            }
        }

        System.out.println("+" + "-".repeat(4 * size - 1) + "+");
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                System.out.print("| %c ".formatted(board[x][y]));
            }
            System.out.println("|");
            System.out.println("+" + "-".repeat(4 * size - 1) + "+");
        }
    }
}
