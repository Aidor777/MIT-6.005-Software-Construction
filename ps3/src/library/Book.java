package library;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Book is an immutable type representing an edition of a book -- not the physical object, 
 * but the combination of words and pictures that make up a book.  Each book is uniquely
 * identified by its title, author list, and publication year.  Alphabetic case and author 
 * order are significant, so a book written by "Fred" is different than a book written by "FRED".
 */
public class Book {

    // Rep

    private final String title;

    private final List<String> authors;

    private final int year;
    
    // Rep invariant
    // This is mostly described in the constructor spec

    // Abstraction function
    // Represents a book whose title is title, whose authors are contained and ordered in authors, and whose publication year is year

    // Safety from rep exposure argument
    // All fields are private and final. Both title and year are immutable. Authors is only exposed using defensive copying.

    /**
     * Make a Book.
     * @param title Title of the book. Must contain at least one non-space character.
     * @param authors Names of the authors of the book.  Must have at least one name, and each name must contain 
     * at least one non-space character.
     * @param year Year when this edition was published in the conventional (Common Era) calendar.  Must be non-negative.
     */
    public Book(String title, List<String> authors, int year) {
        this.title = title;
        this.authors = authors;
        this.year = year;
        checkRep();
    }
    
    // Assert the rep invariant
    private void checkRep() {
        assert this != null;
        assert this.title != null;
        assert !this.title.trim().isEmpty();
        assert this.authors != null;
        assert !this.authors.isEmpty();
        for (String author : this.authors) {
            assert !author.trim().isEmpty();
        }
        assert this.year >= 0;
    }
    
    /**
     * @return the title of this book
     */
    public String getTitle() {
        return this.title;
    }
    
    /**
     * @return the authors of this book
     */
    public List<String> getAuthors() {
        return this.authors.stream().collect(Collectors.toList());
    }

    /**
     * @return the year that this book was published
     */
    public int getYear() {
        return this.year;
    }

    /**
     * @return human-readable representation of this book that includes its title,
     *    authors, and publication year
     */
    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", authors=" + authors +
                ", year=" + year +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return year == book.year && title.equals(book.title) && authors.equals(book.authors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, authors, year);
    }

    /* Copyright (c) 2016 MIT 6.005 course staff, all rights reserved.
     * Redistribution of original or derived work requires explicit permission.
     * Don't post any of this code on the web or to a public Github repository.
     */

}
