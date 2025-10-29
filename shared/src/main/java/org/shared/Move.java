package org.shared;

import static org.shared.Constants.BOARD_WIDTH;

// MOVE x y
public record Move(int x, int y) {
    public static Move parse(String line) throws IllegalArgumentException {
        var split = line.trim().split(" ");
        int x, y;
        try {
            x = Integer.parseInt(split[1]);
            y = Integer.parseInt(split[2]);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        if (!(0 <= x && x < BOARD_WIDTH)) {
            throw new IllegalArgumentException("x is out of bounds");
        }
        if (!(0 <= x && x < BOARD_WIDTH)) {
            throw new IllegalArgumentException("y is out of bounds");
        }
        return new Move(x, y);
    }

    public String encode() {
        return "MOVE %d %d".formatted(x, y);
    }
}
