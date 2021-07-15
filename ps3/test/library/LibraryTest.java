package library;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for Library ADT.
 */
@RunWith(Parameterized.class)
public class LibraryTest {

    /*
     * Note: all the tests you write here must be runnable against any
     * Library class that follows the spec.  JUnit will automatically
     * run these tests against both SmallLibrary and BigLibrary.
     */

    /**
     * Implementation classes for the Library ADT.
     * JUnit runs this test suite once for each class name in the returned array.
     *
     * @return array of Java class names, including their full package prefix
     */
    @Parameters(name = "{0}")
    public static Object[] allImplementationClassNames() {
        return new Object[]{
                "library.SmallLibrary",
                "library.BigLibrary"
        };
    }

    /**
     * Implementation class being tested on this run of the test suite.
     * JUnit sets this variable automatically as it iterates through the array returned
     * by allImplementationClassNames.
     */
    @Parameter
    public String implementationClassName;

    /**
     * @return a fresh instance of a Library, constructed from the implementation class specified
     * by implementationClassName.
     */
    public Library makeLibrary() {
        try {
            Class<?> cls = Class.forName(implementationClassName);
            return (Library) cls.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    /*
     * Testing strategy
     * ==================
     * Observer methods will not need to be tested individually, as they will serve to test others.
     * We can still check that they do not return wrong results.
     * Buy will be tested with a single copy, two copies of same book added repeatedly, and two copies of two different books added.
     * For checkout, we need to verify that a single copy is not available but still in all copies,
     * that only one copy of the same book is checked out, and that other books are still available.
     * Checkin is the exact opposite strategy as checkout.
     * Find has the most test cases. We do not really know what should NOT be returned, but we know what should.
     * Any exact matching in title or authors or both, then sorting by date as required for similar books.
     * For equal books, check that only one is sent back. A more complex case with several found books.
     * To test lose, we need to check that the lost book copy does not appear anywhere anymore, but that other copies are not impacted.
     * Equals and hashCode should not be overridden, so we test this as well.
     */

    private Library library;

    private final Book book1 = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 1990);

    private final Book book2 = new Book("Another book", Arrays.asList("Dave"), 2020);

    private final Book book3 = new Book("Dave", Arrays.asList("Alice", "Dave"), 2020);

    private final Book book4 = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 2000);

    private final Book book5 = new Book("A book", Arrays.asList("Alice", "Bob", "Charlie"), 2010);

    private final Book book6 = new Book("Dave", Arrays.asList("Alice", "Bob", "Charlie"), 2010);

    @Before
    public void init() {
        library = makeLibrary();
    }

    @Test
    public void testBuy_singleBook() {
        BookCopy bookCopy = library.buy(book1);
        assertEquals(bookCopy.getCondition(), BookCopy.Condition.GOOD);
        assertEquals(bookCopy.getBook(), book1);
        assertTrue(library.isAvailable(bookCopy));
        assertAllContainsAndSize(book1, Arrays.asList(bookCopy), 1);
        assertAvailableContainsAndSize(book1, Arrays.asList(bookCopy), 1);
        assertFalse(library.allCopies(book2).contains(bookCopy));
        assertFalse(library.availableCopies(book2).contains(bookCopy));
    }

    @Test
    public void testBuy_twoCopiesSameBook() {
        BookCopy bookCopy1 = library.buy(book1);
        assertEquals(bookCopy1.getCondition(), BookCopy.Condition.GOOD);
        assertEquals(bookCopy1.getBook(), book1);
        BookCopy bookCopy2 = library.buy(book1);
        assertEquals(bookCopy2.getCondition(), BookCopy.Condition.GOOD);
        assertEquals(bookCopy2.getBook(), book1);
        assertTrue(library.isAvailable(bookCopy1));
        assertTrue(library.isAvailable(bookCopy2));
        assertAllContainsAndSize(book1, Arrays.asList(bookCopy1, bookCopy2), 2);
        assertAvailableContainsAndSize(book1, Arrays.asList(bookCopy1, bookCopy2), 2);
    }

    @Test
    public void testBuy_twoCopiesDifferentBook() {
        BookCopy bookCopy1 = library.buy(book1);
        assertEquals(bookCopy1.getCondition(), BookCopy.Condition.GOOD);
        assertEquals(bookCopy1.getBook(), book1);
        BookCopy bookCopy2 = library.buy(book2);
        assertEquals(bookCopy2.getCondition(), BookCopy.Condition.GOOD);
        assertEquals(bookCopy2.getBook(), book2);
        assertTrue(library.isAvailable(bookCopy1));
        assertTrue(library.isAvailable(bookCopy2));
        assertAllContainsAndSize(book1, Arrays.asList(bookCopy1), 1);
        assertAllContainsAndSize(book2, Arrays.asList(bookCopy2), 1);
        assertAvailableContainsAndSize(book1, Arrays.asList(bookCopy1), 1);
        assertAvailableContainsAndSize(book2, Arrays.asList(bookCopy2), 1);
    }

    @Test
    public void testCheckout_onlyBookCopy() {
        BookCopy bookCopy = library.buy(book1);
        library.checkout(bookCopy);
        assertAllContainsAndSize(book1, Arrays.asList(bookCopy), 1);
        assertAvailableContainsAndSize(book1, Collections.emptyList(), 0);
        assertFalse(library.isAvailable(bookCopy));
    }

    @Test
    public void testCheckout_twoCopiesSameBook() {
        BookCopy bookCopy1 = library.buy(book1);
        BookCopy bookCopy2 = library.buy(book1);
        library.checkout(bookCopy1);
        assertAllContainsAndSize(book1, Arrays.asList(bookCopy1, bookCopy2), 2);
        assertAvailableContainsAndSize(book1, Arrays.asList(bookCopy2), 1);
        assertFalse(library.isAvailable(bookCopy1));
        assertTrue(library.isAvailable(bookCopy2));
        library.checkout(bookCopy2);
        assertAllContainsAndSize(book1, Arrays.asList(bookCopy1, bookCopy2), 2);
        assertAvailableContainsAndSize(book1, Collections.emptyList(), 0);
        assertFalse(library.isAvailable(bookCopy2));
    }

    @Test
    public void testCheckout_twoCopiesDifferentBook() {
        BookCopy bookCopy1 = library.buy(book1);
        BookCopy bookCopy2 = library.buy(book2);
        library.checkout(bookCopy1);
        assertAllContainsAndSize(book1, Arrays.asList(bookCopy1), 1);
        assertAllContainsAndSize(book2, Arrays.asList(bookCopy2), 1);
        assertAvailableContainsAndSize(book1, Collections.emptyList(), 0);
        assertAvailableContainsAndSize(book2, Arrays.asList(bookCopy2), 1);
        assertFalse(library.isAvailable(bookCopy1));
        assertTrue(library.isAvailable(bookCopy2));
        library.checkout(bookCopy2);
        assertAllContainsAndSize(book2, Arrays.asList(bookCopy2), 1);
        assertAvailableContainsAndSize(book2, Collections.emptyList(), 0);
        assertFalse(library.isAvailable(bookCopy2));
    }

    @Test
    public void testCheckin_onlyBookCopy() {
        BookCopy bookCopy = library.buy(book1);
        library.checkout(bookCopy);
        library.checkin(bookCopy);
        assertAvailableContainsAndSize(book1, Arrays.asList(bookCopy), 1);
        assertAllContainsAndSize(book1, Arrays.asList(bookCopy), 1);
        assertTrue(library.isAvailable(bookCopy));
    }

    @Test
    public void testCheckin_twoCopiesSameBook() {
        BookCopy bookCopy1 = library.buy(book1);
        BookCopy bookCopy2 = library.buy(book1);
        library.checkout(bookCopy1);
        library.checkout(bookCopy2);
        library.checkin(bookCopy1);
        assertAllContainsAndSize(book1, Arrays.asList(bookCopy1, bookCopy2), 2);
        assertAvailableContainsAndSize(book1, Arrays.asList(bookCopy1), 1);
        assertTrue(library.isAvailable(bookCopy1));
        assertFalse(library.isAvailable(bookCopy2));
        library.checkin(bookCopy2);
        assertAllContainsAndSize(book1, Arrays.asList(bookCopy1, bookCopy2), 2);
        assertAvailableContainsAndSize(book1, Arrays.asList(bookCopy1, bookCopy2), 2);
        assertTrue(library.isAvailable(bookCopy2));
    }

    @Test
    public void testCheckin_twoCopiesDifferentBook() {
        BookCopy bookCopy1 = library.buy(book1);
        BookCopy bookCopy2 = library.buy(book2);
        library.checkout(bookCopy1);
        library.checkout(bookCopy2);
        library.checkin(bookCopy1);
        assertAllContainsAndSize(book1, Arrays.asList(bookCopy1), 1);
        assertAvailableContainsAndSize(book1, Arrays.asList(bookCopy1), 1);
        assertTrue(library.isAvailable(bookCopy1));
        assertAllContainsAndSize(book2, Arrays.asList(bookCopy2), 1);
        assertAvailableContainsAndSize(book2, Collections.emptyList(), 0);
        assertFalse(library.isAvailable(bookCopy2));
        library.checkin(bookCopy2);
        assertAllContainsAndSize(book2, Arrays.asList(bookCopy2), 1);
        assertAvailableContainsAndSize(book2, Arrays.asList(bookCopy2), 1);
        assertTrue(library.isAvailable(bookCopy2));
    }

    @Test
    public void testFind_authorMatch() {
        library.buy(book1);
        List<Book> foundBooks1 = library.find("Alice");
        List<Book> foundBooks2 = library.find("Bob");
        List<Book> foundBooks3 = library.find("Charlie");
        assertTrue(foundBooks1.contains(book1));
        assertTrue(foundBooks2.contains(book1));
        assertTrue(foundBooks3.contains(book1));
    }

    @Test
    public void testFind_titleMatch() {
        library.buy(book1);
        List<Book> foundBooks = library.find("A book");
        assertTrue(foundBooks.contains(book1));
    }

    @Test
    public void testFind_bothMatch() {
        library.buy(book3);
        List<Book> foundBooks = library.find("Dave");
        assertTrue(foundBooks.contains(book3));
        assertEquals(foundBooks.stream().filter(book -> book.equals(book3)).count(), 1L);
    }

    @Test
    public void testFind_orderingByPublicationDate() {
        library.buy(book1);
        library.buy(book5);
        library.buy(book4);
        List<Book> foundBooks = library.find("A book");
        assertTrue(foundBooks.contains(book1));
        assertTrue(foundBooks.contains(book4));
        assertTrue(foundBooks.contains(book5));
        assertTrue(foundBooks.indexOf(book5) < foundBooks.indexOf(book4));
        assertTrue(foundBooks.indexOf(book4) < foundBooks.indexOf(book1));
    }

    @Test
    public void testFind_bookFoundOnlyOnce() {
        library.buy(book1);
        library.buy(book1);
        library.buy(book1);
        List<Book> foundBooks = library.find("A book");
        assertTrue(foundBooks.contains(book1));
        assertEquals(foundBooks.stream().filter(book -> book.equals(book1)).count(), 1L);
    }

    @Test
    public void testFind_severalMatches() {
        library.buy(book1);
        library.buy(book2);
        library.buy(book3);
        library.buy(book4);
        library.buy(book5);
        library.buy(book6);
        List<Book> foundBooks = library.find("Dave");
        assertTrue(foundBooks.contains(book2));
        assertTrue(foundBooks.contains(book3));
        assertTrue(foundBooks.contains(book6));
    }

    @Test
    public void testLose_onlyBook() {
        BookCopy bookCopy = library.buy(book1);
        library.lose(bookCopy);
        assertFalse(library.isAvailable(bookCopy));
        assertAllContainsAndSize(book1, Collections.emptyList(), 0);
        assertAvailableContainsAndSize(book1, Collections.emptyList(), 0);
    }

    @Test
    public void testLose_sameBookTwoCopies() {
        BookCopy bookCopy1 = library.buy(book1);
        BookCopy bookCopy2 = library.buy(book1);
        library.lose(bookCopy1);
        assertFalse(library.isAvailable(bookCopy1));
        assertAllContainsAndSize(book1, Arrays.asList(bookCopy2), 1);
        assertAvailableContainsAndSize(book1, Arrays.asList(bookCopy2), 1);
        assertTrue(library.isAvailable(bookCopy2));
        library.lose(bookCopy2);
        assertFalse(library.isAvailable(bookCopy2));
        assertAllContainsAndSize(book1, Collections.emptyList(), 0);
        assertAvailableContainsAndSize(book1, Collections.emptyList(), 0);
    }

    @Test
    public void testLose_differentBooks() {
        BookCopy bookCopy1 = library.buy(book1);
        BookCopy bookCopy2 = library.buy(book2);
        library.lose(bookCopy1);
        assertAllContainsAndSize(book1, Collections.emptyList(), 0);
        assertAvailableContainsAndSize(book1, Collections.emptyList(), 0);
        assertFalse(library.isAvailable(bookCopy1));
        assertTrue(library.isAvailable(bookCopy2));
        assertAllContainsAndSize(book2, Arrays.asList(bookCopy2), 1);
        assertAvailableContainsAndSize(book2, Arrays.asList(bookCopy2), 1);
        library.lose(bookCopy2);
        assertFalse(library.isAvailable(bookCopy2));
        assertAllContainsAndSize(book2, Collections.emptyList(), 0);
        assertAvailableContainsAndSize(book2, Collections.emptyList(), 0);
    }

    @Test
    public void testEqualsAndHashCode_sameReference() {
        Library other = library;
        library.buy(book1);
        assertEquals(library, other);
        assertEquals(library.hashCode(), other.hashCode());
    }

    @Test
    public void testEqualsAndHashCode_sameObservationsDifferentReference() {
        Library other = makeLibrary();
        assertNotEquals(library, other);
    }

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    private void assertAllContainsAndSize(Book book, List<BookCopy> bookCopies, int size) {
        bookCopies.forEach(bookCopy -> assertTrue(library.allCopies(book).contains(bookCopy)));
        assertEquals(library.allCopies(book).size(), size);
    }

    private void assertAvailableContainsAndSize(Book book, List<BookCopy> bookCopies, int size) {
        bookCopies.forEach(bookCopy -> assertTrue(library.availableCopies(book).contains(bookCopy)));
        assertEquals(library.availableCopies(book).size(), size);
    }


    /* Copyright (c) 2016 MIT 6.005 course staff, all rights reserved.
     * Redistribution of original or derived work requires explicit permission.
     * Don't post any of this code on the web or to a public Github repository.
     */

}
