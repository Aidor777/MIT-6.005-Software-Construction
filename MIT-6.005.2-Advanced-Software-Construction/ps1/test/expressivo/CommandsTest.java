/* Copyright (c) 2015-2017 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package expressivo;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the static methods of Commands.
 */
public class CommandsTest {

    // Testing strategy
    // As the differentiate operation is already extensively tested on expressions, we would just need one nominal case for that.
    // Then, we could also test for bad inputs.
    // Same strategy for simplification.

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test
    public void testDifferentiate_nominal() {
        String expression = "3 * x + x*(x + y)";
        String variable = "x";
        String expectedResult = "3 + x + x + y";
        String actualResult = Commands.differentiate(expression, variable);
        assertEquals(expectedResult, actualResult);
    }

    @Test(expected = RuntimeException.class)
    public void testDifferentiate_badVariable() {
        String expression = "3 * x + x*(x + y)";
        String variable = "2x";
        Commands.differentiate(expression, variable);
    }

    @Test(expected = RuntimeException.class)
    public void testDifferentiate_badExpression() {
        String expression = "3 * x + x*+(x + y)";
        String variable = "x";
        Commands.differentiate(expression, variable);
    }

    @Test
    public void testSimplify_nominal() {
        String expression = "3 * x + x*(x + y)";
        String expectedResult = "39";
        Map<String, Double> environment = new HashMap<String, Double>() {{
            put("x", 3.0);
            put("yz", 1.7);
            put("y", 7.0);
        }};
        String actualResult = Commands.simplify(expression, environment);
        assertEquals(expectedResult, actualResult);
    }

    @Test(expected = RuntimeException.class)
    public void testSimplify_badEnvironmentVariable() {
        String expression = "3 * x + x*(x + y)";
        Map<String, Double> environment = new HashMap<String, Double>() {{
            put("x", 3.0);
            put("y+z", 1.7);
            put("y", 7.0);
        }};
        Commands.simplify(expression, environment);
    }

    @Test(expected = RuntimeException.class)
    public void testSimplify_badEnvironmentValue() {
        String expression = "3 * x + x*(x + y)";
        Map<String, Double> environment = new HashMap<String, Double>() {{
            put("x", 3.0);
            put("yz", -1.7);
            put("y", 7.0);
        }};
        Commands.simplify(expression, environment);
    }

    @Test(expected = RuntimeException.class)
    public void testSimplify_badExpression() {
        String expression = "3 * x + x*+(x + y)";
        Map<String, Double> environment = new HashMap<String, Double>() {{
            put("x", 3.0);
            put("yz", 1.7);
            put("y", 7.0);
        }};
        Commands.simplify(expression, environment);
    }

}
