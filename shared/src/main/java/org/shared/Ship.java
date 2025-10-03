package org.shared;

public class Ship {
    public enum Orientation {
        Vertical,
        Horizontal,
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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLength() {
        return length;
    }

    public Orientation getO() {
        return o;
    }
}
