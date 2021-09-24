package expressivo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class Number implements Expression {

    // Rep

    private final double number;

    private static final double CLOSE_ENOUGH_DIFFERENCE = 0.00005;

    private static final Expression ZERO = new Number(0);

    // Rep invariant
    // Number is non-negative

    // Abstraction function
    // Represents a floating-point rational number or an integer number

    // Safety from rep exposure argument
    // All fields are private and final.

    /**
     * @param number a floating-point or integer number to be represented as an expression
     * @throws IllegalArgumentException if the given number is negative
     */
    public Number(double number) {
        if (number < 0.0) {
            throw new IllegalArgumentException("Number must be non-negative");
        }
        this.number = number;
        checkRep();
    }

    // Assert the rep invariant
    private void checkRep() {
        assert this.number >= 0.0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(this.number);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Number)) {
            return false;
        }
        Number other = (Number) o;
        return Math.abs(this.number - other.number) < CLOSE_ENOUGH_DIFFERENCE;
    }

    /**
     * @return a parsable representation of this expression, trimmed of all whitespaces and
     * without trailing zeroes and a maximum of 4 decimals,
     * such that for all e:Expression, e.equals(Expression.parse(e.toString())).
     */
    @Override
    public String toString() {
        return BigDecimal.valueOf(this.number).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toString();
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    /**
     * @return the number contained in this expression
     */
    @Override
    public double numericValue() {
        return this.number;
    }

    /**
     * @param variable the variable to differentiate by, a case-sensitive nonempty string of letters.
     * @return the result of differentiating a constant c with respect to a variable x, following dc/dx = 0 this will always be Number(n=0)
     */
    @Override
    public Expression differentiate(String variable) {
        return ZERO;
    }

    /**
     * @param environment an environment mapping a variable name to its numeric value
     * @return the same expression
     */
    @Override
    public Expression simplify(Map<String, Double> environment) {
        return this;
    }

}
