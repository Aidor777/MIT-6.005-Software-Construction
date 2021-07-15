package library;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for Book ADT.
 */
public class BookTest {

    /*
     * Testing strategy
     * ==================
     * Tests will primary focus on checking that the required invariants are not violated for the constructor.
     * This has to be tester using getters, so it's also a nice way to test that as well. A nominal case will suffice.
     * ToString can also primarily be tested by a nominal case, where the string representation contains the required elements.
     * Equals can be tested on non-equal fields; slightly different book name, authors list not same and not same order,
     * year different. Both equals and hashCode should obey the object contract;
     * Equals is reflexive (t.equals(t)), symmetric (t.equals(u) && u.equals(t)) and transitive (t.equals(u) && u.equals(v) && t.equals(v))
     * Repeated calls are consistent.
     * t.equals(null) is false
     * If t.equals(u) then t.hashCode() equals u.hashCode()
     */
    
    @Test
    public void testConstructorNominal() {
        Book book = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        assertEquals("A book", book.getTitle());
        assertEquals(1990, book.getYear());
        assertEquals(Arrays.asList("Alice", "Bob", "Charlie"), book.getAuthors());
    }

    @Test(expected=AssertionError.class)
    public void testConstructorFailure_nullTitle() {
        Book book = new Book(null, Arrays.asList("Alice", "Bob", "Charlie"), 1990);
    }

    @Test(expected=AssertionError.class)
    public void testConstructorFailure_badTitle() {
        Book book = new Book("   ", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
    }

    @Test(expected=AssertionError.class)
    public void testConstructorFailure_nullAuthors() {
        Book book = new Book("A book", null, 1990);
    }

    @Test(expected=AssertionError.class)
    public void testConstructorFailure_emptyAuthors() {
        Book book = new Book("A book", Collections.emptyList(), 1990);
    }

    @Test(expected=AssertionError.class)
    public void testConstructorFailure_badAuthors() {
        Book book = new Book("A book", Arrays.asList("Alice", "Bob", "   "), 1990);
    }

    @Test(expected=AssertionError.class)
    public void testConstructorFailure_badYear() {
        Book book = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), -1990);
    }

    @Test
    public void testToStringNominal() {
        Book book = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        String representation = book.toString();
        assertTrue(representation.contains("A book"));
        assertTrue(representation.contains("Alice"));
        assertTrue(representation.contains("Bob"));
        assertTrue(representation.contains("Charlie"));
        assertTrue(representation.contains(String.valueOf(1990)));
    }

    @Test
    public void testEquals_differentTitle() {
        Book book1 = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        Book book2 = new Book("Another book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        assertFalse(book1.equals(book2));
    }

    @Test
    public void testEquals_differentAuthors() {
        Book book1 = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        Book book2 = new Book("A book", Arrays.asList("Alice", "Bob", "Dave"), 1990);
        assertFalse(book1.equals(book2));
    }

    @Test
    public void testEquals_differentAuthorsCase() {
        Book book1 = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        Book book2 = new Book("A book", Arrays.asList("Alice", "Bob", "CHARLIE"), 1990);
        assertFalse(book1.equals(book2));
    }

    @Test
    public void testEquals_differentAuthorsOrder() {
        Book book1 = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        Book book2 = new Book("A book", Arrays.asList("Alice", "Charlie", "Bob"), 1990);
        assertFalse(book1.equals(book2));
    }

    @Test
    public void testEquals_differentYear() {
        Book book1 = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        Book book2 = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1991);
        assertFalse(book1.equals(book2));
    }

    @Test
    public void testEquals_objectContract() {
        Book book1 = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        Book book2 = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        Book book3 = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);
        assertTrue(book1.equals(book2));
        assertTrue(book2.equals(book1));
        assertTrue(book1.equals(book1));
        assertTrue(book2.equals(book3));
        assertTrue(book1.equals(book3));
        assertFalse(book1.equals(null));
        assertTrue(book1.hashCode() == book2.hashCode() && book2.hashCode() == book3.hashCode());
        assertTrue(book1.equals(book2));
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
