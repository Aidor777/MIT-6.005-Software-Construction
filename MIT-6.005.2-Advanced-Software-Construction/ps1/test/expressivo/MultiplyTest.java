package expressivo;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for the Multiply abstract data type.
 */
public class MultiplyTest {

    // Testing strategy
    // Assuming Number, Variable and Add work as intended
    // Will test the constructor with good and bad input, with the help of toString
    // Check also the correct building and printing of recursive operations
    // Check correct grouping of non-terminals when using toString
    // Equals can test a bit more: reversed operations, recursive builds, we assume it works well for Variable, Number and Add
    // Differentiate has to test: both sides zero, left side zero, right side zero, simplification of ones, both sides non-zero
    // Simplify will need to be tested with (with numbers added up) and without simplification

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testConstructor_nominal() {
        Number number1 = new Number(1.23);
        Number number2 = new Number(52.35);
        Multiply multiply1 = new Multiply(number1, number2);
        assertEquals("1.23 * 52.35", multiply1.toString());

        Variable variable1 = new Variable("x");
        Variable variable2 = new Variable("yz");
        Multiply multiply2 = new Multiply(variable1, variable2);
        assertEquals("x * yz", multiply2.toString());

        Multiply multiply3 = new Multiply(number2, variable2);
        assertEquals("52.35 * yz", multiply3.toString());

        Add add1 = new Add(number1, number2);
        Multiply multiply4 = new Multiply(add1, variable2);
        assertEquals("(1.23 + 52.35) * yz", multiply4.toString());

        Add add2 = new Add(number1, multiply3);
        assertEquals("1.23 + 52.35 * yz", add2.toString());
    }

    @Test(expected = AssertionError.class)
    public void testConstructor_failureNullInput() {
        Multiply multiply = new Multiply(null, null);
    }

    @Test
    public void testEquals_nominal() {
        Multiply multiply1 = new Multiply(new Add(new Variable("sd"), new Number(12.21)), new Number(45.67));
        Multiply multiply2 = new Multiply(new Add(new Variable("sd"), new Number(12.22)), new Number(45.67));
        Multiply multiply3 = new Multiply(new Add(new Variable("sd"), new Number(12.21)), new Number(45.76));
        assertFalse(multiply1.equals(multiply2) || multiply2.equals(multiply1)
                || multiply1.equals(multiply3) || multiply3.equals(multiply1));
        Multiply multiply4 = new Multiply(new Add(new Variable("sd"), new Number(12.21)), new Number(45.67));
        assertTrue(multiply1.equals(multiply4) && multiply4.equals(multiply1) && multiply4.equals(multiply4));
    }

    @Test
    public void testEquals_recursiveAndReversed() {
        Multiply multiply1 = new Multiply(new Add(new Variable("sd"), new Number(12.21)), new Number(45.67));
        Multiply multiply2 = new Multiply(new Variable("ki"), new Add(new Variable("qi"), new Number(33.44)));
        Multiply multiply3 = new Multiply(new Add(new Variable("sd"), new Number(12.21)), new Number(45.67));
        Multiply multiply4 = new Multiply(new Variable("ki"), new Add(new Variable("qi"), new Number(33.44)));
        Multiply multiply5 = new Multiply(multiply1, multiply2);
        Multiply multiply6 = new Multiply(multiply3, multiply4);
        Multiply multiply7 = new Multiply(multiply2, multiply1);
        assertTrue(multiply5.equals(multiply6) && multiply6.equals(multiply5));
        assertFalse(multiply5.equals(multiply7) || multiply7.equals(multiply5));
        Multiply multiply8 = new Multiply(multiply1, multiply3);
        assertFalse(multiply5.equals(multiply8) || multiply8.equals(multiply5));
    }

    @Test
    public void testDifferentiate_fullZero() {
        Multiply multiply = new Multiply(new Number(2.4), new Variable("x"));
        Expression expectedResult = new Number(0);
        assertEquals(expectedResult, multiply.differentiate("y"));
    }

    @Test
    public void testDifferentiate_LHSZero() {
        Multiply multiply = new Multiply(new Number(2.4), new Variable("x"));
        Expression expectedResult = new Number(2.4);
        assertEquals(expectedResult, multiply.differentiate("x"));
    }

    @Test
    public void testDifferentiate_RHSZero() {
        Multiply multiply = new Multiply(new Variable("yz"), new Variable("x"));
        Expression expectedResult = new Variable("x");
        assertEquals(expectedResult, multiply.differentiate("yz"));
    }

    @Test
    public void testDifferentiate_simplifyOnes() {
        Multiply multiply = new Multiply(
                new Number(1),
                new Add(new Variable("yz"),
                        new Variable("yz")));
        Expression expectedResult = new Add(new Number(1), new Number(1));
        assertEquals(expectedResult, multiply.differentiate("yz"));
    }

    @Test
    public void testDifferentiate_complex1() {
        Multiply multiply = new Multiply(
                new Add(new Number(2),
                        new Variable("x")),
                new Add(new Variable("x"),
                        new Variable("yz")));
        Expression expectedResult = new Add(
                new Add(new Number(2),
                        new Variable("x")),
                new Add(new Variable("x"),
                        new Variable("yz")));
        assertEquals(expectedResult, multiply.differentiate("x"));
    }

    @Test
    public void testDifferentiate_complex2() {
        Multiply multiply = new Multiply(
                new Multiply(new Number(2),
                        new Variable("x")),
                new Multiply(new Variable("x"),
                        new Variable("yz")));
        Expression expectedResult = new Add(
                new Multiply(
                        new Multiply(
                                new Number(2),
                                new Variable("x")),
                        new Variable("yz")),
                new Multiply(
                        new Multiply(
                                new Variable("x"),
                                new Variable("yz")),
                        new Number(2)));
        assertEquals(expectedResult, multiply.differentiate("x"));
    }

    @Test
    public void testSimplify_noSimplification() {
        Multiply multiply = new Multiply(new Number(3.4), new Variable("x"));
        Expression expectedResult = new Multiply(new Number(3.4), new Variable("x"));
        Map<String, Double> environment = new HashMap<String, Double>() {{
            put("xy", 3.0);
            put("yz", 1.7);
        }};
        assertEquals(expectedResult, multiply.simplify(environment));
    }

    @Test
    public void testSimplify_variableSimplification() {
        Multiply multiply = new Multiply(
                new Variable("xy"),
                new Add(new Variable("zx"),
                        new Variable("yz")));
        Expression expectedResult = new Multiply(
                new Number(3),
                new Add(new Variable("zx"),
                        new Number(1.7)));
        Map<String, Double> environment = new HashMap<String, Double>() {{
            put("xy", 3.0);
            put("ZX", 123.45);
            put("yz", 1.7);
        }};
        assertEquals(expectedResult, multiply.simplify(environment));
    }

    @Test
    public void testSimplify_numberSimplification() {
        Multiply multiply = new Multiply(
                new Variable("xy"),
                new Add(new Variable("zx"),
                        new Variable("yz")));
        Expression expectedResult = new Number(34.71);
        Map<String, Double> environment = new HashMap<String, Double>() {{
            put("xy", 3.0);
            put("ZX", 123.45);
            put("yz", 1.7);
            put("zx", 9.87);
        }};
        assertEquals(expectedResult, multiply.simplify(environment));
    }

}
