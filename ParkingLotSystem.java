import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a vehicle with a name and type.
 * Vehicle types can be represented by integers, e.g.,
 * 0: SMALL (Motorcycle), 1: MEDIUM (Car), 2: LARGE (Truck).
 */
class Vehicle {
    private final String name;
    private final int type;

    public Vehicle(String name, int type) {
        // In the original C++ code, `name = name;` was a self-assignment bug.
        // It's corrected here using `this`.
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }
}

/**
 * Represents a single parking slot.
 */
class Slot {
    private final int spotId;
    private Vehicle vehicle;
    private boolean isFree;

    public Slot(int id) {
        this.spotId = id;
        this.isFree = true;
        this.vehicle = null;
    }

    public boolean isAvailable() {
        return isFree;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * Assigns a vehicle to this slot.
     * @param vehicle The vehicle to be parked.
     */
    public void bookSlot(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.isFree = false;
    }

    /**
     * Frees the slot by removing the vehicle.
     */
    public void freeSlot() {
        this.vehicle = null;
        this.isFree = true;
    }
}

/**
 * Represents a single floor in the parking garage.
 * It contains multiple slots of different types.
 */
class Floor {
    private final int level;
    // Map from vehicle type (0: small, 1: medium, 2: large) to a list of slots
    private final Map<Integer, List<Slot>> slots;

    public Floor(int level, int smallSlotsCount, int mediumSlotsCount, int largeSlotsCount) {
        this.level = level;
        this.slots = new HashMap<>();
        
        slots.put(0, new ArrayList<>());
        slots.put(1, new ArrayList<>());
        slots.put(2, new ArrayList<>());

        for (int i = 0; i < smallSlotsCount; i++) {
            slots.get(0).add(new Slot(i));
        }
        for (int i = 0; i < mediumSlotsCount; i++) {
            slots.get(1).add(new Slot(i));
        }
        for (int i = 0; i < largeSlotsCount; i++) {
            slots.get(2).add(new Slot(i));
        }
    }

    /**
     * Finds the first available slot for a given vehicle type.
     * @param vehicleType The type of vehicle.
     * @return A free Slot object, or null if none are available.
     */
    public Slot getFreeSlot(int vehicleType) {
        for (Slot slot : slots.get(vehicleType)) {
            if (slot.isAvailable()) {
                return slot;
            }
        }
        return null;
    }
    
    /**
     * Parks a vehicle in a specific slot.
     * @param slot The slot to book.
     * @param vehicle The vehicle to park.
     */
    public void bookSlot(Slot slot, Vehicle vehicle) {
        slot.bookSlot(vehicle);
    }
    
    /**
     * Removes a vehicle from a slot on this floor.
     * @param vehicleName The name/license plate of the vehicle to remove.
     * @param vehicleType The type of the vehicle.
     */
    public void removeVehicle(String vehicleName, int vehicleType) {
        for (Slot slot : slots.get(vehicleType)) {
            // Check if slot is not free and if the vehicle name matches
            if (!slot.isAvailable() && slot.getVehicle().getName().equals(vehicleName)) {
                slot.freeSlot();
                return; // Assuming unique vehicle names, we can exit after finding it.
            }
        }
    }
}

/**
 * Manages the entire parking lot, including all floors and vehicle assignments.
 */
class Manager {
    // Maps a vehicle name to the floor level it's parked on.
    private final Map<String, Integer> vehicleToFloorMap;
    // Maps a floor level to the Floor object.
    private final Map<Integer, Floor> floorMap;

    public Manager() {
        this.vehicleToFloorMap = new HashMap<>();
        this.floorMap = new HashMap<>();
    }

    /**
     * Adds a new floor to the parking lot.
     */
    public void addFloor(int level, int small, int medium, int large) {
        if (!floorMap.containsKey(level)) {
            floorMap.put(level, new Floor(level, small, medium, large));
            System.out.println("Added floor " + level);
        }
    }

    /**
     * Parks a car by finding an available slot across all floors.
     */
    public void parkCar(String name, int type) {
        if (vehicleToFloorMap.containsKey(name)) {
            System.out.println("Error: Vehicle with name " + name + " already exists.");
            return;
        }

        for (Map.Entry<Integer, Floor> entry : floorMap.entrySet()) {
            int floorLevel = entry.getKey();
            Floor floor = entry.getValue();
            Slot freeSlot = floor.getFreeSlot(type);

            if (freeSlot != null) {
                Vehicle vehicle = new Vehicle(name, type);
                floor.bookSlot(freeSlot, vehicle);
                vehicleToFloorMap.put(name, floorLevel);
                System.out.println("Booked vehicle " + name + " (type " + type + ") at floor " + floorLevel);
                return;
            }
        }

        System.out.println("No free slots available for vehicle type " + type);
    }

    /**
     * Removes a car from the parking lot.
     */
    public void removeCar(String name, int type) {
        if (!vehicleToFloorMap.containsKey(name)) {
            System.out.println("Error: Vehicle with name " + name + " does not exist.");
            return;
        }
        
        int floorLevel = vehicleToFloorMap.get(name);
        Floor floor = floorMap.get(floorLevel);
        
        if (floor != null) {
            floor.removeVehicle(name, type);
            vehicleToFloorMap.remove(name);
            System.out.println("Removed vehicle " + name + " from floor " + floorLevel);
        }
    }
}


public class ParkingLotSystem {
    public static void main(String[] args) {
        Manager manager = new Manager();
        manager.addFloor(0, 1, 2, 2);
        manager.addFloor(1, 0, 3, 1);
        
        System.out.println("\n--- Parking Sequence ---");
        manager.parkCar("deep", 0); // Parks on floor 0, type 0 (small)
        manager.parkCar("as", 0);   // Tries to park another small car
        manager.removeCar("deep", 0);
        manager.parkCar("as", 0);   // Tries again after a slot is freed
        
        System.out.println("\n--- Edge Case Tests ---");
        manager.parkCar("car1", 1); // Parks on floor 0, type 1 (medium)
        manager.parkCar("car2", 1); // Parks on floor 0, type 1 (medium)
        manager.parkCar("car3", 1); // Parks on floor 1, type 1 (medium)
        manager.parkCar("as", 0);   // Trying to park an existing car
        manager.removeCar("unknown", 0); // Trying to remove a non-existent car
    }
}