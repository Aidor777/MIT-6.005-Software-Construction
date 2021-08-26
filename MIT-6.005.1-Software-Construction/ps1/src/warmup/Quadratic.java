package warmup;

import java.util.HashSet;
import java.util.Set;

public class Quadratic {

    /**
     * Find the integer roots of a quadratic equation, ax^2 + bx + c = 0.
     *
     * @param a coefficient of x^2
     * @param b coefficient of x
     * @param c constant term.  Requires that a, b, and c are not ALL zero.
     * @return all integers x such that ax^2 + bx + c = 0.
     */
    public static Set<Integer> roots(int a, int b, int c) {
        Set<Integer> result = new HashSet<>();

        // Case of a linear equation
        if (a == 0) {
            if (b != 0 && c % b == 0) {
                int root = -c / b;
                result.add(root);
            }
        } else { // Actual quadratic equation
            // Solved using the famous quadratic formula everyone has had to learn by heart
            // Parts of this computation may overflow, better cast to long first
            long bSquaredMinus4AC = (long) b * b - (long) 4 * a * c;
            boolean hasSolution = bSquaredMinus4AC >= 0;
            if (hasSolution) {
                int denominator = 2 * a;
                double numerator1 = -b + Math.sqrt(bSquaredMinus4AC);
                double numerator2 = -b - Math.sqrt(bSquaredMinus4AC);

                // Only return integer results
                if (numerator1 % denominator == 0) {
                    int root1 = (int) (numerator1 / denominator);
                    result.add(root1);
                }
                if (numerator1 != numerator2 && numerator2 % denominator == 0) {
                    int root2 = (int) (numerator2 / denominator);
                    result.add(root2);
                }
            }
        }

        return result;
    }


    /**
     * Main function of program.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        System.out.println("For the equation x^2 - 4x + 3 = 0, the possible solutions are:");
        Set<Integer> result = roots(1, -4, 3);
        System.out.println(result);
    }

    /* Copyright (c) 2016 MIT 6.005 course staff, all rights reserved.
     * Redistribution of original or derived work requires explicit permission.
     * Don't post any of this code on the web or to a public Github repository.
     */
}
