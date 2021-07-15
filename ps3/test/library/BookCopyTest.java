package library;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;

/**
 * Test suite for BookCopy ADT.
 */
public class BookCopyTest {

    /*
     * Testing strategy
     * ==================
     * Tests will primary focus on checking that the required invariants are not violated for the constructor.
     * This has to be tester using getters, so it's also a nice way to test that as well. A nominal case will suffice.
     * We can then test setters, also with a nominal case.
     * ToString can also primarily be tested by a nominal case, where the string representation contains the required elements.
     * Equals and hashCode should not be overridden, so we test this as well.
     */
    
    @Test
    public void testConstructorNominal() {
        Book book = new Book("Alice", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        BookCopy copy = new BookCopy(book);
        assertEquals(book, copy.getBook());
        assertEquals(BookCopy.Condition.GOOD, copy.getCondition());
    }

    @Test(expected=AssertionError.class)
    public void testConstructorFailure_nullBook() {
        BookCopy copy = new BookCopy(null);
    }

    @Test
    public void testSetterNominal() {
        Book book = new Book("Alice", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        BookCopy copy = new BookCopy(book);
        copy.setCondition(BookCopy.Condition.DAMAGED);
        assertEquals(BookCopy.Condition.DAMAGED, copy.getCondition());
        copy.setCondition(BookCopy.Condition.GOOD);
        assertEquals(BookCopy.Condition.GOOD, copy.getCondition());
    }

    @Test(expected=AssertionError.class)
    public void testSetterFailure_nullCondition() {
        Book book = new Book("Alice", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        BookCopy copy = new BookCopy(book);
        copy.setCondition(null);
    }

    @Test
    public void testToStringNominal() {
        Book book = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        BookCopy copy = new BookCopy(book);
        String representation = copy.toString();
        assertTrue(representation.contains(book.toString()));
        assertTrue(representation.contains("good"));
        copy.setCondition(BookCopy.Condition.DAMAGED);
        representation = copy.toString();
        assertTrue(representation.contains("damaged"));
    }

    @Test
    public void testEqualsAndHashCode_sameReference() {
        Book book = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        BookCopy copy = new BookCopy(book);
        BookCopy anotherCopy = copy;
        copy.setCondition(BookCopy.Condition.DAMAGED);
        assertEquals(copy, anotherCopy);
        assertEquals(copy.hashCode(), anotherCopy.hashCode());
    }

    @Test
    public void testEqualsAndHashCode_sameObservationsDifferentReference() {
        Book book = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        BookCopy copy = new BookCopy(book);
        BookCopy anotherCopy = new BookCopy(book);
        assertNotEquals(copy, anotherCopy);
    }
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }


    /* Copyright (c) 2016 MIT 6.005 course staff, all rights reserved.
     * Redistribution of original or derived work requires explicit permission.
     * Don't post any of this code on the web or to a public Github repository.
     */

}
