package expressivo;

import java.util.Map;
import java.util.Objects;

public class Multiply implements Expression {

    // Rep

    private final Expression leftExpression;

    private final Expression rightExpression;

    private static final Expression ZERO = new Number(0);

    private static final Expression ONE = new Number(1);

    // Rep invariant
    // Both left and right expressions are not null

    // Abstraction function
    // Represents the multiplication of the left expression with the right expression

    // Safety from rep exposure argument
    // All fields are private and final. Expression implementations are all immutable

    /**
     * @param leftExpression  any valid non-null Expression
     * @param rightExpression any valid non-null Expression
     */
    public Multiply(Expression leftExpression, Expression rightExpression) {
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
        return Objects.hash(this.rightExpression, this.leftExpression);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Multiply)) {
            return false;
        }
        Multiply other = (Multiply) o;
        return this.leftExpression.equals(other.leftExpression) && this.rightExpression.equals(other.rightExpression);
    }

    /**
     * @return a parsable representation of this expression, with whitespaces around operators,
     * with non-terminals in parentheses to showcase operation order,
     * such that for all e:Expression, e.equals(Expression.parse(e.toString())).
     */
    @Override
    public String toString() {
        return inParenthesesIfNonTerminal(this.leftExpression) + " * " + inParenthesesIfNonTerminal(this.rightExpression);
    }

    private String inParenthesesIfNonTerminal(Expression expression) {
        return expression.isTerminal() ? expression.toString() : "(" + expression + ")";
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
     * @return an expression representing the differentiation of an addition following d(u * v)/x = u(dv/dx) + v(du/dx),
     * ignore zeroes and simplify ones
     */
    @Override
    public Expression differentiate(String variable) {
        Expression leftExpressionDifferentiated = this.leftExpression.differentiate(variable);
        Expression rightExpressionDifferentiated = this.rightExpression.differentiate(variable);

        Expression lhs;
        if (this.leftExpression.equals(ZERO) || rightExpressionDifferentiated.equals(ZERO)) {
            lhs = ZERO;
        } else if (this.leftExpression.equals(ONE)) {
            lhs = rightExpressionDifferentiated;
        } else if (rightExpressionDifferentiated.equals(ONE)) {
            lhs = this.leftExpression;
        } else {
            lhs = new Multiply(this.leftExpression, rightExpressionDifferentiated);
        }

        Expression rhs;
        if (this.rightExpression.equals(ZERO) || leftExpressionDifferentiated.equals(ZERO)) {
            rhs = ZERO;
        } else if (this.rightExpression.equals(ONE)) {
            rhs = leftExpressionDifferentiated;
        } else if (leftExpressionDifferentiated.equals(ONE)) {
            rhs = this.rightExpression;
        } else {
            rhs = new Multiply(this.rightExpression, leftExpressionDifferentiated);
        }

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
            return new Number(simplifiedLHS.numericValue() * simplifiedRHS.numericValue());
        } else {
            return new Multiply(simplifiedLHS, simplifiedRHS);
        }
    }

}
