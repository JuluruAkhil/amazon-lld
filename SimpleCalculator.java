import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// --- Strategy Pattern ---

/**
 * The 'Strategy' interface. Defines the contract for all mathematical
 * operations.
 * Any class that performs a calculation will implement this interface.
 */
interface Operation {
    /**
     * Executes the mathematical operation.
     * 
     * @param a The first operand.
     * @param b The second operand.
     * @return The result of the operation.
     */
    double execute(double a, double b);
}

// --- Concrete Strategies ---

/** A concrete strategy for addition. */
class AddOperation implements Operation {
    @Override
    public double execute(double a, double b) {
        return a + b;
    }
}

/** A concrete strategy for subtraction. */
class SubtractOperation implements Operation {
    @Override
    public double execute(double a, double b) {
        return a - b;
    }
}

/** A concrete strategy for multiplication. */
class MultiplyOperation implements Operation {
    @Override
    public double execute(double a, double b) {
        return a * b;
    }
}

/** A concrete strategy for division. */
class DivideOperation implements Operation {
    @Override
    public double execute(double a, double b) {
        if (b == 0) {
            System.out.println("Error: Cannot divide by zero.");
            return Double.NaN; // Not a Number, to indicate an error
        }
        return a / b;
    }
}

/** A concrete strategy for the power operation. */
class PowerOperation implements Operation {
    @Override
    public double execute(double a, double b) {
        return Math.pow(a, b);
    }
}

// --- Context Class ---

/**
 * The 'Context' class. It holds a reference to a strategy object and uses it
 * to perform calculations without knowing the details of the specific
 * operation.
 */
class Calculator {
    private Operation operation;

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public double calculate(double a, double b) {
        if (operation == null) {
            System.out.println("Error: No operation selected.");
            return Double.NaN;
        }
        return operation.execute(a, b);
    }
}

/**
 * Main class to run an interactive calculator from the console.
 */
public class SimpleCalculator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Calculator calculator = new Calculator();

        // Use a map to easily select the operation strategy based on user input
        Map<String, Operation> operations = new HashMap<>();
        operations.put("+", new AddOperation());
        operations.put("-", new SubtractOperation());
        operations.put("*", new MultiplyOperation());
        operations.put("/", new DivideOperation());
        operations.put("pow", new PowerOperation());

        while (true) {
            System.out.println("\n--- Simple Calculator ---");
            System.out.print("Enter first number: ");
            double num1 = scanner.nextDouble();

            System.out.print("Enter operation (+, -, *, /, pow) or 'exit': ");
            String opSymbol = scanner.next();

            if (opSymbol.equalsIgnoreCase("exit")) {
                System.out.println("Exiting calculator.");
                break;
            }

            System.out.print("Enter second number: ");
            double num2 = scanner.nextDouble();

            Operation selectedOperation = operations.get(opSymbol);
            if (selectedOperation == null) {
                System.out.println("Invalid operation. Please try again.");
                continue;
            }

            calculator.setOperation(selectedOperation);
            double result = calculator.calculate(num1, num2);

            if (!Double.isNaN(result)) {
                System.out.println("Result: " + num1 + " " + opSymbol + " " + num2 + " = " + result);
            }
        }
        scanner.close();
    }
}
