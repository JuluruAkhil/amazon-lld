import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single item with a name and a price.
 * This is a simple data class.
 */
class Item {
    private String name;
    private double price;

    public Item(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name + ": $" + String.format("%.2f", price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Item item = (Item) o;
        return Double.compare(item.price, price) == 0 && Objects.equals(name, item.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price);
    }
}

/**
 * Interface for applying a discount to a list of items.
 * This allows for different types of discount calculations.
 */
interface Discount {
    /**
     * Calculates the discount amount for a given list of items.
     * 
     * @param items        The list of items in the cart.
     * @param currentTotal The current total price of the items before this
     *                     discount.
     * @return The calculated discount amount.
     */
    double applyDiscount(List<Item> items, double currentTotal);
}

/**
 * A discount that applies a certain percentage off the total price.
 */
class PercentageDiscount implements Discount {
    private double percentage;

    public PercentageDiscount(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100.");
        }
        this.percentage = percentage;
    }

    @Override
    public double applyDiscount(List<Item> items, double currentTotal) {
        return currentTotal * (percentage / 100.0);
    }
}

/**
 * A discount that gives a fixed amount off if the total exceeds a certain
 * minimum.
 */
class FixedAmountDiscount implements Discount {
    private double discountAmount;
    private double minimumTotal;

    public FixedAmountDiscount(double discountAmount, double minimumTotal) {
        this.discountAmount = discountAmount;
        this.minimumTotal = minimumTotal;
    }

    @Override
    public double applyDiscount(List<Item> items, double currentTotal) {
        if (currentTotal >= minimumTotal) {
            return discountAmount;
        }
        return 0.0;
    }
}

/**
 * A "Buy One, Get One Free" discount for a specific item.
 * For every two of the specified item, one is free.
 */
class BuyOneGetOneFreeDiscount implements Discount {
    private String itemName;

    public BuyOneGetOneFreeDiscount(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public double applyDiscount(List<Item> items, double currentTotal) {
        int count = 0;
        double itemPrice = 0;

        for (Item item : items) {
            if (item.getName().equals(itemName)) {
                count++;
                itemPrice = item.getPrice();
            }
        }

        int freeItems = count / 2;
        return freeItems * itemPrice;
    }
}

/**
 * Manages a collection of items, applies discounts, and calculates the final
 * price.
 */
class ShoppingCart {
    private List<Item> items;
    private List<Discount> discounts;

    public ShoppingCart() {
        this.items = new ArrayList<>();
        this.discounts = new ArrayList<>();
    }

    /**
     * Adds an item to the cart.
     * 
     * @param item The item to add.
     */
    public void addItem(Item item) {
        items.add(item);
        System.out.println("Added to cart: " + item);
    }

    /**
     * Removes an item from the cart.
     * 
     * @param item The item to remove.
     */
    public void removeItem(Item item) {
        if (items.remove(item)) {
            System.out.println("Removed from cart: " + item);
        } else {
            System.out.println("Item not found in cart: " + item);
        }
    }

    /**
     * Adds a discount rule to the cart.
     * 
     * @param discount The discount to add.
     */
    public void addDiscount(Discount discount) {
        discounts.add(discount);
    }

    /**
     * Calculates the final price after applying all discounts.
     * 
     * @return The final total price.
     */
    public double calculateFinalPrice() {
        System.out.println("\n--- Calculating Final Price ---");
        double subtotal = 0;
        for (Item item : items) {
            subtotal += item.getPrice();
        }
        System.out.printf("Subtotal: $%.2f\n", subtotal);

        double totalDiscount = 0;
        double currentTotal = subtotal;

        // Apply BOGO discounts first as they affect the item count/value
        for (Discount discount : discounts) {
            if (discount instanceof BuyOneGetOneFreeDiscount) {
                double discountAmount = discount.applyDiscount(items, currentTotal);
                if (discountAmount > 0) {
                    System.out.printf("Applied BOGO Discount: -$%.2f\n", discountAmount);
                    totalDiscount += discountAmount;
                }
            }
        }

        currentTotal = subtotal - totalDiscount;

        // Apply other discounts on the potentially reduced total
        for (Discount discount : discounts) {
            if (!(discount instanceof BuyOneGetOneFreeDiscount)) {
                double discountAmount = discount.applyDiscount(items, currentTotal);
                if (discountAmount > 0) {
                    System.out.printf("Applied Discount: -$%.2f\n", discountAmount);
                    totalDiscount += discountAmount;
                    currentTotal -= discountAmount; // Update running total for subsequent discounts
                }
            }
        }

        double finalPrice = subtotal - totalDiscount;

        System.out.printf("Total Discounts: -$%.2f\n", totalDiscount);
        System.out.printf("Final Price: $%.2f\n", finalPrice);
        System.out.println("-----------------------------");

        return finalPrice;
    }

    public void displayCart() {
        System.out.println("\n--- Items in Your Cart ---");
        if (items.isEmpty()) {
            System.out.println("Your cart is empty.");
        } else {
            Map<String, Integer> itemCounts = new HashMap<>();
            for (Item item : items) {
                itemCounts.put(item.getName(), itemCounts.getOrDefault(item.getName(), 0) + 1);
            }

            for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
                System.out.println("- " + entry.getKey() + " (x" + entry.getValue() + ")");
            }
        }
        System.out.println("--------------------------");
    }
}

/**
 * Main class to run a demonstration of the ShoppingCart.
 */
public class ShoppingCartSystem {
    public static void main(String[] args) {
        // --- SCENARIO 1: Multiple Discounts ---
        System.out.println("||| SCENARIO 1: BOGO, Percentage, and Fixed Amount Discounts |||");
        ShoppingCart cart1 = new ShoppingCart();

        // Initial Setup
        Item laptop = new Item("Laptop", 1200.00);
        Item mouse = new Item("Mouse", 25.00);
        Item keyboard = new Item("Keyboard", 75.00);
        Item monitor = new Item("Monitor", 300.00);

        // 1. Add items to cart
        cart1.addItem(laptop);
        cart1.addItem(mouse);
        cart1.addItem(mouse); // Add a second mouse for BOGO
        cart1.addItem(keyboard);
        cart1.addItem(monitor);

        cart1.displayCart();

        // 2. Add multiple discounts
        cart1.addDiscount(new BuyOneGetOneFreeDiscount("Mouse"));
        cart1.addDiscount(new PercentageDiscount(10)); // 10% off entire cart
        cart1.addDiscount(new FixedAmountDiscount(100, 1500)); // $100 off if total is over $1500

        // 3. Calculate final price
        cart1.calculateFinalPrice();

        // --- SCENARIO 2: Removing Items ---
        System.out.println("\n||| SCENARIO 2: Removing an item and recalculating |||");
        cart1.removeItem(laptop);
        cart1.displayCart();
        cart1.calculateFinalPrice(); // The fixed discount should no longer apply
    }
}
