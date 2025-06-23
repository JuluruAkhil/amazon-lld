import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Main class to run the Amazon Locker System simulation.
 * All supporting classes are defined within this single file for ease of use.
 */
public class AmazonLockerSystem {

    public static void main(String[] args) {
        System.out.println("--- Setting up Amazon Locker System ---");
        // Initialize the main service
        AmazonLockerService service = new AmazonLockerService();

        // Create and setup a new locker location
        LockerLocation location = new LockerLocation("Downtown", "123 Main St, Chicago");
        location.addLocker(LockerFactory.createLocker(101, "small"));
        location.addLocker(LockerFactory.createLocker(102, "small"));
        location.addLocker(LockerFactory.createLocker(201, "medium"));
        service.addLocation(location);
        System.out.println("Locker location 'Downtown' created with 3 lockers.");

        // Create users and packages for the demo
        Customer customer1 = new Customer("John Doe", "john.doe@example.com");
        DeliveryAgent agent1 = new DeliveryAgent("Speedy Delivery Inc.", "agent007");
        Package package1 = PackageFactory.createPackage(9001, customer1, "small");
        System.out.println("Created package " + package1.getId() + " for " + customer1.getName());

        // --- DEMO 1: Successful Delivery and Pickup ---
        System.out.println("\n--- DEMO 1: Successful Delivery and Pickup ---");
        // 1. Deliver the package
        Integer otp = service.deliver(package1, agent1, "Downtown");
        if (otp != null) {
            System.out.println(
                    "SUCCESS: Package " + package1.getId() + " delivered to Downtown location. OTP is: " + otp);
        } else {
            System.out.println("FAILURE: Could not deliver package " + package1.getId());
        }

        // 2. Customer picks up the package with the correct OTP
        Package pickedUpPackage = service.pickup(package1.getId(), otp, "Downtown");
        if (pickedUpPackage != null) {
            System.out.println("SUCCESS: Customer " + pickedUpPackage.getCustomer().getName() + " picked up package "
                    + pickedUpPackage.getId() + ".");
            System.out.println("Package status: " + pickedUpPackage.getStatus());
        } else {
            System.out.println("FAILURE: Pickup failed for package " + package1.getId());
        }

        // --- DEMO 2: Expired Package Return ---
        System.out.println("\n--- DEMO 2: Expired Package Return ---");
        Package package2 = new Package(9002, customer1, new Small());
        // Manually set an old delivery date to simulate expiry
        package2.setExpiry(-5); // Set expiry to 5 days ago

        Integer otp2 = service.deliver(package2, agent1, "Downtown");
        if (otp2 != null) {
            System.out.println("INFO: Package " + package2.getId() + " delivered, but it is already expired.");
        }

        // Run the return process for expired packages
        System.out.println("Running expired package return process...");
        List<Package> returned = service.returnExpired("Downtown");

        if (!returned.isEmpty()) {
            System.out.println("SUCCESS: Returned " + returned.size() + " expired package(s).");
            for (Package p : returned) {
                System.out.println("- Returned Package ID: " + p.getId());
            }
        } else {
            System.out.println("INFO: No expired packages found to return.");
        }
    }
}

// factory pattern for locker
class LockerFactory {
    public static Locker createLocker(int lockerId, String size) {
        Size lockerSize;
        switch (size) {
            case "small":
                lockerSize = new Small();
                break;
            case "medium":
                lockerSize = new Medium();
                break;
            default:
                throw new IllegalArgumentException("Invalid size for locker: " + size);
        }
        return new Locker(lockerId, lockerSize);
    }
}

// factory pattern for package
class PackageFactory {
    public static Package createPackage(int id, Customer customer, String size) {
        Size packageSize;
        switch (size) {
            case "small":
                packageSize = new Small();
                break;
            case "medium":
                packageSize = new Medium();
                break;
            default:
                throw new IllegalArgumentException("Invalid size for package: " + size);
        }
        return new Package(id, customer, packageSize);
    }
}

// STEP 1: SIZE CLASSES
// These classes define the different sizes for packages and lockers.

/**
 * Base class for the size of a locker or a package.
 */
class Size {
    private final String name;
    private final int sizeValue;

    public Size(String name, int sizeValue) {
        this.name = name;
        this.sizeValue = sizeValue;
    }

    public String getName() {
        return name;
    }

    public boolean canFit(Size other) {
        return this.sizeValue >= other.sizeValue;
    }
}

/**
 * Concrete class for a "Small" size.
 */
class Small extends Size {
    public Small() {
        super("small", 1);
    }
}

/**
 * Concrete class for a "Medium" size.
 */
class Medium extends Size {
    public Medium() {
        super("medium", 2);
    }
}

// STEP 2: USER CLASSES
// Simple classes to represent the customer and the delivery agent.

/**
 * Represents a customer who receives packages.
 */
class Customer {
    private final String name;
    private final String email;

    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}

/**
 * Represents a delivery agent who delivers packages.
 */
class DeliveryAgent {
    private final String name;
    private final String agentId;

    public DeliveryAgent(String name, String agentId) {
        this.name = name;
        this.agentId = agentId;
    }

    public String getName() {
        return name;
    }
}

// STEP 3: CORE BUSINESS OBJECTS
// These are the main entities: Package, Locker, and Otp.

/**
 * Represents a package with its details and status.
 */
class Package {
    private final int id;
    private final Customer customer;
    private final Size size;
    private String status;
    private LocalDateTime pickupExpiry;

    public Package(int id, Customer customer, Size size) {
        this.id = id;
        this.customer = customer;
        this.size = size;
        this.status = "In Transit";
    }

    // Getters
    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Size getSize() {
        return size;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getPickupExpiry() {
        return pickupExpiry;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setExpiry(int days) {
        this.pickupExpiry = LocalDateTime.now().plus(days, ChronoUnit.DAYS);
    }
}

/**
 * Represents a single locker unit.
 */
class Locker {
    private final int lockerId;
    private final Size size;
    private boolean isAvailable;
    private Package currentPackage;

    public Locker(int lockerId, Size size) {
        this.lockerId = lockerId;
        this.size = size;
        this.isAvailable = true;
        this.currentPackage = null;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public boolean canFit(Package aPackage) {
        return this.size.canFit(aPackage.getSize());
    }

    public boolean placeParcel(Package aPackage) {
        if (isAvailable) {
            this.currentPackage = aPackage;
            this.isAvailable = false;
            return true;
        }
        return false;
    }

    public boolean removeParcel() {
        if (!isAvailable) {
            this.currentPackage = null;
            this.isAvailable = true;
            return true;
        }
        return false;
    }
}

/**
 * Represents a One-Time Password for secure pickup.
 */
class Otp {
    private final int code;

    public Otp() {
        this.code = new Random().nextInt(900000) + 100000; // 6-digit OTP
    }

    public int getCode() {
        return code;
    }

    public boolean verify(int codeToVerify) {
        return this.code == codeToVerify;
    }
}

// STEP 4: LOCATION MANAGEMENT
// Manages a collection of lockers and active deliveries at a physical location.

/**
 * A helper record to store all information about an active delivery.
 */
record DeliveryInfo(Locker locker, Otp otp, Package aPackage) {
}

/**
 * Represents a physical location containing multiple lockers.
 */
class LockerLocation {
    private final String name;
    private final String address;
    private final List<Locker> lockers;
    private final Map<Integer, DeliveryInfo> usedLockers; // packageId -> DeliveryInfo

    public LockerLocation(String name, String address) {
        this.name = name;
        this.address = address;
        this.lockers = new ArrayList<>();
        this.usedLockers = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<Integer, DeliveryInfo> getUsedLockers() {
        return usedLockers;
    }

    public void addLocker(Locker locker) {
        this.lockers.add(locker);
    }

    public Optional<Locker> findLocker(Package aPackage) {
        return lockers.stream()
                .filter(Locker::isAvailable)
                .filter(locker -> locker.canFit(aPackage))
                .findFirst();
    }
}

// STEP 5: MAIN SERVICE (FACADE)
// The main service that orchestrates all operations.

/**
 * Main service managing all locker locations and operations.
 */
class AmazonLockerService {
    private final Map<String, LockerLocation> locations;

    public AmazonLockerService() {
        this.locations = new HashMap<>();
    }

    public void addLocation(LockerLocation location) {
        this.locations.put(location.getName(), location);
    }

    public Integer deliver(Package aPackage, DeliveryAgent deliveryAgent, String locationName) {
        LockerLocation loc = locations.get(locationName);
        if (loc == null)
            return null;

        Optional<Locker> foundLocker = loc.findLocker(aPackage);
        if (foundLocker.isEmpty())
            return null;

        Locker locker = foundLocker.get();
        if (locker.placeParcel(aPackage)) {
            aPackage.setStatus("Delivered");
            if (aPackage.getPickupExpiry() == null) {
                aPackage.setExpiry(3); // Default 3 days to pickup
            }

            Otp code = new Otp();
            loc.getUsedLockers().put(aPackage.getId(), new DeliveryInfo(locker, code, aPackage));
            return code.getCode();
        }
        return null;
    }

    public Package pickup(int packageId, int code, String locationName) {
        LockerLocation loc = locations.get(locationName);
        if (loc == null || !loc.getUsedLockers().containsKey(packageId))
            return null;

        DeliveryInfo info = loc.getUsedLockers().get(packageId);
        if (info.otp().verify(code)) {
            info.locker().removeParcel();
            info.aPackage().setStatus("Picked_Up");
            loc.getUsedLockers().remove(packageId);
            return info.aPackage();
        }
        return null;
    }

    public List<Package> returnExpired(String locationName) {
        List<Package> expiredPackages = new ArrayList<>();
        LockerLocation loc = locations.get(locationName);
        if (loc == null)
            return expiredPackages;

        // Use a copy of keys to avoid ConcurrentModificationException during removal
        List<Integer> packageIds = new ArrayList<>(loc.getUsedLockers().keySet());

        for (Integer pkgId : packageIds) {
            DeliveryInfo info = loc.getUsedLockers().get(pkgId);
            Package aPackage = info.aPackage();
            if (aPackage.getPickupExpiry() != null && LocalDateTime.now().isAfter(aPackage.getPickupExpiry())) {
                info.locker().removeParcel();
                loc.getUsedLockers().remove(pkgId);
                aPackage.setStatus("Returned_Expired");
                expiredPackages.add(aPackage);
            }
        }
        return expiredPackages;
    }
}
