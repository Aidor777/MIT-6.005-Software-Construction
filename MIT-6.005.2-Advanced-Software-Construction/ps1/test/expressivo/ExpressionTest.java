/* Copyright (c) 2015-2017 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package expressivo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests for the Expression abstract data type.
 */
public class ExpressionTest {

    // Testing strategy
    // The only method to test here is parse.
    // We should test for errors, namely an RuntimeException, for the following cases:
    // Left and right parentheses count does not match, invalid variable (disallowed chars), invalid operations,
    // empty parentheses, missing operation between numbers or variables, missing LHS, missing RHS
    // Nominal cases would be: a single integer number, a single decimal number, a single variable,
    // a single variable surrounded by parentheses, ignoring whitespace correctly, 3 simple additions (mix and match numbers and variables),
    // 3 simple multiplications, additions of additions, multiplications of multiplications,
    // and respect operations order when combining multiplications with additions, different concrete ST giving same AST,
    // similar CST giving different AST

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_parenthesesCountNotMatch() {
        Expression expression = Expression.parse("((1.2) + x) + (3) + yz)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_invalidVariable() {
        Expression expression = Expression.parse("1.2 + thé");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_invalidOperation() {
        Expression expression = Expression.parse("1.2 - x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_emptyParentheses() {
        Expression expression = Expression.parse("1.2 + ()");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_missingOperation() {
        Expression expression = Expression.parse("1.2  x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_missingLHS() {
        Expression expression = Expression.parse("+ x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_missingRHS() {
        Expression expression = Expression.parse("1.2 +");
    }

    @Test
    public void testParse_singleInteger() {
        Expression expression = Expression.parse("23");
        assertEquals(new Number(23), expression);
    }

    @Test
    public void testParse_singleDecimal() {
        Expression expression = Expression.parse("23.45");
        assertEquals(new Number(23.45), expression);
    }

    @Test
    public void testParse_singleVariable() {
        Expression expression = Expression.parse("xYz");
        assertEquals(new Variable("xYz"), expression);
    }

    @Test
    public void testParse_singleVariableSurroundedByParentheses() {
        Expression expression = Expression.parse("(((xYz)))");
        assertEquals(new Variable("xYz"), expression);
    }

    @Test
    public void testParse_ignoreWhitespacesSingle() {
        Expression expression = Expression.parse(" xYz\t");
        assertEquals(new Variable("xYz"), expression);
    }

    @Test
    public void testParse_simpleAddition1() {
        Expression expression = Expression.parse("1.23 + 2.34");
        assertEquals(new Add(new Number(1.23), new Number(2.34)), expression);
    }

    @Test
    public void testParse_simpleAddition2() {
        Expression expression = Expression.parse("xy + yz");
        assertEquals(new Add(new Variable("xy"), new Variable("yz")), expression);
    }

    @Test
    public void testParse_simpleAddition3() {
        Expression expression = Expression.parse("34 + yz");
        assertEquals(new Add(new Number(34), new Variable("yz")), expression);
    }

    @Test
    public void testParse_simpleMultiplication1() {
        Expression expression = Expression.parse("1.23 * 2.34");
        assertEquals(new Multiply(new Number(1.23), new Number(2.34)), expression);
    }

    @Test
    public void testParse_simpleMultiplication2() {
        Expression expression = Expression.parse("xy * yz");
        assertEquals(new Multiply(new Variable("xy"), new Variable("yz")), expression);
    }

    @Test
    public void testParse_simpleMultiplication3() {
        Expression expression = Expression.parse("34 * yz");
        assertEquals(new Multiply(new Number(34), new Variable("yz")), expression);
    }

    @Test
    public void testParse_combineAdditions() {
        Expression expression = Expression.parse("34 + yz + 25.6 + xz");
        assertEquals(new Add(
                new Number(34),
                new Add(new Variable("yz"),
                        new Add(new Number(25.6),
                                new Variable("xz")))), expression);
    }

    @Test
    public void testParse_combineMultiplications() {
        Expression expression = Expression.parse("34 * yz * 25.6 * xz");
        assertEquals(new Multiply(
                new Number(34),
                new Multiply(
                        new Variable("yz"),
                        new Multiply(
                                new Number(25.6),
                                new Variable("xz")))), expression);
    }

    @Test
    public void testParse_ignoreWhitespacesMulti() {
        Expression expression = Expression.parse("1+w +\tz ");
        assertEquals(new Add(
                new Number(1),
                new Add(new Variable("w"),
                        new Variable("z"))), expression);
    }

    @Test
    public void testParse_respectOperationOrder1() {
        Expression expression = Expression.parse("1.2 + x * 3");
        assertEquals(new Add(
                new Number(1.2),
                new Multiply(
                        new Variable("x"),
                        new Number(3))), expression);
    }

    @Test
    public void testParse_respectOperationOrder2() {
        Expression expression = Expression.parse("1.2 * x + 3");
        assertEquals(new Add(
                new Multiply(
                        new Number(1.2),
                        new Variable("x")),
                new Number(3)), expression);
    }

    @Test
    public void testParse_respectOperationOrder3() {
        Expression expression = Expression.parse("1.2 * x + 3 * yz");
        assertEquals(new Add(
                new Multiply(
                        new Number(1.2),
                        new Variable("x")),
                new Multiply(
                        new Number(3),
                        new Variable("yz"))), expression);
    }

    @Test
    public void testParse_respectOperationOrder4() {
        Expression expression = Expression.parse("1.2 + x * 3 + yz");
        assertEquals(new Add(
                new Number(1.2),
                new Add(
                        new Multiply(
                                new Variable("x"),
                                new Number(3)),
                        new Variable("yz"))), expression);
    }

    @Test
    public void testParse_respectOperationOrder5() {
        Expression expression = Expression.parse("(1.2 + x) * 3");
        assertEquals(new Multiply(
                new Add(new Number(1.2),
                        new Variable("x")),
                new Number(3)), expression);
    }

    @Test
    public void testParse_respectOperationOrder6() {
        Expression expression = Expression.parse("1.2 * (x + 3)");
        assertEquals(new Multiply(
                new Number(1.2),
                new Add(new Variable("x"),
                        new Number(3))), expression);
    }

    @Test
    public void testParse_respectOperationOrder7() {
        Expression expression = Expression.parse("(1.2 + x) * (3 * yz)");
        assertEquals(new Multiply(
                new Add(new Number(1.2),
                        new Variable("x")),
                new Multiply(
                        new Number(3),
                        new Variable("yz"))), expression);
    }

    @Test
    public void testParse_respectOperationOrder8() {
        Expression expression = Expression.parse("1.2 + (x * (3 + yz))");
        assertEquals(new Add(
                new Number(1.2),
                new Multiply(
                        new Variable("x"),
                        new Add(new Number(3),
                                new Variable("yz")))), expression);
    }

    @Test
    public void testParse_respectOperationOrder9() {
        Expression expression = Expression.parse("((1.2 + x) * 3) + yz");
        assertEquals(new Add(
                new Multiply(
                        new Add(
                                new Number(1.2),
                                new Variable("x")),
                        new Number(3)),
                new Variable("yz")), expression);
    }

    @Test
    public void testParse_differentCstSameAst() {
        Expression expression1 = Expression.parse("1.2 * x + 3");
        Expression expression2 = Expression.parse("(1.2 * x + 3)");
        Expression expression3 = Expression.parse("(1.2 * x) + 3");
        Expression expression4 = Expression.parse("((1.2) * (x)) + (3)");
        Expression expectedResult = new Add(
                new Multiply(
                        new Number(1.2),
                        new Variable("x")),
                new Number(3));
        assertEquals(expression1, expectedResult);
        assertEquals(expression2, expectedResult);
        assertEquals(expression3, expectedResult);
        assertEquals(expression4, expectedResult);
    }

    @Test
    public void testParse_similarCstDifferentAst() {
        Expression expression1 = Expression.parse("1.2 + x + 3 + yz");
        Expression expression2 = Expression.parse("(1.2 + x + 3) + yz");
        Expression expression3 = Expression.parse("(1.2 + x) + 3 + yz");
        Expression expression4 = Expression.parse("((1.2 + x) + 3) + yz");
        assertNotEquals(expression1, expression2);
        assertNotEquals(expression1, expression3);
        assertNotEquals(expression1, expression4);
        assertNotEquals(expression2, expression3);
        assertNotEquals(expression2, expression4);
        assertNotEquals(expression3, expression4);
    }

}
