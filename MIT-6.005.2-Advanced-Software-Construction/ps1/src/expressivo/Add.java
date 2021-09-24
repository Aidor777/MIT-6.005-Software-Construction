package expressivo;

import java.util.Map;
import java.util.Objects;

public class Add implements Expression {

    // Rep

    private final Expression leftExpression;

    private final Expression rightExpression;

    private static final Expression ZERO = new Number(0);

    // Rep invariant
    // Both left and right expressions are not null

    // Abstraction function
    // Represents the addition of the left expression to the right expression

    // Safety from rep exposure argument
    // All fields are private and final. Expression implementations are all immutable

    /**
     * @param leftExpression  any valid non-null Expression
     * @param rightExpression any valid non-null Expression
     */
    public Add(Expression leftExpression, Expression rightExpression) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
        checkRep();
    }

    // Assert the rep invariant
    private void checkRep() {
        assert leftExpression != null;
        assert rightExpression != null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.leftExpression, this.rightExpression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Add)) {
            return false;
        }
        Add other = (Add) o;
        return this.leftExpression.equals(other.leftExpression) && this.rightExpression.equals(other.rightExpression);
    }

    @Override
    public String toString() {
        return this.leftExpression.toString() + " + " + this.rightExpression.toString();
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    /**
     * @param variable the variable to differentiate by, a case-sensitive nonempty string of letters.
     * @return an expression representing the differentiation of an addition following d(u + v)/x = du/dx + dv/dx, ignore zeroes
     */
    @Override
    public Expression differentiate(String variable) {
        Expression lhs = this.leftExpression.differentiate(variable);
        Expression rhs = this.rightExpression.differentiate(variable);
        if (lhs.equals(ZERO) && rhs.equals(ZERO)) {
            return ZERO;
        } else if (lhs.equals(ZERO)) {
            return rhs;
        } else if (rhs.equals(ZERO)) {
            return lhs;
        } else {
            return new Add(lhs, rhs);
        }
    }

    /**
     * @param environment an environment mapping a variable name to its numeric value
     * @return an expression where variables present in the environment have been replaced by their numeric value.<br/>
     * If no variables remain, numeric values have to be grouped together into a single number
     */
    @Override
    public Expression simplify(Map<String, Double> environment) {
        Expression simplifiedLHS = this.leftExpression.simplify(environment);
        Expression simplifiedRHS = this.rightExpression.simplify(environment);
        if (simplifiedLHS.isNumeric() && simplifiedRHS.isNumeric()) {
            return new Number(simplifiedLHS.numericValue() + simplifiedRHS.numericValue());
        } else {
            return new Add(simplifiedLHS, simplifiedRHS);
        }
    }

}
