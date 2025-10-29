package org.shared;

public class Ship {
    public enum Orientation {
        Vertical,
        Horizontal;

        public char encode() {
            return switch (this) {
                case Vertical -> 'v';
                case Horizontal -> 'h';
            };
        }
    }

    private final int x;
    private final int y;
    private final int length;
    private final Orientation o;

    /*
     * An array of booleans keeping track
     * of which segment has been hit and which hasn't been
     */
    private final boolean[] segments;

    public Ship(int x, int y, int length, Orientation o) {
        this.x = x;
        this.y = y;
        this.o = o;
        this.length = length;
        this.segments = new boolean[length];
    }

    /*
     * Checks whether the ship was hit. If it was hit
     * updates the segment array accordingly.
     *
     * @return `true` if the ship was hit.
     */
    public boolean registerHit(int x, int y) {
        if (isAt(x, y)) {
            switch (o) {
                case Orientation.Vertical ->
                    segments[y - this.y] = true;
                case Orientation.Horizontal ->
                    segments[x - this.x] = true;
            }
            return true;
        }
        return false;
    }

    public boolean isAt(int x, int y) {
        return switch (o) {
            case Orientation.Vertical ->
                this.x == x
                        && this.y <= y && y < (this.y + length);
            case Orientation.Horizontal ->
                this.y == y
                        && this.x <= x && x < (this.x + length);
        };
    }

    public boolean isAlive() {
        for (int i = 0; i < segments.length; i++) {
            if (!segments[i])
                return true;
        }
        return false;
    }

    public String encode() {
        return "%d,%d,%d,%c".formatted(getX(), getY(), getLength(), getOrientation().encode());
    }

    // INFO: Assumes encoded Ship is valid
    public static Ship decode(String encoded) throws IllegalArgumentException, NumberFormatException {
        encoded = encoded.trim();

        var split = encoded.split(",");
        if (split.length != 4)
            throw new IllegalArgumentException("Invalid ship encoding");

        var x = split[0];
        var y = split[1];
        var l = split[2];
        Ship.Orientation o;
        switch (split[3]) {
            case "h" -> o = Ship.Orientation.Horizontal;
            case "v" -> o = Ship.Orientation.Vertical;
            default ->
                throw new IllegalArgumentException(
                        "Invalid orientation encountered while parsing ships.");
        }

        return new Ship(
                Integer.parseInt(x),
                Integer.parseInt(y),
                Integer.parseInt(l),
                o);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLength() {
        return length;
    }

    public Orientation getOrientation() {
        return o;
    }

    public char getCellAt(int x, int y) {
        return switch (o) {
            case Orientation.Vertical ->
                segments[y - this.y];
            case Orientation.Horizontal ->
                segments[x - this.x];
        } ? 'O' : 'X';
    }

}
