class ShipBoard extends Board {
    public enum Orientation {
        North,
        South,
        East,
        West,
    }

    public ShipBoard(int size) {
        super(size);
    }

    public void addShip(int x, int y, Ship s, Orientation o) {
    }
}
