public

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single book in the library.
 * The state (checked out, reserved) is managed within this class.
 */
class Book {
    private final String title;
    private final String author;
    private final String isbn;
    private boolean isReserved;
    private boolean isCheckedOut;

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.isReserved = false;
        this.isCheckedOut = false;
    }

    // Getters for book properties
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public boolean isReserved() {
        return isReserved;
    }

    public boolean isCheckedOut() {
        return isCheckedOut;
    }

    /**
     * Checks out the book if it is not already checked out.
     * 
     * @return true if checkout was successful, false otherwise.
     */
    public boolean checkout() {
        if (this.isCheckedOut) {
            System.out.println("Error: Book '" + title + "' is already checked out.");
            return false;
        }
        this.isCheckedOut = true;
        return true;
    }

    /**
     * Reserves the book if it is not already reserved.
     * 
     * @return true if reservation was successful, false otherwise.
     */
    public boolean reserve() {
        if (this.isReserved) {
            System.out.println("Error: Book '" + title + "' is already reserved.");
            return false;
        }
        this.isReserved = true;
        return true;
    }

    /** Marks the book as returned (not checked out). */
    public void returnBook() {
        this.isCheckedOut = false;
        // Business rule: A returned book is also unreserved.
        this.isReserved = false;
    }

    /** Marks the book as no longer reserved. */
    public void unreserve() {
        this.isReserved = false;
    }

    @Override
    public String toString() {
        return "Book(title='" + title + "', author='" + author + "', isbn='" + isbn + "')";
    }
}

/**
 * Represents a library user.
 * Manages the books a user has checked out or reserved.
 */
class User {
    private final String userId;
    private final List<Book> checkedOutBooks;
    private final List<Book> reservedBooks;

    public User(String userId) {
        this.userId = userId;
        this.checkedOutBooks = new ArrayList<>();
        this.reservedBooks = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public List<Book> getCheckedOutBooks() {
        return checkedOutBooks;
    }

    public List<Book> getReservedBooks() {
        return reservedBooks;
    }

    /**
     * Allows a user to reserve a book.
     */
    public void reserve(Book book) {
        if (book.reserve()) {
            this.reservedBooks.add(book);
            System.out.println(userId + " successfully reserved '" + book.getTitle() + "'.");
        }
    }

    /**
     * Allows a user to check out a book.
     */
    public void checkoutBook(Book book) {
        if (book.checkout()) {
            this.checkedOutBooks.add(book);
            // If the user had this book reserved, the reservation is now fulfilled.
            if (this.reservedBooks.contains(book)) {
                this.reservedBooks.remove(book);
                book.unreserve(); // The book is no longer in a reserved state.
            }
            System.out.println(userId + " successfully checked out '" + book.getTitle() + "'.");
        }
    }

    /**
     * Allows a user to return a book.
     */
    public void returnBook(Book book) {
        if (this.checkedOutBooks.remove(book)) {
            book.returnBook();
            System.out.println(userId + " successfully returned '" + book.getTitle() + "'.");
        } else {
            System.out.println("Error: " + userId + " did not have '" + book.getTitle() + "' checked out.");
        }
    }
}

/**
 * Represents the library itself.
 * Manages the catalog of all available books.
 */
class Library {
    private final Map<String, Book> books; // Key: ISBN, Value: Book object

    public Library() {
        this.books = new HashMap<>();
    }

    /**
     * Adds a new book to the library's catalog.
     */
    public void addBook(Book book) {
        this.books.put(book.getIsbn(), book);
        System.out.println("Added to catalog: " + book.getTitle());
    }

    /**
     * Finds a book in the catalog by its ISBN.
     * 
     * @return The Book object if found, otherwise null.
     */
    public Book findBook(String isbn) {
        return this.books.get(isbn);
    }

    /**
     * Displays the status of all books in the library.
     */
    public void showAllBooks() {
        System.out.println("\n--- Library Catalog Status ---");
        if (books.isEmpty()) {
            System.out.println("The library has no books.");
            return;
        }
        for (Book book : this.books.values()) {
            System.out.printf(
                    "%-45s - Checked Out: %-5s, Reserved: %s%n",
                    book.toString(),
                    book.isCheckedOut(),
                    book.isReserved());
        }
        System.out.println("----------------------------\n");
    }
}

/**
 * Main class to run a demonstration of the Library System.
 */
public class LibrarySystem {
    public static void main(String[] args) {
        // Setup
        Library library = new Library();
        Book b1 = new Book("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565");
        Book b2 = new Book("To Kill a Mockingbird", "Harper Lee", "9780061120084");
        library.addBook(b1);
        library.addBook(b2);

        User user1 = new User("Alice");
        User user2 = new User("Bob");

        library.showAllBooks();

        // Execution
        user1.checkoutBook(b1); // Alice checks out The Great Gatsby
        user2.reserve(b1);      // Bob tries to reserve it, succeeds
        user2.checkoutBook(b1); // Bob tries to check it out, fails
        
        System.out.println();
        library.showAllBooks();
        
        user1.returnBook(b1);   // Alice returns The Great Gatsby
        user2.checkoutBook(b1); // Now Bob can check it out
        
        System.out.println();
        library.showAllBooks();
    }
}{

}
