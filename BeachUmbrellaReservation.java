import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// --- Type Object Pattern for Spot Types ---
/**
 * Base class representing a category of beach spot. This allows different
 * pricing strategies (tiers) for different types of spots.
 */
class SpotType {
    private final String name;
    private final double hourlyRate;

    public SpotType(String name, double hourlyRate) {
        this.name = name;
        this.hourlyRate = hourlyRate;
    }

    public String getName() {
        return name;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }
}

class StandardSpot extends SpotType {
    public StandardSpot() {
        super("Standard", 20.0);
    }
}

class PremiumSpot extends SpotType {
    public PremiumSpot() {
        super("Premium", 40.0);
    }
}

class VIPSpot extends SpotType {
    public VIPSpot() {
        super("VIP", 80.0);
    }
}

// --- Core Entities ---

/**
 * Represents a customer visiting the beach.
 */
class Customer {
    private final String customerId;
    private final String name;

    public Customer(String customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }
}

/**
 * Represents a single bookable beach spot. It manages its own state.
 */
class BeachSpot {
    private final String spotId;
    private final SpotType spotType;
    private boolean isAvailable;
    private String bookingId;
    private Customer customer;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public BeachSpot(String spotId, SpotType spotType) {
        this.spotId = spotId;
        this.spotType = spotType;
        this.isAvailable = true;
    }

    // Getters
    public String getSpotId() {
        return spotId;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public String getBookingId() {
        return bookingId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Books the spot for a customer and returns a unique booking ID.
     */
    public Optional<String> book(Customer customer, LocalDateTime start, int hours) {
        if (isAvailable) {
            this.isAvailable = false;
            this.customer = customer;
            this.startTime = start;
            this.endTime = start.plusHours(hours);
            this.bookingId = String.format("%s-%s", spotId, start.toLocalDate());
            return Optional.of(bookingId);
        }
        return Optional.empty();
    }

    /**
     * Releases the spot, making it available again.
     */
    public void release() {
        this.isAvailable = true;
        this.customer = null;
        this.startTime = null;
        this.endTime = null;
        this.bookingId = null;
    }

    /**
     * Calculates the price for the current booking.
     */
    public double calculatePrice() {
        if (startTime != null && endTime != null) {
            long hours = Duration.between(startTime, endTime).toHours();
            return spotType.getHourlyRate() * hours;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Spot " + spotId + " (" + spotType.getName() + ")";
    }
}

/**
 * Represents a beach, which contains a collection of spots.
 */
class Beach {
    private final String name;
    private final Map<String, BeachSpot> spots = new HashMap<>();
    private final Map<String, BeachSpot> activeBookings = new HashMap<>(); // bookingId -> BeachSpot

    public Beach(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, BeachSpot> getActiveBookings() {
        return activeBookings;
    }

    public void addSpot(BeachSpot spot) {
        spots.put(spot.getSpotId(), spot);
    }

    public Optional<BeachSpot> findAvailableSpot(String spotTypeName) {
        return spots.values().stream()
                .filter(BeachSpot::isAvailable)
                .filter(spot -> spot.getSpotType().getName().equalsIgnoreCase(spotTypeName))
                .findFirst();
    }

    public List<BeachSpot> getAvailableSpots() {
        return spots.values().stream()
                .filter(BeachSpot::isAvailable)
                .collect(Collectors.toList());
    }
}

/**
 * Facade Pattern: Provides a simplified interface to the booking system's
 * complex operations.
 * It also acts as a simulated repository for beaches and customers.
 */
class BeachBookingService {
    private final Map<String, Beach> beaches = new HashMap<>();
    private final Map<String, Customer> customers = new HashMap<>();

    public void setupBeach(String beachName, int rows, int spotsPerRow) {
        Beach beach = new Beach(beachName);
        for (int r = 1; r <= rows; r++) {
            for (int s = 1; s <= spotsPerRow; s++) {
                String spotId = String.format("R%d-S%d", r, s);
                SpotType type;
                if (r <= 2) {
                    type = new VIPSpot(); // First 2 rows are VIP
                } else if (r <= 5) {
                    type = new PremiumSpot(); // Next 3 are Premium
                } else {
                    type = new StandardSpot(); // The rest are Standard
                }
                beach.addSpot(new BeachSpot(spotId, type));
            }
        }
        beaches.put(beachName, beach);
        System.out.println("Beach '" + beachName + "' has been set up with " + (rows * spotsPerRow) + " spots.");
    }

    public void registerCustomer(String customerId, String name) {
        customers.putIfAbsent(customerId, new Customer(customerId, name));
    }

    public Optional<String> bookSpot(String customerId, String beachName, String spotTypeName, LocalDateTime startTime,
            int hours) {
        if (!customers.containsKey(customerId) || !beaches.containsKey(beachName)) {
            System.out.println("Error: Invalid customer or beach name.");
            return Optional.empty();
        }

        Customer customer = customers.get(customerId);
        Beach beach = beaches.get(beachName);
        Optional<BeachSpot> spotOpt = beach.findAvailableSpot(spotTypeName);

        if (spotOpt.isEmpty()) {
            System.out.println("Sorry, no " + spotTypeName + " spots are available at this time.");
            return Optional.empty();
        }

        BeachSpot spot = spotOpt.get();
        Optional<String> bookingIdOpt = spot.book(customer, startTime, hours);
        bookingIdOpt.ifPresent(bookingId -> beach.getActiveBookings().put(bookingId, spot));

        return bookingIdOpt;
    }

    public void displayBookingDetails(String beachName, String bookingId) {
        if (!beaches.containsKey(beachName))
            return;
        BeachSpot spot = beaches.get(beachName).getActiveBookings().get(bookingId);
        if (spot != null) {
            System.out.println("\n--- Booking Details ---");
            System.out.println("Booking ID: " + spot.getBookingId());
            System.out.println("Spot: " + spot.getSpotId() + " (" + spot.getSpotType().getName() + ")");
            System.out.println("Customer: " + spot.getCustomer().getName());
            System.out.println("Period: " + spot.getStartTime() + " to " + spot.getEndTime());
            System.out.printf("Total Price: $%.2f\n", spot.calculatePrice());
            System.out.println("-----------------------");
        } else {
            System.out.println("Booking ID not found.");
        }
    }

    public boolean cancelBooking(String beachName, String bookingId) {
        if (!beaches.containsKey(beachName))
            return false;
        BeachSpot spot = beaches.get(beachName).getActiveBookings().remove(bookingId);
        if (spot != null) {
            spot.release();
            System.out.println("Booking " + bookingId + " has been successfully canceled.");
            return true;
        }
        System.out.println("Error: Booking ID " + bookingId + " not found.");
        return false;
    }

    public void listAvailableSpots(String beachName) {
        if (!beaches.containsKey(beachName))
            return;
        System.out.println("\n--- Available Spots at " + beachName + " ---");
        beaches.get(beachName).getAvailableSpots().forEach(System.out::println);
        System.out.println("------------------------------------");
    }
}

/**
 * Main class to demonstrate the Beach Umbrella Reservation system.
 */
public class BeachUmbrellaReservation {
    public static void main(String[] args) {
        // 1. Setup the service
        BeachBookingService service = new BeachBookingService();
        service.setupBeach("Sunny Beach", 6, 5);

        // 2. Register customers
        service.registerCustomer("C001", "John Doe");
        service.registerCustomer("C002", "Jane Smith");

        // 3. List available spots initially
        service.listAvailableSpots("Sunny Beach");

        // 4. John books a VIP spot
        System.out.println("\nJohn is booking a VIP spot...");
        Optional<String> bookingIdOpt = service.bookSpot(
                "C001", "Sunny Beach", "VIP", LocalDateTime.now(), 4);

        // 5. Display booking details if successful
        if (bookingIdOpt.isPresent()) {
            String bookingId = bookingIdOpt.get();
            System.out.println("Booking successful! ID: " + bookingId);
            service.displayBookingDetails("Sunny Beach", bookingId);
        }

        // 6. List available spots again
        service.listAvailableSpots("Sunny Beach");

        // 7. Cancel the booking
        System.out.println("\nCancelling John's booking...");
        bookingIdOpt.ifPresent(id -> service.cancelBooking("Sunny Beach", id));

        // 8. List available spots one last time
        service.listAvailableSpots("Sunny Beach");
    }
}
