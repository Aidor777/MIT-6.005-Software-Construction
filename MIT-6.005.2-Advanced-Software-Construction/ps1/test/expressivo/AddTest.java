package expressivo;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for the Add abstract data type.
 */
public class AddTest {

    // Testing strategy
    // Assuming Number and Variable work as intended
    // Will test the constructor with good and bad input, with the help of toString
    // Check also the correct building and printing of recursive operations
    // Equals can test a bit more: reversed operations, recursive builds, we assume it works well for Variable and Number
    // Differentiate has to test: full zero, LSH zero, RHS zero and both LHS and RHS non-zero results
    // Simplify will need to be tested with (with numbers added up) and without simplification

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testConstructor_nominal() {
        Number number1 = new Number(1.23);
        Number number2 = new Number(52.35);
        Add add1 = new Add(number1, number2);
        assertEquals("1.23 + 52.35", add1.toString());

        Variable variable1 = new Variable("x");
        Variable variable2 = new Variable("yz");
        Add add2 = new Add(variable1, variable2);
        assertEquals("x + yz", add2.toString());

        Add add3 = new Add(number1, variable2);
        assertEquals("1.23 + yz", add3.toString());

        Add add4 = new Add(add2, add1);
        assertEquals("x + yz + 1.23 + 52.35", add4.toString());
    }

    @Test(expected = AssertionError.class)
    public void testConstructor_failureNullInput() {
        Add add = new Add(null, null);
    }

    @Test
    public void testEquals_nominal() {
        Add add1 = new Add(new Number(3), new Variable("yz"));
        Add add2 = new Add(new Number(3), new Variable("xz"));
        Add add3 = new Add(new Number(4), new Variable("yz"));
        assertFalse(add1.equals(add2) || add2.equals(add1) || add1.equals(add3) || add3.equals(add1));
        Add add4 = new Add(new Number(3), new Variable("yz"));
        assertTrue(add1.equals(add4) && add4.equals(add1) && add4.equals(add4));
    }

    @Test
    public void testEquals_recursiveAndReversed() {
        Add add1 = new Add(new Number(3), new Variable("yz"));
        Add add2 = new Add(new Variable("ui"), new Number(2.95));
        Add add3 = new Add(new Number(3), new Variable("yz"));
        Add add4 = new Add(new Variable("ui"), new Number(2.95));
        Add add5 = new Add(add1, add2);
        Add add6 = new Add(add3, add4);
        Add add7 = new Add(add2, add1);
        assertTrue(add5.equals(add6) && add6.equals(add5));
        assertFalse(add7.equals(add5) || add5.equals(add7));
        Add add8 = new Add(add1, add3);
        assertFalse(add8.equals(add5) || add5.equals(add8));
    }

    @Test
    public void testDifferentiate_fullZero() {
        Add add = new Add(new Number(2.4), new Variable("x"));
        Expression expectedResult = new Number(0);
        assertEquals(expectedResult, add.differentiate("y"));
    }

    @Test
    public void testDifferentiate_LHSZero() {
        Add add = new Add(new Number(2.4), new Variable("x"));
        Expression expectedResult = new Number(1);
        assertEquals(expectedResult, add.differentiate("x"));
    }

    @Test
    public void testDifferentiate_RHSZero() {
        Add add = new Add(new Variable("yz"), new Variable("x"));
        Expression expectedResult = new Number(1);
        assertEquals(expectedResult, add.differentiate("yz"));
    }

    @Test
    public void testDifferentiate_nonZeroes() {
        Add add = new Add(new Add(new Variable("yz"), new Number(2.4)), new Variable("yz"));
        Expression expectedResult = new Add(new Number(1), new Number(1));
        assertEquals(expectedResult, add.differentiate("yz"));
    }

    @Test
    public void testSimplify_noSimplification() {
        Add add = new Add(new Number(3.4), new Variable("x"));
        Expression expectedResult = new Add(new Number(3.4), new Variable("x"));
        Map<String, Double> environment = new HashMap<String, Double>() {{
            put("xy", 3.0);
            put("yz", 1.7);
        }};
        assertEquals(expectedResult, add.simplify(environment));
    }

    @Test
    public void testSimplify_variableSimplification() {
        Add add = new Add(
                new Variable("xy"),
                new Add(new Variable("zx"),
                        new Variable("yz")));
        Expression expectedResult = new Add(
                new Number(3),
                new Add(new Variable("zx"),
                        new Number(1.7)));
        Map<String, Double> environment = new HashMap<String, Double>() {{
            put("xy", 3.0);
            put("ZX", 123.45);
            put("yz", 1.7);
        }};
        assertEquals(expectedResult, add.simplify(environment));
    }

    @Test
    public void testSimplify_numberSimplification() {
        Add add = new Add(
                new Variable("xy"),
                new Add(new Variable("zx"),
                        new Variable("yz")));
        Expression expectedResult = new Number(14.57);
        Map<String, Double> environment = new HashMap<String, Double>() {{
            put("xy", 3.0);
            put("ZX", 123.45);
            put("yz", 1.7);
            put("zx", 9.87);
        }};
        assertEquals(expectedResult, add.simplify(environment));
    }

}
