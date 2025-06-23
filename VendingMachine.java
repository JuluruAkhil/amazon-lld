import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Represents a product available in the vending machine.
 */
class Product {
    private final String code;
    private final String name;
    private final int price; // Price in cents to avoid floating-point issues
    private int quantity;

    public Product(String code, String name, int price, int quantity) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    // Setter for quantity
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void reduceQuantity() {
        if (this.quantity > 0) {
            this.quantity--;
        }
    }
}

/**
 * Manages the collection of products (inventory) in the vending machine.
 * This class acts as a repository for Product entities.
 */
class Inventory {
    private final Map<String, Product> products;

    public Inventory() {
        this.products = new HashMap<>();
    }

    public void addProduct(Product product) {
        products.put(product.getCode(), product);
    }

    public Product getProduct(String productCode) {
        return products.get(productCode);
    }

    public boolean isAvailable(String productCode) {
        Product product = getProduct(productCode);
        return product != null && product.getQuantity() > 0;
    }

    public Map<String, Product> getAllProducts() {
        return products;
    }
}

/**
 * The main VendingMachine class that acts as a facade for all operations.
 * It encapsulates the inventory and transaction logic.
 */
public class VendingMachine {
    private final Inventory inventory;
    private int currentBalance; // Balance in cents

    public VendingMachine() {
        this.inventory = new Inventory();
        this.currentBalance = 0;
        // Load initial products
        initializeProducts();
    }

    private void initializeProducts() {
        inventory.addProduct(new Product("A1", "Coke", 150, 10));
        inventory.addProduct(new Product("A2", "Pepsi", 150, 10));
        inventory.addProduct(new Product("B1", "Chips", 200, 5));
        inventory.addProduct(new Product("B2", "Pretzels", 175, 8));
        inventory.addProduct(new Product("C1", "Water", 125, 12));
    }

    public void displayProducts() {
        System.out.println("\n--- Available Products ---");
        System.out.println("Code | Product Name | Price   | Stock");
        System.out.println("------------------------------------");
        inventory.getAllProducts().values().forEach(p -> System.out.printf("%-4s | %-12s | $%-6.2f | %-5s\n",
                p.getCode(),
                p.getName(),
                p.getPrice() / 100.0,
                p.getQuantity() > 0 ? p.getQuantity() : "Out"));
        System.out.println("------------------------------------");
    }

    public void insertMoney(int cents) {
        if (cents > 0) {
            this.currentBalance += cents;
            System.out.printf("Current balance: $%.2f\n", this.currentBalance / 100.0);
        } else {
            System.out.println("Invalid amount. Please insert positive value.");
        }
    }

    public String selectProduct(String productCode) {
        Product product = inventory.getProduct(productCode);

        // 1. Check if product exists
        if (product == null) {
            return "Error: Invalid product code '" + productCode + "'.";
        }
        // 2. Check if product is in stock
        if (!inventory.isAvailable(productCode)) {
            return "Sorry, " + product.getName() + " is out of stock.";
        }
        // 3. Check if there are sufficient funds
        if (currentBalance < product.getPrice()) {
            double needed = (product.getPrice() - currentBalance) / 100.0;
            return String.format("Insufficient funds. Please insert $%.2f more.", needed);
        }

        // Process the purchase
        product.reduceQuantity();
        int change = currentBalance - product.getPrice();
        currentBalance = 0; // Reset balance for next transaction

        String dispenseMessage = "Dispensing " + product.getName() + ".";
        if (change > 0) {
            return dispenseMessage + String.format(" Your change is $%.2f.", change / 100.0);
        } else {
            return dispenseMessage + " Thank you!";
        }
    }

    public String getChange() {
        if (currentBalance <= 0) {
            return "No change to return.";
        }
        int changeToReturn = currentBalance;
        currentBalance = 0;
        return String.format("Returning change: $%.2f", changeToReturn / 100.0);
    }

    // Admin function to refill stock
    public void refillProduct(String productCode, int quantity) {
        Product product = inventory.getProduct(productCode);
        if (product != null && quantity > 0) {
            product.setQuantity(product.getQuantity() + quantity);
            System.out.println("Refilled " + product.getName() + ". New stock: " + product.getQuantity());
        } else {
            System.out.println("Error: Could not refill product " + productCode);
        }
    }

    /**
     * Main method to run an interactive Vending Machine simulation.
     */
    public static void main(String[] args) {
        VendingMachine vm = new VendingMachine();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            vm.displayProducts();
            System.out.printf("Current Balance: $%.2f\n", vm.currentBalance / 100.0);
            System.out.println(
                    "\nOptions: (1) Insert Money, (2) Select Product, (3) Get Change, (4) Refill (Admin), (5) Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter amount in cents (e.g., 100 for $1.00): ");
                    try {
                        int cents = Integer.parseInt(scanner.nextLine());
                        vm.insertMoney(cents);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                    }
                    break;
                case "2":
                    System.out.print("Enter product code (e.g., A1): ");
                    String code = scanner.nextLine().toUpperCase();
                    System.out.println(vm.selectProduct(code));
                    break;
                case "3":
                    System.out.println(vm.getChange());
                    break;
                case "4":
                    System.out.print("Enter product code to refill: ");
                    String refillCode = scanner.nextLine().toUpperCase();
                    System.out.print("Enter quantity to add: ");
                    try {
                        int qty = Integer.parseInt(scanner.nextLine());
                        vm.refillProduct(refillCode, qty);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                    }
                    break;
                case "5":
                    System.out.println("Thank you for using the vending machine. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
