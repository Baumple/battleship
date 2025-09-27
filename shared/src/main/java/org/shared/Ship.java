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

    public Ship(int x, int y, int length, Orientation o) {
        this.x = x;
        this.y = y;
        this.length = length;
        this.o = o;
    }

    public boolean isHit(int x, int y) {
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
