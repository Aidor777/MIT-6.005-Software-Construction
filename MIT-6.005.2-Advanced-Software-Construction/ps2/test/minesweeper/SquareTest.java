package minesweeper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test suite for the Square data structure
 */
public class SquareTest {

    // We can test our static factory with both possible inputs.
    // Trivial methods such as accessors won't be tested by themselves, and debugging methods like toString neither.
    // As this data type is mutable, equals should only return true for references to the same object in memory.
    // The dig method will have the most tests, for each possible error cases, by propagation or not, with a bomb or not,
    // with neighbours holding bombs or not.
    // The decrementNeighbouringBombCount only need a nominal case.
    // The flag and deflag can be tested with a change and a no change input.

    private List<Square> neighbours;

    @Before
    public void init() {
        this.neighbours = Arrays.asList(Square.makeSquare(false), Square.makeSquare(true), Square.makeSquare(false),
                Square.makeSquare(true), Square.makeSquare(false), Square.makeSquare(true), Square.makeSquare(false),
                Square.makeSquare(true));
    }

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testMakeSquare_withBomb() {
        Square square = Square.makeSquare(true);
        Assert.assertTrue(square.containsBomb());
        Assert.assertEquals(SquareStateEnum.UNTOUCHED, square.getState());
    }

    @Test
    public void testMakeSquare_withoutBomb() {
        Square square = Square.makeSquare(false);
        Assert.assertFalse(square.containsBomb());
        Assert.assertEquals(SquareStateEnum.UNTOUCHED, square.getState());
    }

    @Test
    public void testEquals() {
        Square square1 = Square.makeSquare(false);
        Square square2 = Square.makeSquare(false);
        Square square3 = square1;
        Assert.assertNotEquals(square1, square2);
        Assert.assertEquals(square1, square3);
    }

    @Test
    public void testDig_noBombNotByPropagationWithNeighbouringBombs() {
        Square square = Square.makeSquare(false);
        square.dig(this.neighbours, false);
        Assert.assertEquals(SquareStateEnum.DUG, square.getState());
        Assert.assertNotNull(square.getNeighbouringBombsCount());
        Assert.assertEquals(4, square.getNeighbouringBombsCount().intValue());
    }

    @Test
    public void testDig_withBombNotByPropagationWithNeighbouringBombs() {
        Square square = Square.makeSquare(true);
        square.dig(this.neighbours, false);
        Assert.assertEquals(SquareStateEnum.DUG, square.getState());
        Assert.assertFalse(square.containsBomb());
        Assert.assertNotNull(square.getNeighbouringBombsCount());
        Assert.assertEquals(4, square.getNeighbouringBombsCount().intValue());
    }

    @Test
    public void testDig_noBombNotByPropagationNoNeighbouringBombs() {
        Square square = Square.makeSquare(false);
        List<Square> bomblessNeighbours = this.neighbours.stream().filter(sq -> !sq.containsBomb()).collect(Collectors.toList());
        square.dig(bomblessNeighbours, false);
        Assert.assertEquals(SquareStateEnum.DUG, square.getState());
        Assert.assertNotNull(square.getNeighbouringBombsCount());
        Assert.assertEquals(0, square.getNeighbouringBombsCount().intValue());
    }

    @Test
    public void testDig_noBombByPropagationWithNeighbouringBombs() {
        Square square = Square.makeSquare(false);
        square.dig(this.neighbours, true);
        Assert.assertEquals(SquareStateEnum.DUG, square.getState());
        Assert.assertNotNull(square.getNeighbouringBombsCount());
        Assert.assertEquals(4, square.getNeighbouringBombsCount().intValue());
    }

    @Test(expected = AssertionError.class)
    public void testDig_errorTooManyNeighbours() {
        Square square = Square.makeSquare(false);
        List<Square> numerousNeighbours = new ArrayList<>(this.neighbours);
        numerousNeighbours.add(Square.makeSquare(false));
        square.dig(numerousNeighbours, false);
    }

    @Test(expected = AssertionError.class)
    public void testDig_errorPropagationWithBomb() {
        Square square = Square.makeSquare(true);
        square.dig(this.neighbours, true);
    }

    @Test
    public void testDecrementNeighbouringBombs() {
        Square square = Square.makeSquare(false);
        square.decrementNeighbouringBombCount();
        square.dig(this.neighbours, false);
        square.decrementNeighbouringBombCount();
        Assert.assertEquals(3, square.getNeighbouringBombsCount().intValue());
        square.decrementNeighbouringBombCount();
        Assert.assertEquals(2, square.getNeighbouringBombsCount().intValue());
        square.decrementNeighbouringBombCount();
        square.decrementNeighbouringBombCount();
        square.decrementNeighbouringBombCount();
        Assert.assertEquals(0, square.getNeighbouringBombsCount().intValue());
    }

    @Test
    public void testFlag() {
        Square square = Square.makeSquare(false);
        square.flag();
        Assert.assertEquals(SquareStateEnum.FLAGGED, square.getState());
        square.flag();
        Assert.assertEquals(SquareStateEnum.FLAGGED, square.getState());
    }

    @Test
    public void testDeflag() {
        Square square = Square.makeSquare(false);
        square.flag();
        square.deflag();
        Assert.assertEquals(SquareStateEnum.UNTOUCHED, square.getState());
        square.dig(this.neighbours, false);
        square.deflag();
        Assert.assertEquals(SquareStateEnum.DUG, square.getState());
    }

}
