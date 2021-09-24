package expressivo;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for the Variable abstract data type.
 */
public class VariableTest {

    // Testing strategy
    // Will test the constructor with good and bad input (empty string and non-letter characters), with the help of toString
    // Equals will only need a true and a false case
    // Differentiate will be tested for the same (case-sensitive !) variable, or another variable
    // Simplify will need to test both when the variable is in the environment, and when it is not

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testConstructor_nominal() {
        Variable variable1 = new Variable("y");
        assertEquals("y", variable1.toString());
        Variable variable2 = new Variable("aBc");
        assertEquals("aBc", variable2.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_badInputWhitespaces() {
        Variable variable = new Variable("a\nbc");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_badInputNonLetters1() {
        Variable variable = new Variable("hello world");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_badInputNonLetters2() {
        Variable variable = new Variable("helloWorld!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_badInputNonLetters3() {
        Variable variable = new Variable("hello2world");
    }

    @Test
    public void testEquals() {
        Variable variable1 = new Variable("hello");
        Variable variable2 = new Variable("world");
        assertFalse(variable1.equals(variable2) || variable2.equals(variable1));
        Variable variable3 = new Variable("hello");
        assertTrue(variable1.equals(variable3) && variable3.equals(variable1) && variable3.equals(variable3));
    }

    @Test
    public void testDifferentiate_sameVariable() {
        Variable variable = new Variable("xYz");
        Expression expectedResult = new Number(1);
        assertEquals(expectedResult, variable.differentiate("xYz"));
    }

    @Test
    public void testDifferentiate_differentVariable() {
        Variable variable = new Variable("xYz");
        Expression expectedResult = new Number(0);
        assertEquals(expectedResult, variable.differentiate("xyz"));
    }

    @Test
    public void testSimplify_notPresent() {
        Variable variable = new Variable("xYz");
        Expression expectedResult = new Variable("xYz");
        Map<String, Double> environment = new HashMap<String, Double>() {{
            put("x", 3.0);
            put("xyz", 1.7);
        }};
        assertEquals(expectedResult, variable.simplify(environment));
    }

    @Test
    public void testSimplify_present() {
        Variable variable = new Variable("xYz");
        Expression expectedResult = new Number(1.7);
        Map<String, Double> environment = new HashMap<String, Double>() {{
            put("x", 3.0);
            put("xYz", 1.7);
        }};
        assertEquals(expectedResult, variable.simplify(environment));
    }

}
