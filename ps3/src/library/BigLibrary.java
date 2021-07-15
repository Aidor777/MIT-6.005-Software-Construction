package library;

import java.util.*;

/**
 * BigLibrary represents a large collection of books that might be held by a city or
 * university library system -- millions of books.
 * <p>
 * In particular, every operation needs to run faster than linear time (as a function of the number of books
 * in the library).
 */
public class BigLibrary implements Library {

    // Rep
    private final Map<Book, Set<BookCopy>> availableCopiesByBook;

    private final Map<Book, Set<BookCopy>> checkedOutCopiesByBook;

    private final SortedSet<Book> allBooks;

    // Rep invariant
    // Any copy found in availableCopiesByBook should not be found in checkedOutCopiesByBook, and vice-versa
    // If a new book is available, it should be present in checked out copies with an empty set, and vice-versa
    // allBooks contains all books that have at least one copy either available or checked out, and nothing else

    // Abstraction function
    // Represents all books in a library in allBooks
    // If a copy is available, it will be found in the value of availableCopiesByBook corresponding to its book (Map key)
    // If a copy is checked out, it will be found in the value of checkedOutCopiesByBook corresponding to its book (Map key)

    // Safety from rep exposure argument
    // All fields are private and final. Whenever a set of book copies is exposed, it is stored in a new set to avoid mutation.
    // Any time a set is mutated inside the class, the rep invariant is checked.

    private static final Comparator<List<String>> BOOK_AUTHORS_COMPARATOR = (x, y) -> {
        int result = 0;
        int minSize = Math.min(x.size(), y.size());
        for (int i = 0; i < minSize; i++) {
            result = x.get(i).compareTo(y.get(i));
            if (result != 0) {
                return result;
            }
        }
        if (x.size() < y.size()) {
            return 1;
        } else if (x.size() > y.size()) {
            return -1;
        }
        return result;
    };

    private static final Comparator<Book> BOOK_SORTING_COMPARATOR = Comparator.comparing(Book::getTitle)
            .thenComparing(Book::getAuthors, BOOK_AUTHORS_COMPARATOR)
            .thenComparing(Comparator.comparing(Book::getYear).reversed());

    public BigLibrary() {
        this.availableCopiesByBook = new HashMap<>();
        this.checkedOutCopiesByBook = new HashMap<>();
        this.allBooks = new TreeSet<>(BOOK_SORTING_COMPARATOR);
        checkRep();
    }

    // Assert the rep invariant
    private void checkRep() {
        assert this.availableCopiesByBook != null;
        assert this.checkedOutCopiesByBook != null;
        assert this.allBooks != null;
        assert this.availableCopiesByBook.keySet().size() == this.checkedOutCopiesByBook.keySet().size();
        assert this.availableCopiesByBook.keySet().size() == this.allBooks.size();

        for (Book book : this.availableCopiesByBook.keySet()) {
            assert this.allBooks.contains(book);
            Set<BookCopy> availableCopies = this.availableCopiesByBook.get(book);
            assert availableCopies != null;
            Set<BookCopy> checkedOutCopies = this.checkedOutCopiesByBook.get(book);
            assert checkedOutCopies != null;
            Set<BookCopy> intersection = new HashSet<>(availableCopies);
            intersection.retainAll(checkedOutCopies);
            assert intersection.isEmpty();
        }
    }

    @Override
    public BookCopy buy(Book book) {
        BookCopy newCopy = new BookCopy(book);
        Set<BookCopy> availableCopies = this.availableCopiesByBook.computeIfAbsent(book, _book -> new HashSet<>());
        this.checkedOutCopiesByBook.putIfAbsent(book, new HashSet<>());
        this.allBooks.add(book);
        availableCopies.add(newCopy);
        checkRep();
        return newCopy;
    }

    @Override
    public void checkout(BookCopy copy) {
        Book book = copy.getBook();
        if (!this.availableCopiesByBook.containsKey(book) || !this.availableCopiesByBook.get(book).contains(copy)) {
            throw new IllegalArgumentException("This book copy is not available in the library");
        }

        this.availableCopiesByBook.get(book).remove(copy);
        this.checkedOutCopiesByBook.get(book).add(copy);
        checkRep();
    }

    @Override
    public void checkin(BookCopy copy) {
        Book book = copy.getBook();
        if (!this.checkedOutCopiesByBook.containsKey(book) || !this.checkedOutCopiesByBook.get(book).contains(copy)) {
            throw new IllegalArgumentException("This book copy was not checked out of the library");
        }

        this.checkedOutCopiesByBook.get(book).remove(copy);
        this.availableCopiesByBook.get(book).add(copy);
        checkRep();
    }

    @Override
    public Set<BookCopy> allCopies(Book book) {
        Set<BookCopy> result = new HashSet<>();
        result.addAll(this.availableCopiesByBook.getOrDefault(book, new HashSet<>()));
        result.addAll(this.checkedOutCopiesByBook.getOrDefault(book, new HashSet<>()));
        return result;
    }

    @Override
    public Set<BookCopy> availableCopies(Book book) {
        return new HashSet<>(this.availableCopiesByBook.getOrDefault(book, new HashSet<>()));
    }

    @Override
    public boolean isAvailable(BookCopy copy) {
        return this.availableCopiesByBook.getOrDefault(copy.getBook(), new HashSet<>()).contains(copy);
    }

    @Override
    public List<Book> find(String query) {
        List<Book> result = new LinkedList<>();
        for (Book book : this.allBooks) {
            if (bookMatchesSearchQuery(book, query)) {
                result.add(book);
            }
        }
        return result;
    }

    private boolean bookMatchesSearchQuery(Book book, String query) {
        return book.getTitle().equals(query) || book.getAuthors().contains(query);
    }

    @Override
    public void lose(BookCopy copy) {
        Book book = copy.getBook();
        Set<BookCopy> availableCopies = this.availableCopiesByBook.getOrDefault(book, new HashSet<>());
        Set<BookCopy> checkedOutCopies = this.checkedOutCopiesByBook.getOrDefault(book, new HashSet<>());
        if (availableCopies.contains(copy)) {
            availableCopies.remove(copy);
        } else if (checkedOutCopies.contains(copy)) {
            checkedOutCopies.remove(copy);
        }
        if (availableCopies.isEmpty() && checkedOutCopies.isEmpty()) {
            this.availableCopiesByBook.remove(book);
            this.checkedOutCopiesByBook.remove(book);
            this.allBooks.remove(book);
        }
        checkRep();
    }

    /* Copyright (c) 2016 MIT 6.005 course staff, all rights reserved.
     * Redistribution of original or derived work requires explicit permission.
     * Don't post any of this code on the web or to a public Github repository.
     */

}
