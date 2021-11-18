package minesweeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Minesweeper board
 */
public class Board {

    // Rep

    private final int width;

    private final int height;

    private final List<List<Square>> squares;

    // Rep invariant
    // Squares is a list of size equal to width. All sub-lists are of size equal to height. All squares are non-null
    // Width and height are non-negative

    // Abstraction function
    // Represents the Minesweeper board, holding all the squares. In a (x,y)-coordinates system, the origin being at the top left corner,
    // the square at the position (x,y) can be found at squares.get(x).get(y). A sub-list can thus be thought as a column.

    // Safety from rep exposure argument
    // All fields are private and final. The list of squares is wrapped in an immutable wrapper class and is not exposed anyway.
    // Squares are not individually exposed.

    // TODO: thread safety argument

    private Board(List<List<Square>> squares, int width, int height) {
        this.squares = Collections.unmodifiableList(squares);
        this.width = width;
        this.height = height;
        checkRep();
    }

    /**
     * Use this for testing purposes
     *
     * @param squares Should contain at least one square. All sub-lists should be of equal size, and contains non-null elements
     */
    public static Board makeTestBoard(List<List<Square>> squares) {
        assert !squares.isEmpty();
        int width = squares.size();
        List<Square> firstColumn = squares.get(0);
        assert !firstColumn.isEmpty();
        int height = firstColumn.size();
        return new Board(squares, width, height);
    }

    private void checkRep() {
        assert this.width >= 0;
        assert this.height >= 0;
        assert this.squares.size() == this.width;
        for (List<Square> column : this.squares) {
            assert column.size() == this.height;
            for (Square square : column) {
                assert square != null;
            }
        }
    }

    /**
     * @return a String representation of the Board that is compliant with a BOARD message defined in the server-to-user grammar
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                Square square = this.squares.get(j).get(i);
                switch (square.getState()) {
                    case UNTOUCHED:
                        stringBuilder.append("-");
                        break;
                    case FLAGGED:
                        stringBuilder.append("F");
                        break;
                    case DUG:
                        Integer neighbouringBombsCount = square.getNeighbouringBombsCount();
                        if (neighbouringBombsCount == 0) {
                            stringBuilder.append(" ");
                        } else {
                            stringBuilder.append(neighbouringBombsCount);
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("Missing enum case in switch statement");
                }
                if (j < this.width - 1) {
                    stringBuilder.append(" ");
                }
            }
            if (i < this.height - 1) {
                stringBuilder.append('\n');
            }
        }
        return stringBuilder.toString();
    }

    /**
     * @param position the position of the square, x must be non-negative and less than the width,
     *                 y must be non-negative and less than the height
     * @return true if there is a bomb at that position, false otherwise
     */
    public boolean positionContainsBomb(Position position) {
        Square square = this.squares.get(position.getX()).get(position.getY());
        return square.containsBomb();
    }

    /**
     * Dig at this position. Will update the bomb counters of the neighbouring squares if needed.
     * If no neighbour square contains a bomb, dig all untouched neighbours and propagate this step recursively.
     *
     * @param position     the position of the square, x must be non-negative and less than the width,
     *                     y must be non-negative and less than the height
     * @param containsBomb true if this square contains a bomb, false otherwise
     */
    public void digAtPosition(Position position, boolean containsBomb) {
        this.digAtPosition(position, containsBomb, false);
    }

    private void digAtPosition(Position position, boolean containsBomb, boolean byPropagation) {
        Square square = this.squares.get(position.getX()).get(position.getY());
        if (square.getState() != SquareStateEnum.UNTOUCHED) {
            // Do nothing if the square was not untouched
            return;
        }

        List<SquareAtPosition> neighboursAndPositions = findNeighboursWithPositions(position);
        List<Square> neighbours = neighboursAndPositions.stream().map(SquareAtPosition::getSquare).collect(Collectors.toList());
        square.dig(neighbours, byPropagation);
        if (containsBomb) {
            for (Square neighbour : neighbours) {
                neighbour.decrementNeighbouringBombCount();
            }
        }
        boolean noBombInNeighbourhood = neighbours.stream().noneMatch(Square::containsBomb);
        if (noBombInNeighbourhood) {
            neighboursAndPositions.forEach(nap -> this.digAtPosition(nap.getPosition(), false, true));
        }
    }

    private List<SquareAtPosition> findNeighboursWithPositions(Position middlePosition) {
        int x = middlePosition.getX();
        int y = middlePosition.getY();
        boolean notOnLeftSide = x != 0;
        boolean notOnRightSide = x != this.width - 1;
        boolean notOnTopSide = y != 0;
        boolean notOnBottomSide = y != this.height - 1;

        List<SquareAtPosition> result = new ArrayList<>(8);
        if (notOnLeftSide) {
            result.add(new SquareAtPosition(this.squares.get(x - 1).get(y), Position.of(x - 1, y)));
        }
        if (notOnRightSide) {
            result.add(new SquareAtPosition(this.squares.get(x + 1).get(y), Position.of(x + 1, y)));
        }
        if (notOnTopSide) {
            result.add(new SquareAtPosition(this.squares.get(x).get(y - 1), Position.of(x, y - 1)));
        }
        if (notOnBottomSide) {
            result.add(new SquareAtPosition(this.squares.get(x).get(y + 1), Position.of(x, y + 1)));
        }
        if (notOnLeftSide && notOnTopSide) {
            result.add(new SquareAtPosition(this.squares.get(x - 1).get(y - 1), Position.of(x - 1, y - 1)));
        }
        if (notOnLeftSide && notOnBottomSide) {
            result.add(new SquareAtPosition(this.squares.get(x - 1).get(y + 1), Position.of(x - 1, y + 1)));
        }
        if (notOnRightSide && notOnTopSide) {
            result.add(new SquareAtPosition(this.squares.get(x + 1).get(y - 1), Position.of(x + 1, y - 1)));
        }
        if (notOnRightSide && notOnBottomSide) {
            result.add(new SquareAtPosition(this.squares.get(x + 1).get(y + 1), Position.of(x + 1, y + 1)));
        }
        return result;
    }

    /**
     * Flag the given position if untouched, otherwise do nothing
     *
     * @param position the position of the square, x must be non-negative and less than the width,
     *                 y must be non-negative and less than the height
     */
    public void flagPosition(Position position) {
        Square square = this.squares.get(position.getX()).get(position.getY());
        square.flag();
    }

    /**
     * Deflag the given position if flagged, otherwise do nothing
     *
     * @param position the position of the square, x must be non-negative and less than the width,
     *                 y must be non-negative and less than the height
     */
    public void deflagPosition(Position position) {
        Square square = this.squares.get(position.getX()).get(position.getY());
        square.deflag();
    }

    /**
     * Utility class to pair square and position, should not be exposed
     */
    private static class SquareAtPosition {

        private final Square square;

        private final Position position;

        public SquareAtPosition(Square square, Position position) {
            this.square = square;
            this.position = position;
        }

        public Square getSquare() {
            return square;
        }

        public Position getPosition() {
            return position;
        }
    }
}
