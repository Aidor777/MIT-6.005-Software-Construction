package minesweeper;

import java.util.List;

/**
 * Minesweeper board square (mutable)
 */
public class Square {

    // Rep

    private SquareStateEnum state;

    private boolean containsBomb;

    private Integer neighbouringBombsCount;

    // Rep invariant
    // State is an enum holding only possible states, neighbouring bombs count cannot be lower than 0 and more than 8

    // Abstraction function
    // Represents a square on a Minesweeper board, holding its current state among possible ones, if it contains a bomb or not,
    // and if dug, the number of its direct neighbours holding a bomb

    // Safety from rep exposure argument
    // All fields are private and use immutable data types. No direct mutators are provided, only carefully chosen ones

    // Thread safety argument
    // This class need not be thread-safe

    private Square(SquareStateEnum squareState, boolean containsBomb, Integer neighbouringBombsCount) {
        this.state = squareState;
        this.containsBomb = containsBomb;
        this.neighbouringBombsCount = neighbouringBombsCount;
        checkRep();
    }

    private void checkRep() {
        assert this.state != null;
        switch (this.state) {
            case DUG:
                assert this.neighbouringBombsCount != null;
                int minNeighbouringBombsCount = 0;
                int maxNeighbouringBombsCount = 8;
                assert this.neighbouringBombsCount >= minNeighbouringBombsCount;
                assert this.neighbouringBombsCount <= maxNeighbouringBombsCount;
                assert !this.containsBomb;
                break;
            case FLAGGED:
            case UNTOUCHED:
                assert this.neighbouringBombsCount == null;
                break;
            default:
                throw new UnsupportedOperationException("Missing enum case in switch statement");
        }
    }

    /**
     * Initiate a new square on the board, with untouched state
     *
     * @param containsBomb if this square will contain a bomb at creation time
     * @return the square in an untouched state
     */
    public static Square makeSquare(boolean containsBomb) {
        return new Square(SquareStateEnum.UNTOUCHED, containsBomb, null);
    }

    /**
     * @return the current state of the square
     */
    public SquareStateEnum getState() {
        return state;
    }

    /**
     * @return true if the square currently holds a bomb, false otherwise
     */
    public boolean containsBomb() {
        return containsBomb;
    }

    /**
     * @return the number of bombs held by direct neighbouring squares
     * @throws AssertionError if the square is not dug
     */
    public Integer getNeighbouringBombsCount() {
        assert this.state == SquareStateEnum.DUG;
        return neighbouringBombsCount;
    }

    /**
     * @return a string representation of this square (for debugging purposes only)
     */
    @Override
    public String toString() {
        return "Square{state=" + state + ", containsBomb=" + containsBomb + ", neighbouringBombsCount=" + neighbouringBombsCount + '}';
    }

    /**
     * Dig a hole on an untouched square, marking it as dug. If the square contained a bomb, it will not anymore,
     * and it is up to the caller to react appropriately before calling this method.
     * Also, the number of bombs in the neighbourhood of this square alone will be calculated
     *
     * @param directNeighbours direct neighbours of this square (will not be mutated)
     * @param byPropagation    indicate if this square is being dug by propagation, false if it's a direct dig
     * @throws AssertionError if the square is not untouched, if the number of direct neighbours is not
     *                        between 0 and 8 (thus incoherent), and if it contains a bomb on propagation mode
     */
    public void dig(List<Square> directNeighbours, boolean byPropagation) {
        assert this.state == SquareStateEnum.UNTOUCHED;
        int maxPossibleNeighboursCount = 8;
        assert directNeighbours.size() <= maxPossibleNeighboursCount;

        if (byPropagation) {
            assert !this.containsBomb;
        } else {
            this.containsBomb = false;
        }

        this.state = SquareStateEnum.DUG;
        this.neighbouringBombsCount = Math.toIntExact(directNeighbours.stream().filter(Square::containsBomb).count());
        checkRep();
    }

    /**
     * To be used after a bomb exploded in the neighbourhood to reflect that change.
     * Does not mutate anything if the square is not DUG or has 0 bombs in its neighbourhood.
     */
    public void decrementNeighbouringBombCount() {
        if (this.neighbouringBombsCount != null && this.neighbouringBombsCount > 0) {
            this.neighbouringBombsCount--;
        }
        checkRep();
    }

    /**
     * Change state to flagged, if and only if it was untouched
     */
    public void flag() {
        if (this.state == SquareStateEnum.UNTOUCHED) {
            this.state = SquareStateEnum.FLAGGED;
        }
        checkRep();
    }

    /**
     * Change state to unflagged, if and only if it was flagged
     */
    public void deflag() {
        if (this.state == SquareStateEnum.FLAGGED) {
            this.state = SquareStateEnum.UNTOUCHED;
        }
        checkRep();
    }
}
