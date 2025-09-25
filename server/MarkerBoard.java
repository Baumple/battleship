public class MarkerBoard {
    public enum Marker {
        Empty,
        Ship,
        Missed,
        Hit,
    }

    private final int size;
    private final Marker[] board;

    public MarkerBoard(int size) {
        this.size = size;
        this.board = new Marker[size * size];
        for (int i = 0; i < size * size; i++) {
            board[i] = Marker.Empty;
        }
    }

    /**
     * Attempts to place a marker within the board.
     *
     * If the given coordinates are not within the board's
     * dimensions, returns false.
     *
     * @param m The {@link Marker} to place
     * @param x The x coordinate
     * @param y The y coordinate
     * @return a boolean indicating whether placing the marker was successful or not
     */
    public boolean placeMarker(Marker m, int x, int y) {
        if (x >= size || y >= size)
            return false;
        this.board[x + size * y] = m;
        return true;
    }
}
