import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class representing a single node in the Trie.
 */
class TrieNode {
    Map<Character, TrieNode> children;
    int freq;
    boolean isEndOfWord;
    String word;

    public TrieNode() {
        children = new HashMap<>();
        freq = 0;
        isEndOfWord = false;
        word = null;
    }
}

/**
 * This class represents a Trie (Prefix Tree) data structure.
 * It supports insertion, search, prefix counting, and autocomplete
 * functionality.
 */
public class Trie {
    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    /**
     * Inserts a word into the Trie.
     * 
     * @param word The word to insert.
     */
    public void insert(String word) {
        TrieNode current = root;
        for (char ch : word.toCharArray()) {
            // Get the child node for the character, or create it if it doesn't exist.
            current = current.children.computeIfAbsent(ch, c -> new TrieNode());
            // Increment the frequency count for the prefix.
            current.freq++;
        }
        // Mark the end of the word and store the complete word.
        current.isEndOfWord = true;
        current.word = word;
    }

    /**
     * Searches for an exact word in the Trie.
     * 
     * @param word The word to search for.
     * @return true if the word is found, false otherwise.
     */
    public boolean search(String word) {
        TrieNode node = findNode(word);
        // The word exists only if the node is found and it's marked as the end of a
        // word.
        return node != null && node.isEndOfWord;
    }

    /**
     * Checks if there is any word in the Trie that starts with the given prefix.
     * 
     * @param prefix The prefix to check.
     * @return true if there is a word with the given prefix, false otherwise.
     */
    public boolean startsWith(String prefix) {
        // It's a valid prefix if we can find a node corresponding to it.
        return findNode(prefix) != null;
    }

    /**
     * Counts the number of words in the Trie that start with the given prefix.
     * 
     * @param prefix The prefix to count words for.
     * @return The number of words starting with the prefix.
     */
    public int countWordsWithPrefix(String prefix) {
        TrieNode node = findNode(prefix);
        // The frequency is stored in the prefix's terminal node.
        return (node != null) ? node.freq : 0;
    }

    /**
     * Finds all words in the Trie that start with the given prefix.
     * 
     * @param prefix The prefix for which to find autocomplete suggestions.
     * @param limit  The maximum number of suggestions to return.
     * @return A list of words that start with the prefix.
     */
    public List<String> autocomplete(String prefix, int limit) {
        List<String> results = new ArrayList<>();
        TrieNode node = findNode(prefix);

        // If the prefix doesn't exist, return an empty list.
        if (node == null) {
            return results;
        }

        // Perform a Depth First Search from the prefix's node.
        dfs(node, results, limit);
        return results;
    }

    /**
     * Helper method to find the node corresponding to the end of a string (word or
     * prefix).
     * 
     * @param s The string to search for.
     * @return The TrieNode at the end of the string, or null if not found.
     */
    private TrieNode findNode(String s) {
        TrieNode current = root;
        for (char ch : s.toCharArray()) {
            if (!current.children.containsKey(ch)) {
                return null;
            }
            current = current.children.get(ch);
        }
        return current;
    }

    /**
     * Helper method to perform a Depth First Search (DFS) for autocomplete.
     * 
     * @param node    The starting node for the DFS.
     * @param results The list to store the found words.
     * @param limit   The maximum number of words to find.
     */
    private void dfs(TrieNode node, List<String> results, int limit) {
        // Stop if we have reached the limit.
        if (results.size() >= limit) {
            return;
        }

        // If the current node is the end of a word, add it to the results.
        if (node.isEndOfWord) {
            results.add(node.word);
        }

        // Recursively visit all children.
        for (char ch : node.children.keySet()) {
            if (results.size() < limit) {
                dfs(node.children.get(ch), results, limit);
            } else {
                break; // Stop if limit is reached during the loop.
            }
        }
    }

    /**
     * Main method to demonstrate the functionality of the Trie.
     */
    public static void main(String[] args) {
        Trie trie = new Trie();

        // Insert words
        trie.insert("apple");
        trie.insert("apply");
        trie.insert("application");
        trie.insert("ape");
        trie.insert("bat");
        trie.insert("ball");

        System.out.println("--- Testing Search ---");
        System.out.println("Search 'apple': " + trie.search("apple")); // true
        System.out.println("Search 'app': " + trie.search("app")); // false (it's a prefix, not a full word)
        System.out.println("Search 'batman': " + trie.search("batman")); // false

        System.out.println("\n--- Testing Prefix ---");
        System.out.println("Starts with 'app': " + trie.startsWith("app")); // true
        System.out.println("Starts with 'bal': " + trie.startsWith("bal")); // true
        System.out.println("Starts with 'cat': " + trie.startsWith("cat")); // false

        System.out.println("\n--- Testing Prefix Count ---");
        System.out.println("Words starting with 'app': " + trie.countWordsWithPrefix("app")); // 3
        System.out.println("Words starting with 'b': " + trie.countWordsWithPrefix("b")); // 2

        System.out.println("\n--- Testing Autocomplete ---");
        System.out.println("Autocomplete for 'ap' (limit 5): " + trie.autocomplete("ap", 5));
        System.out.println("Autocomplete for 'ball' (limit 5): " + trie.autocomplete("ball", 5));
        System.out.println("Autocomplete for 'z' (limit 5): " + trie.autocomplete("z", 5));
    }
}
