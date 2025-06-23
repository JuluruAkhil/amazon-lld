import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// --- Composite Pattern: Component ---
/**
 * Abstract base class (or interface) for any item that can be part of an order.
 * This is the 'Component' in the Composite design pattern.
 */
interface OrderItem {
    double getPrice();

    String getName();
}

// --- Composite Pattern: Leaves ---
/**
 * Represents the base of a pizza.
 */
class Base implements OrderItem {
    private final String name;
    private final double price;

    public Base(String name, double price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public String getName() {
        return name;
    }
}

/**
 * Represents a topping for a pizza.
 */
class Topping implements OrderItem {
    private final String name;
    private final double price;

    public Topping(String name, double price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public String getName() {
        return name;
    }
}

/**
 * Represents a drink that can be ordered.
 */
class Drink implements OrderItem {
    private final String name;
    private final double price;

    public Drink(String name, double price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public String getName() {
        return name;
    }
}

// --- Composite Pattern: Composite & Builder Pattern ---
/**
 * Represents a custom pizza, composed of a base and multiple toppings.
 * It also uses a fluent interface which acts like a Builder pattern.
 */
class Pizza implements OrderItem {
    private final Base base;
    private final List<Topping> toppings;
    private final Store store; // Reference to the store to act as a factory for toppings

    public Pizza(Base base, Store store) {
        this.base = base;
        this.toppings = new ArrayList<>();
        this.store = store;
    }

    /**
     * Adds a topping to the pizza using its name (fluent interface for builder
     * pattern).
     */
    public Pizza addTopping(String toppingName) {
        this.toppings.add(store.buildTopping(toppingName));
        return this;
    }

    @Override
    public double getPrice() {
        double totalPrice = base.getPrice();
        for (Topping topping : toppings) {
            totalPrice += topping.getPrice();
        }
        return totalPrice;
    }

    @Override
    public String getName() {
        if (toppings.isEmpty()) {
            return base.getName() + " Pizza";
        }
        String toppingNames = toppings.stream()
                .map(Topping::getName)
                .collect(Collectors.joining(", "));
        return base.getName() + " Pizza with " + toppingNames;
    }
}

// --- Strategy Pattern for Discounts ---
/**
 * The 'Strategy' interface for different discount deals.
 */
interface Deal {
    double calculateDiscount(List<OrderItem> items);

    String getName();
}

/**
 * A concrete implementation of the Deal strategy for "Buy One Get One Free" on
 * pizzas.
 */
class BogoDeal implements Deal {
    @Override
    public String getName() {
        return "Buy One Get One Free on Pizzas!";
    }

    @Override
    public double calculateDiscount(List<OrderItem> items) {
        List<Pizza> pizzas = items.stream()
                .filter(item -> item instanceof Pizza)
                .map(item -> (Pizza) item)
                .sorted((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()))
                .collect(Collectors.toList());

        int numberOfFreePizzas = pizzas.size() / 2;
        double discount = 0.0;

        for (int i = 0; i < numberOfFreePizzas; i++) {
            discount += pizzas.get(i).getPrice();
        }
        return discount;
    }
}

// --- Factory Pattern ---
/**
 * Represents the store, which acts as a Factory for creating orderable items.
 */
class Store {
    private final String name;
    private final Map<String, Double> bases = new HashMap<>();
    private final Map<String, Double> toppings = new HashMap<>();
    private final Map<String, Double> drinks = new HashMap<>();

    public Store(String name) {
        this.name = name;
    }

    // Methods to configure the store's menu
    public void addBase(String name, double price) {
        bases.put(name, price);
    }

    public void addTopping(String name, double price) {
        toppings.put(name, price);
    }

    public void addDrink(String name, double price) {
        drinks.put(name, price);
    }

    // Factory methods to build items
    public Base buildBase(String name) {
        if (!bases.containsKey(name))
            throw new IllegalArgumentException("Base '" + name + "' not found.");
        return new Base(name, bases.get(name));
    }

    public Topping buildTopping(String name) {
        if (!toppings.containsKey(name))
            throw new IllegalArgumentException("Topping '" + name + "' not found.");
        return new Topping(name, toppings.get(name));
    }

    public Drink buildDrink(String name) {
        if (!drinks.containsKey(name))
            throw new IllegalArgumentException("Drink '" + name + "' not found.");
        return new Drink(name, drinks.get(name));
    }

    public Pizza createPizza(String baseName) {
        return new Pizza(buildBase(baseName), this);
    }
}

/**
 * Represents a customer's order.
 */
class Order {
    private final List<OrderItem> items = new ArrayList<>();
    private double total = 0;
    private double discount = 0;

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public double getInvoice(Deal deal) {
        double subtotal = items.stream().mapToDouble(OrderItem::getPrice).sum();

        if (deal != null) {
            this.discount = deal.calculateDiscount(items);
        }

        this.total = subtotal - this.discount;
        return this.total;
    }

    public void printInvoice(Deal deal) {
        System.out.println("\n--- Your Order ---");
        for (OrderItem item : items) {
            System.out.printf("%-40s $%6.2f\n", item.getName(), item.getPrice());
        }
        System.out.println("---------------------------------------------");
        double subtotal = items.stream().mapToDouble(OrderItem::getPrice).sum();
        System.out.printf("%-40s $%6.2f\n", "Subtotal", subtotal);
        if (deal != null) {
            System.out.printf("%-40s -$%6.2f\n", deal.getName(), this.discount);
        }
        System.out.println("---------------------------------------------");
        System.out.printf("%-40s $%6.2f\n", "Total", getInvoice(deal));
        System.out.println("---------------------------------------------\n");
    }
}

/**
 * Main class to demonstrate the Pizza Builder system.
 */
public class PizzaBuilder {
    public static void main(String[] args) {
        // 1. Setup the store and its menu (Factory setup)
        Store dominos = new Store("Domino's");
        dominos.addBase("Regular", 8.0);
        dominos.addBase("Thin Crust", 9.0);
        dominos.addTopping("Cheese", 1.5);
        dominos.addTopping("Pepperoni", 2.0);
        dominos.addTopping("Mushrooms", 1.75);
        dominos.addDrink("Coke", 2.5);

        // 2. Create pizzas using the fluent builder-like interface
        Pizza pizza1 = dominos.createPizza("Regular")
                .addTopping("Cheese")
                .addTopping("Pepperoni");

        Pizza pizza2 = dominos.createPizza("Thin Crust")
                .addTopping("Mushrooms");

        Drink coke = dominos.buildDrink("Coke");

        // 3. Create an order and add items
        Order order = new Order();
        order.addItem(pizza1);
        order.addItem(pizza2);
        order.addItem(coke);

        // 4. Create a deal strategy
        Deal bogoDeal = new BogoDeal();

        // 5. Generate and print the invoice with the deal applied
        order.printInvoice(bogoDeal);
    }
}
