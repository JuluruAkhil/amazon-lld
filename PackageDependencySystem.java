import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a single package with its name and a set of dependencies.
 */
class Package {
    private final String name;
    // The set of package names that this package depends on.
    private final Set<String> dependencies;

    public Package(String name) {
        this.name = name;
        this.dependencies = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public void addDependency(String dependencyName) {
        this.dependencies.add(dependencyName);
    }

    public void removeDependency(String dependencyName) {
        this.dependencies.remove(dependencyName);
    }
}

/**
 * Core logic for managing packages and calculating build order using
 * topological sort.
 */
class PackageManager {
    private final Map<String, Package> packages;

    public PackageManager() {
        this.packages = new HashMap<>();
    }

    public void addPackage(String name) {
        packages.putIfAbsent(name, new Package(name));
    }

    public Package getPackage(String name) {
        return packages.get(name);
    }

    /**
     * Calculates a valid build order using topological sort (Kahn's algorithm).
     * 
     * @return A list of package names in a valid build order. Returns an empty list
     *         if a cycle is detected.
     */
    public List<String> getBuildOrder() {
        // Map to store the in-degree of each package (how many packages depend on it).
        Map<String, Integer> inDegree = new HashMap<>();
        // Adjacency list to represent the graph (dependency -> list of packages that
        // depend on it).
        Map<String, List<String>> adj = new HashMap<>();

        // Initialize in-degree and adjacency list for all packages.
        for (String name : packages.keySet()) {
            inDegree.put(name, 0);
            adj.put(name, new ArrayList<>());
        }

        // Populate in-degree and adjacency list based on dependencies.
        for (Package pkg : packages.values()) {
            for (String depName : pkg.getDependencies()) {
                inDegree.put(pkg.getName(), inDegree.get(pkg.getName()) + 1);
                adj.get(depName).add(pkg.getName());
            }
        }

        // Queue for nodes with an in-degree of 0 (packages with no dependencies).
        Deque<String> queue = new ArrayDeque<>();
        for (String name : inDegree.keySet()) {
            if (inDegree.get(name) == 0) {
                queue.add(name);
            }
        }

        List<String> buildOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            buildOrder.add(current);

            // Decrease the in-degree of all packages that depend on the current package.
            for (String neighbor : adj.get(current)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                // If a neighbor's in-degree becomes 0, add it to the queue.
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        // If the build order doesn't include all packages, a cycle exists.
        if (buildOrder.size() != packages.size()) {
            return Collections.emptyList(); // Cycle detected
        }

        return buildOrder;
    }

    /**
     * Validates if the current dependency graph is a valid DAG (Directed Acyclic
     * Graph).
     * 
     * @return true if there are no cycles, false otherwise.
     */
    public boolean validateDependencies() {
        return getBuildOrder().size() == packages.size();
    }
}

/**
 * Facade that provides a simplified interface for build operations,
 * including automatic cycle detection and rollback.
 */
class BuildSystem {
    private final PackageManager manager;

    public BuildSystem() {
        this.manager = new PackageManager();
    }

    public void register(String packageName) {
        manager.addPackage(packageName);
    }

    /**
     * Adds a dependency relationship and checks for cycles.
     * If a cycle is created, the operation is rolled back.
     * 
     * @param packageName The package that will have a new dependency.
     * @param dependsOn   The package it will depend on.
     * @return true if the dependency was added successfully, false if it created a
     *         cycle.
     */
    public boolean addDependency(String packageName, String dependsOn) {
        Package pkg = manager.getPackage(packageName);
        Package dep = manager.getPackage(dependsOn);

        if (pkg == null || dep == null || packageName.equals(dependsOn)) {
            return false; // Invalid packages or self-dependency
        }

        pkg.addDependency(dependsOn);

        // Check if this new dependency created a cycle.
        if (!manager.validateDependencies()) {
            // If a cycle is found, roll back the change.
            pkg.removeDependency(dependsOn);
            System.out.println("Error: Adding dependency '" + dependsOn + "' to '" + packageName
                    + "' creates a cycle. Operation aborted.");
            return false;
        }
        return true;
    }

    public List<String> getBuildOrder() {
        return manager.getBuildOrder();
    }
}

/**
 * Main class to demonstrate the Package Dependency System.
 */
public class PackageDependencySystem {
    public static void main(String[] args) {
        // Test 1: Simple dependency chain
        System.out.println("--- Test 1: Simple Chain ---");
        BuildSystem build1 = new BuildSystem();
        for (String pkg : new String[] { "Database", "Server", "UI" }) {
            build1.register(pkg);
        }
        build1.addDependency("UI", "Server");
        build1.addDependency("Server", "Database");
        System.out.println("Build order: " + build1.getBuildOrder());

        // Test 2: Complex dependencies
        System.out.println("\n--- Test 2: Complex Dependencies ---");
        BuildSystem build2 = new BuildSystem();
        String[] packages = { "Core", "Utils", "DataLayer", "BusinessLogic", "API", "WebApp" };
        for (String pkg : packages) {
            build2.register(pkg);
        }
        build2.addDependency("DataLayer", "Core");
        build2.addDependency("DataLayer", "Utils");
        build2.addDependency("BusinessLogic", "DataLayer");
        build2.addDependency("BusinessLogic", "Utils");
        build2.addDependency("API", "BusinessLogic");
        build2.addDependency("WebApp", "API");
        build2.addDependency("WebApp", "Utils");
        System.out.println("Build order: " + build2.getBuildOrder());

        // Test 3: Cycle detection and rollback
        System.out.println("\n--- Test 3: Cycle Detection ---");
        BuildSystem build3 = new BuildSystem();
        for (String pkg : new String[] { "A", "B", "C" }) {
            build3.register(pkg);
        }
        System.out.println("A depends on B: " + (build3.addDependency("A", "B") ? "Success" : "Failed"));
        System.out.println("B depends on C: " + (build3.addDependency("B", "C") ? "Success" : "Failed"));
        System.out.println(
                "Trying C depends on A (creates cycle): " + (build3.addDependency("C", "A") ? "Success" : "Failed"));

        List<String> order = build3.getBuildOrder();
        System.out.println(
                "Build order after cycle attempt: " + (order.isEmpty() ? "No valid order (cycle exists)" : order));
        System.out.println("Final valid build order: " + build3.getBuildOrder());
    }
}
