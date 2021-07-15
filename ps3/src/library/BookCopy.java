package library;

import java.util.Locale;

/**
 * BookCopy is a mutable type representing a particular copy of a book that is held in a library's
 * collection.
 */
public class BookCopy {

    // Rep

    private final Book book;

    private Condition condition;
    
    // Rep invariant
    // Condition is either good or damaged

    // Abstraction function
    // A book copy contains data about the book it is a specific copy of, and its condition either good or damaged.

    // Safety from rep exposure argument
    // All fields are private. Book is immutable and final. Condition may be mutable, but is restricted by enum values.
    
    public static enum Condition {
        GOOD, DAMAGED
    };
    
    /**
     * Make a new BookCopy, initially in good condition.
     * @param book the Book of which this is a copy
     */
    public BookCopy(Book book) {
        this.book = book;
        this.condition = Condition.GOOD;
        checkRep();
    }
    
    // Assert the rep invariant
    private void checkRep() {
        assert this.book != null;
        assert this.condition != null;
        assert this.condition.getClass().equals(Condition.class); // May be very difficult to break
    }
    
    /**
     * @return the Book of which this is a copy
     */
    public Book getBook() {
        return this.book;
    }
    
    /**
     * @return the condition of this book copy
     */
    public Condition getCondition() {
        return this.condition;
    }

    /**
     * Set the condition of a book copy.  This typically happens when a book copy is returned and a librarian inspects it.
     * @param condition the latest condition of the book copy
     */
    public void setCondition(Condition condition) {
        this.condition = condition;
        checkRep();
    }
    
    /**
     * @return human-readable representation of this book that includes book.toString()
     *    and the words "good" or "damaged" depending on its condition
     */
    @Override
    public String toString() {
        return "BookCopy{" +
                "book=" + book +
                ", condition=" + condition.toString().toLowerCase(Locale.ROOT) +
                '}';
    }

    /* Copyright (c) 2016 MIT 6.005 course staff, all rights reserved.
     * Redistribution of original or derived work requires explicit permission.
     * Don't post any of this code on the web or to a public Github repository.
     */

}
