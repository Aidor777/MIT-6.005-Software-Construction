package minesweeper;

/**
 * Position on a Minesweeper board
 */
public class Position {

    // Rep

    private final int x;

    private final int y;

    // Rep invariant
    // Both x and y are zero or positive

    // Abstraction function
    // Represents a position on a Minesweeper board in a (x,y)-coordinates system

    // Safety from rep exposure argument
    // All fields are private, final and immutable

    // Thread safety argument
    // This type is immutable

    private Position(int x, int y) {
        this.x = x;
        this.y = y;
        checkRep();
    }

    private void checkRep() {
        assert this.x >= 0;
        assert this.y >= 0;
    }

    /**
     * @param x the x-coordinate, x >= 0
     * @param y the y-coordinate, y >= 0
     */
    public static Position of(int x, int y) {
        return new Position(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
