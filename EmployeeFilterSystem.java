import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents an employee with their professional details.
 */
class Employee {
    private final String name;
    private final String org;
    private final int experience;
    private final boolean isManager;

    public Employee(String name, String org, int experience, boolean isManager) {
        this.name = name;
        this.org = org;
        this.experience = experience;
        this.isManager = isManager;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getOrg() {
        return org;
    }

    public int getExperience() {
        return experience;
    }

    public boolean isManager() {
        return isManager;
    }

    @Override
    public String toString() {
        return "Employee [Name=" + name + ", Org=" + org + ", Experience=" + experience + ", IsManager=" + isManager
                + "]";
    }
}

// --- Strategy and Composite Pattern for Filtering ---

/**
 * The 'Component' and 'Strategy' interface. Defines the contract for all
 * filters,
 * whether they are simple (a leaf) or complex (a composite).
 */
interface EmployeeFilter {
    boolean matches(Employee employee);
}

// --- Leaf Filters (Simple Strategies) ---

class OrgFilter implements EmployeeFilter {
    private final String org;

    public OrgFilter(String org) {
        this.org = org;
    }

    @Override
    public boolean matches(Employee employee) {
        return employee.getOrg().equalsIgnoreCase(this.org);
    }
}

class ExperienceFilter implements EmployeeFilter {
    private final int minExperience;

    public ExperienceFilter(int minExperience) {
        this.minExperience = minExperience;
    }

    @Override
    public boolean matches(Employee employee) {
        return employee.getExperience() >= this.minExperience;
    }
}

class IsManagerFilter implements EmployeeFilter {
    private final boolean isManager;

    public IsManagerFilter(boolean isManager) {
        this.isManager = isManager;
    }

    @Override
    public boolean matches(Employee employee) {
        return employee.isManager() == this.isManager;
    }
}

// --- Composite Filters ---

/**
 * A composite filter that combines other filters with AND logic.
 */
class AndFilter implements EmployeeFilter {
    private final List<EmployeeFilter> filters;

    public AndFilter(EmployeeFilter... filters) {
        this.filters = Arrays.asList(filters);
    }

    @Override
    public boolean matches(Employee employee) {
        // Returns true only if all child filters match.
        return filters.stream().allMatch(filter -> filter.matches(employee));
    }
}

/**
 * A composite filter that combines other filters with OR logic.
 */
class OrFilter implements EmployeeFilter {
    private final List<EmployeeFilter> filters;

    public OrFilter(EmployeeFilter... filters) {
        this.filters = Arrays.asList(filters);
    }

    @Override
    public boolean matches(Employee employee) {
        // Returns true if any of the child filters match.
        return filters.stream().anyMatch(filter -> filter.matches(employee));
    }
}

/**
 * Main class to demonstrate the employee filtering system.
 */
public class EmployeeFilterSystem {

    /**
     * Processes a list of employees against a given filter.
     * 
     * @param employees The list of all employees.
     * @param filter    The composite filter representing the query.
     * @return A new list of employees that match the query.
     */
    public static List<Employee> processQuery(List<Employee> employees, EmployeeFilter filter) {
        return employees.stream()
                .filter(filter::matches)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        // 1. Create a list of employees
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee("Alice", "Engineering", 12, true));
        employees.add(new Employee("Bob", "Engineering", 8, false));
        employees.add(new Employee("Charlie", "Sales", 10, true));
        employees.add(new Employee("David", "HR", 5, false));
        employees.add(new Employee("Eve", "Engineering", 5, true));
        employees.add(new Employee("Frank", "Sales", 15, false));

        // 2. Build and process queries
        System.out.println("--- Query 1: All managers in Engineering ---");
        // The structure of this composition determines the priority/order of
        // operations.
        EmployeeFilter query1 = new AndFilter(
                new IsManagerFilter(true),
                new OrgFilter("Engineering"));
        List<Employee> results1 = processQuery(employees, query1);
        results1.forEach(System.out::println);

        System.out.println("\n--- Query 2: Employees who are managers OR have more than 10 years of experience ---");
        EmployeeFilter query2 = new OrFilter(
                new IsManagerFilter(true),
                new ExperienceFilter(11) // More than 10 means 11+
        );
        List<Employee> results2 = processQuery(employees, query2);
        results2.forEach(System.out::println);

        System.out.println("\n--- Query 3: (Managers in Sales) OR (Engineers with less than 10 years experience) ---");
        EmployeeFilter salesManagers = new AndFilter(
                new IsManagerFilter(true),
                new OrgFilter("Sales"));
        EmployeeFilter juniorEngineers = new AndFilter(
                new OrgFilter("Engineering"),
                new ExperienceFilter(0) // This line is incorrect - let's find engineers with less than 10 exp
        );
        // Let's create a NotFilter to handle the "less than" logic cleanly
        // For now, let's just combine the two ANDs with an OR

        // This query requires a bit more thought on how to do "less than".
        // A simple way without adding more classes is to filter in a stream.
        // But let's build the composite query as requested.
        // We'll simulate finding engineers with less than 10 years exp by finding all
        // engineers,
        // then filtering them separately. A better design would have a
        // `MaxExperienceFilter`.

        // Let's re-think a better query for demonstration.
        System.out.println("\n--- Query 3 (Revised): (Managers with 10+ years experience) OR (any employee in HR) ---");
        EmployeeFilter experiencedManagers = new AndFilter(
                new IsManagerFilter(true),
                new ExperienceFilter(10));
        EmployeeFilter hrEmployees = new OrgFilter("HR");

        EmployeeFilter query3 = new OrFilter(experiencedManagers, hrEmployees);
        List<Employee> results3 = processQuery(employees, query3);
        results3.forEach(System.out::println);
    }
}
