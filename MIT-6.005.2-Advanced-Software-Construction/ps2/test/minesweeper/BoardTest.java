/* Copyright (c) 2007-2017 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

/**
 * Test suite for the Board data structure
 */
public class BoardTest {

    // Testing strategy
    // Build progressively more complex boards, and play with them...
    // The strategy is better commented inside the tests themselves

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testBoardSingleSquare_noBomb() {
        Board board = Board.makeTestBoard(Arrays.asList(Arrays.asList(Square.makeSquare(false))));
        Assert.assertEquals("-", board.toString());

        Position position = Position.of(0, 0);
        Assert.assertFalse(board.positionContainsBomb(position));

        board.flagPosition(position);
        Assert.assertEquals("F", board.toString());

        // Digging at a flagged position does nothing
        board.digAtPosition(position, false);
        Assert.assertEquals("F", board.toString());

        board.deflagPosition(position);
        Assert.assertEquals("-", board.toString());

        board.digAtPosition(position, false);
        Assert.assertEquals(" ", board.toString());

        // Flagging a dug position does nothing
        board.flagPosition(position);
        Assert.assertEquals(" ", board.toString());

        // Deflagging a dug position does nothing
        board.deflagPosition(position);
        Assert.assertEquals(" ", board.toString());

        // Digging a dug position does nothing
        board.digAtPosition(position, false);
        Assert.assertEquals(" ", board.toString());
    }

    @Test
    public void testBoardSingleSquare_withBomb() {
        Board board = Board.makeTestBoard(Arrays.asList(Arrays.asList(Square.makeSquare(true))));
        Assert.assertEquals("-", board.toString());

        Position position = Position.of(0, 0);
        Assert.assertTrue(board.positionContainsBomb(position));

        // Digging on a bombed position removes the bomb
        board.digAtPosition(position, true);
        Assert.assertEquals(" ", board.toString());
        Assert.assertFalse(board.positionContainsBomb(position));
    }

    @Test
    public void testBoardFourSquares() {
        Board board = Board.makeTestBoard(Arrays.asList(
                Arrays.asList(Square.makeSquare(true), Square.makeSquare(false)),
                Arrays.asList(Square.makeSquare(false), Square.makeSquare(true))));
        Assert.assertEquals("- -\n- -", board.toString());

        board.flagPosition(Position.of(1, 1));
        Assert.assertEquals("- -\n- F", board.toString());

        board.digAtPosition(Position.of(1, 0), false);
        Assert.assertEquals("- 2\n- F", board.toString());

        board.digAtPosition(Position.of(0, 1), false);
        Assert.assertEquals("- 2\n2 F", board.toString());

        // The bomb counters should reflect the fact a bomb has exploded
        board.digAtPosition(Position.of(0, 0), true);
        Assert.assertEquals("1 1\n1 F", board.toString());
    }

    @Test
    public void testBoardNineSquares_propagation() {
        Board board = Board.makeTestBoard(Arrays.asList(
                Arrays.asList(Square.makeSquare(false), Square.makeSquare(false), Square.makeSquare(false)),
                Arrays.asList(Square.makeSquare(false), Square.makeSquare(false), Square.makeSquare(false)),
                Arrays.asList(Square.makeSquare(false), Square.makeSquare(false), Square.makeSquare(true))));
        Assert.assertEquals("- - -\n- - -\n- - -", board.toString());

        // No propagation as there is a bomb close by
        board.digAtPosition(Position.of(1, 1), false);
        Assert.assertEquals("- - -\n- 1 -\n- - -", board.toString());

        // Can propagate
        board.digAtPosition(Position.of(0, 0), false);
        Assert.assertEquals("     \n  1 1\n  1 -", board.toString());
    }

    @Test
    public void testBigBoard() {
        Board board = Board.makeTestBoard(Arrays.asList(
                Arrays.asList(Square.makeSquare(false), Square.makeSquare(false), Square.makeSquare(false), Square.makeSquare(false)),
                Arrays.asList(Square.makeSquare(false), Square.makeSquare(false), Square.makeSquare(true), Square.makeSquare(false)),
                Arrays.asList(Square.makeSquare(false), Square.makeSquare(false), Square.makeSquare(false), Square.makeSquare(false)),
                Arrays.asList(Square.makeSquare(true), Square.makeSquare(true), Square.makeSquare(false), Square.makeSquare(false)),
                Arrays.asList(Square.makeSquare(false), Square.makeSquare(true), Square.makeSquare(false), Square.makeSquare(false))));
        Assert.assertEquals("- - - - -\n- - - - -\n- - - - -\n- - - - -", board.toString());

        board.digAtPosition(Position.of(4, 0), false);
        Assert.assertEquals("- - - - 3\n- - - - -\n- - - - -\n- - - - -", board.toString());

        board.flagPosition(Position.of(4, 1));
        board.flagPosition(Position.of(3, 0));
        board.flagPosition(Position.of(3, 1));
        board.digAtPosition(Position.of(2, 2), false);
        Assert.assertEquals("- - - F 3\n- - - F F\n- - 2 - -\n- - - - -", board.toString());

        board.digAtPosition(Position.of(0, 0), false);
        Assert.assertEquals("    2 F 3\n1 1 3 F F\n- - 2 - -\n- - - - -", board.toString());

        board.flagPosition(Position.of(1, 2));
        board.deflagPosition(Position.of(3, 1));
        board.digAtPosition(Position.of(3, 1), true);
        Assert.assertEquals("    1 F 2\n1 1 2 2 F\n- F 1 - -\n- - - - -", board.toString());
    }

    @Test(expected = AssertionError.class)
    public void testInvalidBoard() {
        Board.makeTestBoard(Arrays.asList(
                Arrays.asList(Square.makeSquare(false), Square.makeSquare(false)),
                Arrays.asList(Square.makeSquare(false), Square.makeSquare(false)),
                Arrays.asList(Square.makeSquare(false))));
    }

    @Test
    public void testFromDimensions_ok() {
        Board board = Board.fromDimensions(3, 5);
        Assert.assertNotNull("Board was created", board);
        Assert.assertEquals("Board content", "- - -\n- - -\n- - -\n- - -\n- - -", board.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromDimensions_ko() {
        Board.fromDimensions(-34, 56);
    }

    @Test
    public void testFromFile_ok() throws Exception {
        URI uri = Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource("minesweeper/boards/valid-board.txt")).toURI();
        Board board = Board.fromFile(Paths.get(uri).toFile());
        // Test that the board was created, has the correct string representations, and try out some squares
        Assert.assertNotNull("Board was created", board);
        Assert.assertEquals("Board content", "- - - - - -\n- - - - - -\n- - - - - -\n- - - - - -",
                board.toString());
        Assert.assertTrue(board.positionContainsBomb(Position.of(5, 3)));
        Assert.assertTrue(board.positionContainsBomb(Position.of(2, 2)));
        Assert.assertFalse(board.positionContainsBomb(Position.of(0, 0)));
        Assert.assertFalse(board.positionContainsBomb(Position.of(2, 3)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromFile_ko_dimensions() throws Exception {
        URI uri = Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource("minesweeper/boards/invalid-dimensions.txt")).toURI();
        Board.fromFile(Paths.get(uri).toFile());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromFile_ko_header() throws Exception {
        URI uri = Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource("minesweeper/boards/invalid-header.txt")).toURI();
        Board.fromFile(Paths.get(uri).toFile());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromFile_ko_line() throws Exception {
        URI uri = Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource("minesweeper/boards/invalid-line.txt")).toURI();
        Board.fromFile(Paths.get(uri).toFile());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromFile_ko_mismatchColumns() throws Exception {
        URI uri = Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource("minesweeper/boards/invalid-mismatch-columns.txt")).toURI();
        Board.fromFile(Paths.get(uri).toFile());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromFile_ko_mismatchLines() throws Exception {
        URI uri = Objects.requireNonNull(Thread.currentThread().getContextClassLoader()
                .getResource("minesweeper/boards/invalid-mismatch-lines.txt")).toURI();
        Board.fromFile(Paths.get(uri).toFile());
    }
    
}
