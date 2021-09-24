package expressivo;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for the Number abstract data type.
 */
public class NumberTest {

    // Testing strategy
    // Will test the constructor with good and bad input, with the help of toString
    // Will test the correct removal of trailing zeroes too, and the reduction to 4 decimals  for toString
    // Equals will have to test perfect equality, and close enough equality in both ways
    // Differentiate should always return Number(n=0), whatever the input
    // Simplify should always return the same value, whatever the environment

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testConstructor_nominal() {
        Number number1 = new Number(0.0);
        assertEquals("0", number1.toString());
        Number number2 = new Number(52.35);
        assertEquals("52.35", number2.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_badInput() {
        Number number = new Number(-4.67);
    }

    @Test
    public void testToString_removeTrailingZeroes() {
        Number number = new Number(26.80000);
        assertEquals("26.8", number.toString());
    }

    @Test
    public void testToString_removeTrailingZeroesInteger() {
        Number number = new Number(889.000);
        assertEquals("889", number.toString());
    }

    @Test
    public void testToString_fourDecimalsMax() {
        Number number = new Number(26.8234896);
        assertEquals("26.8235", number.toString());
    }

    @Test
    public void testEquals_nominal() {
        Number number1 = new Number(95.35);
        Number number2 = new Number(333.22);
        assertFalse(number1.equals(number2) || number2.equals(number1));
        Number number3 = new Number(95.35);
        assertTrue(number1.equals(number3) && number3.equals(number1) && number3.equals(number3));
    }

    @Test
    public void testEquals_closeEnough() {
        Number number1 = new Number(12.345678);
        Number number2 = new Number(12.34567);
        assertTrue(number1.equals(number2) && number2.equals(number1));
        Number number3 = new Number(12.3456);
        assertFalse(number1.equals(number3) || number3.equals(number1));
    }

    @Test
    public void testDifferentiate_nominal() {
        Number number1 = new Number(34);
        Number number2 = new Number(1.2);
        Expression expectedResult = new Number(0);
        assertEquals(expectedResult, number1.differentiate("x"));
        assertEquals(expectedResult, number2.differentiate("yZ"));
    }

    @Test
    public void testSimplify_emptyEnvironment() {
        Number number = new Number(5.67);
        Expression expectedResult = new Number(5.67);
        Map<String, Double> environment = new HashMap<>();
        assertEquals(expectedResult, number.simplify(environment));
    }

    @Test
    public void testSimplify_nonEmptyEnvironment() {
        Number number = new Number(12);
        Expression expectedResult = new Number(12);
        Map<String, Double> environment = new HashMap<String, Double>() {{
            put("x", 3.0);
            put("y", 1.7);
        }};
        assertEquals(expectedResult, number.simplify(environment));
    }

}
