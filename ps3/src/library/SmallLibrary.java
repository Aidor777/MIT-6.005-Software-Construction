package library;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SmallLibrary represents a small collection of books, like a single person's home collection.
 */
public class SmallLibrary implements Library {

    // Rep
    private Set<BookCopy> inLibrary;

    private Set<BookCopy> checkedOut;

    // Rep invariant:
    //    the intersection of inLibrary and checkedOut is the empty set
    //
    // Abstraction function:
    //    represents the collection of books inLibrary union checkedOut,
    //      where if a book copy is in inLibrary then it is available,
    //      and if a copy is in checkedOut then it is checked out

    // Safety from rep exposure argument
    // All fields are private. Even though they are not final, they are not exposed and thus cannot be reassigned.
    // Any time a set is mutated, the rep invariant is checked.

    private static final Comparator<Book> BOOK_SORTING_COMPARATOR = Comparator.comparing(Book::getYear).reversed();

    public SmallLibrary() {
        this.inLibrary = new HashSet<>();
        this.checkedOut = new HashSet<>();
        checkRep();
    }

    // Assert the rep invariant
    private void checkRep() {
        assert this.inLibrary != null;
        assert this.checkedOut != null;
        Set<BookCopy> intersection = new HashSet<>(inLibrary);
        intersection.retainAll(checkedOut);
        assert intersection.isEmpty();
    }

    @Override
    public BookCopy buy(Book book) {
        BookCopy newCopy = new BookCopy(book);
        this.inLibrary.add(newCopy);
        checkRep();
        return newCopy;
    }

    @Override
    public void checkout(BookCopy copy) {
        if (!this.inLibrary.contains(copy)) {
            throw new IllegalArgumentException("This book copy is not available in the library");
        }

        this.inLibrary.remove(copy);
        this.checkedOut.add(copy);
        checkRep();
    }

    @Override
    public void checkin(BookCopy copy) {
        if (!this.checkedOut.contains(copy)) {
            throw new IllegalArgumentException("This book copy was not checked out of the library");
        }

        this.checkedOut.remove(copy);
        this.inLibrary.add(copy);
        checkRep();
    }

    @Override
    public boolean isAvailable(BookCopy copy) {
        return this.inLibrary.contains(copy);
    }

    @Override
    public Set<BookCopy> allCopies(Book book) {
        Set<BookCopy> result = new HashSet<>();
        result.addAll(this.inLibrary.stream().filter(copy -> copy.getBook().equals(book)).collect(Collectors.toSet()));
        result.addAll(this.checkedOut.stream().filter(copy -> copy.getBook().equals(book)).collect(Collectors.toSet()));
        return result;
    }

    @Override
    public Set<BookCopy> availableCopies(Book book) {
        return this.inLibrary.stream().filter(copy -> copy.getBook().equals(book)).collect(Collectors.toSet());
    }

    @Override
    public List<Book> find(String query) {
        Set<Book> foundInLibrary = findBooksInCopiesMatchingString(this.inLibrary, query);
        Set<Book> foundCheckedOut = findBooksInCopiesMatchingString(this.checkedOut, query);
        Set<Book> resultAsSet = new HashSet<>(foundInLibrary);
        resultAsSet.addAll(foundCheckedOut);
        List<Book> resultAsList = resultAsSet.stream().collect(Collectors.toList());
        resultAsList.sort(BOOK_SORTING_COMPARATOR);
        return resultAsList;
    }

    private Set<Book> findBooksInCopiesMatchingString(Set<BookCopy> copies, String query) {
        return copies.stream().filter(bookCopy -> bookCopy.getBook().getTitle().equals(query)
                || bookCopy.getBook().getAuthors().contains(query)).map(BookCopy::getBook).collect(Collectors.toSet());
    }

    @Override
    public void lose(BookCopy copy) {
        if (this.inLibrary.contains(copy)) {
            this.inLibrary.remove(copy);
            checkRep();
        } else if (this.checkedOut.contains(copy)) {
            this.checkedOut.remove(copy);
            checkRep();
        }
    }

    /* Copyright (c) 2016 MIT 6.005 course staff, all rights reserved.
     * Redistribution of original or derived work requires explicit permission.
     * Don't post any of this code on the web or to a public Github repository.
     */
}
