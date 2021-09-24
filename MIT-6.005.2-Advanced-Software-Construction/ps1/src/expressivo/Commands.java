package expressivo;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String-based commands provided by the expression system.
 */
public class Commands {

    private static final Pattern ONE_OR_MORE_LETTERS = Pattern.compile("^[a-zA-Z]+$");

    /**
     * Differentiate an expression with respect to a variable.
     *
     * @param expression the expression to differentiate
     * @param variable   the variable to differentiate by, a case-sensitive nonempty string of letters.
     * @return expression's derivative with respect to variable.  Must be a valid expression equal
     * to the derivative, but doesn't need to be in simplest or canonical form.
     * @throws IllegalArgumentException if the expression or variable is invalid
     */
    public static String differentiate(String expression, String variable) {
        Matcher matcher = ONE_OR_MORE_LETTERS.matcher(variable);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Input variable is not a non-empty string of letters");
        }

        Expression expr = Expression.parse(expression.trim());
        Expression differentiatedExpression = expr.differentiate(variable.trim());
        return differentiatedExpression.toString();
    }

    /**
     * Simplify an expression.
     *
     * @param expression  the expression to simplify
     * @param environment maps variables to values.  Variables are required to be case-sensitive nonempty
     *                    strings of letters.  The set of variables in environment is allowed to be different than the
     *                    set of variables actually found in expression.  Values must be nonnegative numbers.
     * @return an expression equal to the input, but after substituting every variable v that appears in both
     * the expression and the environment with its value, environment.get(v).  If there are no
     * variables left in this expression after substitution, it must be evaluated to a single number.
     * Additional simplifications to the expression may be done at the implementor's discretion.
     * @throws IllegalArgumentException if the expression is invalid
     */
    public static String simplify(String expression, Map<String, Double> environment) {
        for (String variable : environment.keySet()) {
            Matcher matcher = ONE_OR_MORE_LETTERS.matcher(variable);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Environment variable is not a non-empty string of letters");
            }
            if (environment.get(variable) < 0.0) {
                throw new IllegalArgumentException("Environment value is negative");
            }
        }
        Expression expr = Expression.parse(expression.trim());
        Expression simplifiedExpression = expr.simplify(environment);
        return simplifiedExpression.toString();
    }

    /* Copyright (c) 2015-2017 MIT 6.005 course staff, all rights reserved.
     * Redistribution of original or derived work requires permission of course staff.
     */
}
