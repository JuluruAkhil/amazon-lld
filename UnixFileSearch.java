import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// --- Core File System Structure (Composite Pattern) ---

/**
 * Abstract base class for files and directories.
 * This is the 'Component' in the Composite design pattern.
 */
abstract class Entry {
    protected String name;

    public Entry(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract int getSize();

    public abstract boolean isDirectory();
}

/**
 * Represents a file, which is a 'Leaf' in the Composite pattern.
 */
class File extends Entry {
    private String content;

    public File(String name, String content) {
        super(name);
        this.content = content;
    }

    @Override
    public int getSize() {
        return content.length();
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    public String getExtension() {
        int dotIndex = name.lastIndexOf('.');
        return (dotIndex == -1) ? "" : name.substring(dotIndex + 1);
    }

    @Override
    public String toString() {
        return "File [Name=" + name + ", Size=" + getSize() + "]";
    }
}

/**
 * Represents a directory, which is a 'Composite' in the Composite pattern.
 * It can contain other Entries (Files or sub-Directories).
 */
class Directory extends Entry {
    private final List<Entry> entries = new ArrayList<>();

    public Directory(String name) {
        super(name);
    }

    @Override
    public int getSize() {
        return entries.stream().mapToInt(Entry::getSize).sum();
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
    }

    public List<Entry> getEntries() {
        return entries;
    }
}

// --- Search Logic (Refactored with Composite/Strategy Pattern) ---

/**
 * The 'Component' and 'Strategy' interface for all file filters.
 */
interface FileFilter {
    boolean matches(File file);
}

// --- Leaf Filters (Simple Strategies) ---

class ExtensionFilter implements FileFilter {
    private final String extension;

    public ExtensionFilter(String extension) {
        this.extension = extension;
    }

    @Override
    public boolean matches(File file) {
        return file.getExtension().equalsIgnoreCase(this.extension);
    }
}

class MinSizeFilter implements FileFilter {
    private final int minSize;

    public MinSizeFilter(int minSize) {
        this.minSize = minSize;
    }

    @Override
    public boolean matches(File file) {
        return file.getSize() >= this.minSize;
    }
}

class NameFilter implements FileFilter {
    private final String name;

    public NameFilter(String name) {
        this.name = name;
    }

    @Override
    public boolean matches(File file) {
        return file.getName().equalsIgnoreCase(this.name);
    }
}

// --- Composite Filters ---

/**
 * A composite filter that combines other filters with an AND logic.
 */
class AndFilter implements FileFilter {
    private final List<FileFilter> filters;

    public AndFilter(FileFilter... filters) {
        this.filters = List.of(filters);
    }

    @Override
    public boolean matches(File file) {
        return filters.stream().allMatch(filter -> filter.matches(file));
    }
}

/**
 * A composite filter that combines other filters with an OR logic.
 */
class OrFilter implements FileFilter {
    private final List<FileFilter> filters;

    public OrFilter(FileFilter... filters) {
        this.filters = List.of(filters);
    }

    @Override
    public boolean matches(File file) {
        return filters.stream().anyMatch(filter -> filter.matches(file));
    }
}

/**
 * The main search engine that traverses the directory structure and applies
 * filters.
 */
class FileSearcher {
    /**
     * Searches the directory structure using BFS and applies a filter.
     *
     * @param root   The starting directory for the search.
     * @param filter The filter to apply.
     * @return A list of files that match the filter.
     */
    public List<File> search(Directory root, FileFilter filter) {
        List<File> results = new ArrayList<>();
        Deque<Directory> queue = new ArrayDeque<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Directory currentDir = queue.poll();
            for (Entry entry : currentDir.getEntries()) {
                if (entry.isDirectory()) {
                    queue.add((Directory) entry);
                } else {
                    File file = (File) entry;
                    if (filter.matches(file)) {
                        results.add(file);
                    }
                }
            }
        }
        return results;
    }
}

/**
 * Main class to demonstrate the simplified Unix File Search system.
 */
public class UnixFileSearch {
    public static void main(String[] args) {
        // Build a sample directory structure
        Directory root = new Directory("root");
        Directory documents = new Directory("documents");
        Directory photos = new Directory("photos");
        root.addEntry(documents);
        root.addEntry(photos);
        root.addEntry(new File("config.sys", "system-config-content"));
        root.addEntry(new File("archive.zip", "zip-file-with-lot-of-content"));

        documents.addEntry(new File("report.docx", "This is the annual report content. It's quite long."));
        documents.addEntry(new File("notes.txt", "Some text notes."));
        documents.addEntry(new File("contract.docx", "A short contract."));

        photos.addEntry(new File("summer.jpeg", "Summer vacation photo."));
        photos.addEntry(new File("winter.jpeg", "Winter wonderland."));
        photos.addEntry(new File("project.md", "Project documentation file."));

        FileSearcher searcher = new FileSearcher();

        // --- Demo Search ---
        System.out.println("--- Query 1: Finding all '.jpeg' files ---");
        FileFilter jpegFilter = new ExtensionFilter("jpeg");
        List<File> jpegFiles = searcher.search(root, jpegFilter);
        jpegFiles.forEach(System.out::println);

        System.out.println("\n--- Query 2: Finding all '.docx' files larger than 20 bytes ---");
        FileFilter bigDocxFilter = new AndFilter(
                new ExtensionFilter("docx"),
                new MinSizeFilter(20));
        List<File> bigDocxFiles = searcher.search(root, bigDocxFilter);
        bigDocxFiles.forEach(System.out::println);

        System.out.println("\n--- Query 3: Finding files that are '.txt' OR named 'archive.zip' ---");
        FileFilter txtOrZipFilter = new OrFilter(
                new ExtensionFilter("txt"),
                new NameFilter("archive.zip"));
        List<File> txtOrZipFiles = searcher.search(root, txtOrZipFilter);
        txtOrZipFiles.forEach(System.out::println);

        System.out.println("\n--- Query 4: Complex nested search ---");
        System.out
                .println("Finding ('.jpeg' files in 'photos') OR ('.docx' files larger than 25 bytes in 'documents')");

        // Note: This simplified model doesn't have path-awareness in filters.
        // A more advanced version might include the file's path in the `matches`
        // method.
        // For this example, we will apply filters to the whole tree, as the logic
        // implies.

        FileFilter jpegInPhotos = new AndFilter(
                new ExtensionFilter("jpeg")
        // We cannot filter by path "photos" with the current design.
        // The search is global from the root.
        );

        FileFilter bigDocsInDocuments = new AndFilter(
                new ExtensionFilter("docx"),
                new MinSizeFilter(25)
        // Same path limitation as above.
        );

        FileFilter complexFilter = new OrFilter(jpegInPhotos, bigDocsInDocuments);
        List<File> complexResults = searcher.search(root, complexFilter);
        complexResults.forEach(System.out::println);
    }

    // Helper to print file details
    private static void printResults(List<File> files) {
        if (files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            files.forEach(f -> System.out.println(" - " + f.getName() + " (Size: " + f.getSize() + ")"));
        }
    }
}
