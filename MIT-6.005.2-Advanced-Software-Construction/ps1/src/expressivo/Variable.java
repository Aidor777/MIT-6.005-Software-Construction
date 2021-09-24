package expressivo;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Variable implements Expression {

    // Rep

    private final String variableName;

    private static final Expression ONE = new Number(1);

    private static final Expression ZERO = new Number(0);

    // Rep invariant
    // Variable name is a non-empty string consisting only of letters (capital or not)

    // Abstraction function
    // Represents a variable identified by its name

    // Safety from rep exposure argument
    // All fields are private and final. String is immutable

    /**
     * @param variableName the name to give to this variable
     * @throws IllegalArgumentException if given name is null, empty, or contains characters other than letters
     */
    public Variable(String variableName) {
        if (variableName == null || variableName.trim().isEmpty() || !nameMatches(variableName)) {
            throw new IllegalArgumentException("Illegal variable name");
        }
        this.variableName = variableName;
        checkRep();
    }

    // Assert the rep invariant
    private void checkRep() {
        assert this.variableName != null;
        assert !this.variableName.trim().isEmpty();
        assert nameMatches(this.variableName);
    }

    private boolean nameMatches(String name) {
        Pattern onlyLettersPattern = Pattern.compile("^[a-zA-Z]+$");
        Matcher matcher = onlyLettersPattern.matcher(name);
        return matcher.matches();
    }

    @Override
    public int hashCode() {
        return this.variableName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Variable)) {
            return false;
        }
        Variable other = (Variable) o;
        return this.variableName.equals(other.variableName);
    }

    @Override
    public String toString() {
        return this.variableName;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    /**
     * @param variable the variable to differentiate by, a case-sensitive nonempty string of letters.
     * @return the result of differentiating a variable y with respect to a variable x, which is either 1 if y = x, and 0 otherwise
     */
    @Override
    public Expression differentiate(String variable) {
        if (variable.equals(this.variableName)) {
            return ONE;
        } else {
            return ZERO;
        }
    }

    /**
     * @param environment an environment mapping a variable name to its numeric value
     * @return the corresponding number if the variable was found in the environment, the same expression otherwise
     */
    @Override
    public Expression simplify(Map<String, Double> environment) {
        if (environment.containsKey(this.variableName)) {
            return new Number(environment.get(this.variableName));
        } else {
            return this;
        }
    }

}
