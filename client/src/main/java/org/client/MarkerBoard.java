package org.client;

import static org.shared.Constants.BOARD_WIDTH;

public class MarkerBoard {
    public enum Marker {
        None,
        Miss,
        Hit,
    }

    private final Marker[][] board;

    public MarkerBoard() {
        this.board = new Marker[BOARD_WIDTH][BOARD_WIDTH];
        for (int x = 0; x < BOARD_WIDTH; x++) {
            for (int y = 0; y < BOARD_WIDTH; y++) {
                board[x][y] = Marker.None;
            }
        }
    }

    public void placeMarker(int x, int y, Marker marker) {
        this.board[x][y] = marker;
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
                switch (this.board[x][y]) {
                    case Marker.None ->
                        boardStr.append("   |");
                    case Marker.Miss ->
                        boardStr.append(" O |");
                    case Marker.Hit ->
                        boardStr.append(" X |");
                }
            }
            boardStr.append('\n');
            boardStr.append("  +" + "---+".repeat(BOARD_WIDTH) + '\n');
        }
        return boardStr.toString();
    }
}
