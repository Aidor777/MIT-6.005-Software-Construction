package expressivo;

import lib6005.parser.GrammarCompiler;
import lib6005.parser.ParseTree;
import lib6005.parser.Parser;
import lib6005.parser.UnableToParseException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An immutable data type representing a polynomial expression of:
 * + and *
 * nonnegative integers and floating-point numbers
 * variables (case-sensitive nonempty strings of letters)
 */
public interface Expression {

    // Datatype definition
    // Expression = Number(n:double) + Variable(s:String) +
    // + Add(left:Expression, right:Expression) + Multiply(left:Expression, right:Expression)

    /**
     * Parse an expression.
     *
     * @param input expression to parse, as defined in the PS1 handout.
     * @return expression AST for the input
     * @throws IllegalArgumentException if the expression is invalid
     * @throws RuntimeException         if the grammar file contains errors and could not be parsed or if the grammar file could not be found
     */
    static Expression parse(String input) {
        ParseTree<ExpressionGrammar> parseTree;
        try {
            Parser<ExpressionGrammar> parser = GrammarCompiler.compile(new File("src/expressivo/Expression.g"), ExpressionGrammar.ROOT);
            parseTree = parser.parse(input.trim());
        } catch (UnableToParseException e) {
            throw new IllegalArgumentException("Either the grammar definition file contains errors and could not be parsed,"
                    + " or the input does not fit the grammar", e);
        } catch (IOException e) {
            throw new RuntimeException("Unable to open grammar definition file", e);
        }
        Logger.getGlobal().log(Level.FINE, "Parse tree built from input: " + parseTree.toString());
        // Uncomment to display the concrete syntax tree in a browser
        // tree.display();
        return buildAst(parseTree);
    }

    /**
     * Converts the ParseTree (concrete syntax tree) to Expression (abstract syntax tree)
     *
     * @param parseTree assumed to have been constructed using the grammar defined in Expression.g
     */
    static Expression buildAst(ParseTree<ExpressionGrammar> parseTree) {
        switch (parseTree.getName()) {
            case ROOT:
                // ROOT can only have one child of type SUM, the rest are whitespaces
                return buildAst(parseTree.children().get(0));
            case SUM:
                // SUM has one or more children of type MULTIPLY to be added, ignore whitespaces
                boolean lastSum = true;
                Expression sumResult = null;
                for (int i = parseTree.childrenByName(ExpressionGrammar.MULTIPLY).size() - 1; i >= 0; i--) {
                    ParseTree<ExpressionGrammar> child = parseTree.childrenByName(ExpressionGrammar.MULTIPLY).get(i);
                    if (lastSum) {
                        sumResult = buildAst(child);
                        lastSum = false;
                    } else {
                        sumResult = new Add(buildAst(child), sumResult);
                    }
                }
                if (lastSum) {
                    throw new RuntimeException("The expression must not consist of whitespaces only: " + parseTree);
                }
                return sumResult;
            case MULTIPLY:
                // MULTIPLY has one or more children of type PRIMITIVE to be multiplied, ignore whitespaces
                boolean lastMultiply = true;
                Expression multiplyResult = null;
                for (int i = parseTree.childrenByName(ExpressionGrammar.PRIMITIVE).size() - 1; i >= 0; i--) {
                    ParseTree<ExpressionGrammar> child = parseTree.childrenByName(ExpressionGrammar.PRIMITIVE).get(i);
                    if (lastMultiply) {
                        multiplyResult = buildAst(child);
                        lastMultiply = false;
                    } else {
                        multiplyResult = new Multiply(buildAst(child), multiplyResult);
                    }
                }
                if (lastMultiply) {
                    throw new RuntimeException("The expression must not consist of whitespaces only: " + parseTree);
                }
                return multiplyResult;
            case PRIMITIVE:
                // PRIMITIVE only has one child that is not a whitespace, we just need to figure which kind
                if (!parseTree.childrenByName(ExpressionGrammar.NUMBER).isEmpty()) {
                    return buildAst(parseTree.childrenByName(ExpressionGrammar.NUMBER).get(0));
                } else if (!parseTree.childrenByName(ExpressionGrammar.VARIABLE).isEmpty()) {
                    return buildAst(parseTree.childrenByName(ExpressionGrammar.VARIABLE).get(0));
                } else {
                    return buildAst(parseTree.childrenByName(ExpressionGrammar.SUM).get(0));
                }
            case NUMBER:
                // This terminal will contain a number
                return new Number(Double.parseDouble(parseTree.getContents()));
            case VARIABLE:
                // This terminal will contain a variable name
                return new Variable(parseTree.getContents());
            case WHITESPACE:
            default:
                throw new RuntimeException("You should never reach here: " + parseTree);
        }
    }

    /**
     * @return a parsable representation of this expression, with whitespaces around operators,
     * such that for all e:Expression, e.equals(Expression.parse(e.toString())).
     */
    @Override
    String toString();

    /**
     * @param thatObject any object
     * @return true if and only if this and thatObject are structurally-equal
     * Expressions, as defined in the PS1 handout.
     */
    @Override
    boolean equals(Object thatObject);

    /**
     * @return hash code value consistent with the equals() definition of structural
     * equality, such that for all e1,e2:Expression,
     * e1.equals(e2) implies e1.hashCode() == e2.hashCode()
     */
    @Override
    int hashCode();

    /**
     * @return true if the expression has no children, false otherwise
     */
    boolean isTerminal();

    /**
     * @return true if the expression is an atomic number, false otherwise
     */
    boolean isNumeric();

    /**
     * @return the numeric value of the expression
     * @throws UnsupportedOperationException if the expression returns false when calling {@link #isNumeric()}
     */
    default double numericValue() {
        throw new UnsupportedOperationException("Is not numeric");
    }

    /**
     * @param variable the variable to differentiate by, a case-sensitive nonempty string of letters.
     * @return an expression representing the result of differentiating the current expression with respect to the given variable
     */
    Expression differentiate(String variable);

    /**
     * @param environment an environment mapping a variable name to its numeric value
     * @return an expression where variables present in the environment have been replaced by their numeric value.<br/>
     * If no variables remain, numeric values have to be grouped together into a single number
     */
    Expression simplify(Map<String, Double> environment);

    enum ExpressionGrammar {
        ROOT, SUM, MULTIPLY, PRIMITIVE, NUMBER, VARIABLE, WHITESPACE;
    }

    /* Copyright (c) 2015-2017 MIT 6.005 course staff, all rights reserved.
     * Redistribution of original or derived work requires permission of course staff.
     */
}
