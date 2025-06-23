import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// --- Enums for Game State and Types ---

enum PegColor {
    RED, // Hit
    BLUE // Miss
}

enum BoatOrientation {
    HORIZONTAL,
    VERTICAL
}

// --- Core Game Piece Classes ---

/**
 * Represents a single part of a boat.
 * Helps in tracking hits on a boat.
 */
class BoatPart {
    private final Boat parentBoat;
    private boolean isHit = false;

    public BoatPart(Boat parentBoat) {
        this.parentBoat = parentBoat;
    }

    public void hit() {
        this.isHit = true;
    }

    public boolean isHit() {
        return isHit;
    }

    public Boat getParentBoat() {
        return parentBoat;
    }
}

/**
 * Abstract base class for all boats. Defines the common interface.
 */
abstract class Boat {
    protected List<BoatPart> parts;
    protected BoatOrientation orientation;

    public Boat() {
        this.parts = new ArrayList<>();
        for (int i = 0; i < getSize(); i++) {
            parts.add(new BoatPart(this));
        }
    }

    public abstract int getSize();

    public abstract String getDescription();

    public void setOrientation(BoatOrientation orientation) {
        this.orientation = orientation;
    }

    public BoatOrientation getOrientation() {
        return orientation;
    }

    public boolean isSunk() {
        return parts.stream().allMatch(BoatPart::isHit);
    }

    public List<BoatPart> getParts() {
        return parts;
    }
}

class SmallBoat extends Boat {
    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public String getDescription() {
        return "Small Boat";
    }
}

class MediumBoat extends Boat {
    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public String getDescription() {
        return "Medium Boat";
    }
}

class LargeBoat extends Boat {
    @Override
    public int getSize() {
        return 5;
    }

    @Override
    public String getDescription() {
        return "Large Boat";
    }
}

/**
 * Represents a peg used to mark hits or misses.
 */
class Peg {
    private final PegColor color;

    public Peg(PegColor color) {
        this.color = color;
    }

    public PegColor getColor() {
        return color;
    }
}

// --- Game Board and Player Classes ---

/**
 * Represents the game grid. Manages the placement and state of boats and pegs.
 */
class Grid {
    private final int size;
    // The board can contain either a BoatPart or a Peg.
    private final Object[][] board;

    public Grid(int size) {
        this.size = size;
        this.board = new Object[size][size];
    }

    public int getSize() {
        return size;
    }

    public Object getObjectAt(int x, int y) {
        return board[x][y];
    }

    /**
     * Attempts to place a boat on the grid.
     * 
     * @return true if placement is successful, false otherwise.
     */
    public boolean placeBoat(Boat boat, int x, int y) {
        if (boat.getOrientation() == BoatOrientation.HORIZONTAL) {
            if (y + boat.getSize() > size)
                return false; // Out of bounds
            // Check for overlaps
            for (int i = 0; i < boat.getSize(); i++) {
                if (board[x][y + i] != null)
                    return false;
            }
            // Place the boat parts
            for (int i = 0; i < boat.getSize(); i++) {
                board[x][y + i] = boat.getParts().get(i);
            }
        } else { // VERTICAL
            if (x + boat.getSize() > size)
                return false; // Out of bounds
            // Check for overlaps
            for (int i = 0; i < boat.getSize(); i++) {
                if (board[x + i][y] != null)
                    return false;
            }
            // Place the boat parts
            for (int i = 0; i < boat.getSize(); i++) {
                board[x + i][y] = boat.getParts().get(i);
            }
        }
        return true;
    }

    /**
     * Processes an attack at a specific coordinate.
     * 
     * @return true if it was a hit, false if it was a miss.
     */
    public boolean receiveAttack(int x, int y) {
        Object target = board[x][y];
        if (target instanceof BoatPart) {
            BoatPart part = (BoatPart) target;
            part.hit();
            board[x][y] = new Peg(PegColor.RED); // Mark as hit
            return true;
        } else {
            board[x][y] = new Peg(PegColor.BLUE); // Mark as miss
            return false;
        }
    }
}

/**
 * Represents a player, managing their grid and boats.
 */
class Player {
    private final String name;
    public final Grid grid;
    private final List<Boat> boats;

    public Player(String name, int gridSize) {
        this.name = name;
        this.grid = new Grid(gridSize);
        this.boats = new ArrayList<>(List.of(new LargeBoat(), new MediumBoat(), new SmallBoat()));
    }

    public String getName() {
        return name;
    }

    public List<Boat> getBoats() {
        return boats;
    }

    public boolean hasLost() {
        return boats.stream().allMatch(Boat::isSunk);
    }
}

/**
 * Main class to orchestrate the Battleship game.
 */
public class Battleship {
    private final Player player1;
    private final Player player2;
    private Player currentPlayer;

    public Battleship(String p1Name, String p2Name, int gridSize) {
        this.player1 = new Player(p1Name, gridSize);
        this.player2 = new Player(p2Name, gridSize);
        this.currentPlayer = player1;
    }

    /**
     * Main game loop.
     */
    public void play() {
        Scanner scanner = new Scanner(System.in);

        // Step 1: Boat Placement
        placeBoatsForPlayer(player1, scanner);
        placeBoatsForPlayer(player2, scanner);

        // Step 2: Main game loop
        while (true) {
            Player opponent = (currentPlayer == player1) ? player2 : player1;
            System.out.println("\n------------------------------------------");
            System.out.println(currentPlayer.getName() + "'s turn to attack.");
            displayGrids(opponent);

            System.out.print("Enter attack row: ");
            int x = scanner.nextInt();
            System.out.print("Enter attack column: ");
            int y = scanner.nextInt();

            if (x < 0 || x >= opponent.grid.getSize() || y < 0 || y >= opponent.grid.getSize()) {
                System.out.println("Invalid coordinates. Try again.");
                continue;
            }

            if (opponent.grid.receiveAttack(x, y)) {
                System.out.println("HIT!");
            } else {
                System.out.println("MISS!");
            }

            if (opponent.hasLost()) {
                System.out.println("\n" + currentPlayer.getName() + " WINS THE GAME!");
                break;
            }

            // Switch turns
            currentPlayer = opponent;
        }
        scanner.close();
    }

    private void placeBoatsForPlayer(Player player, Scanner scanner) {
        System.out.println("\n--- " + player.getName() + ", place your boats ---");
        for (Boat boat : player.getBoats()) {
            player.grid.display();
            System.out.println("Placing a " + boat.getDescription() + " (size " + boat.getSize() + ")");
            while (true) {
                System.out.print("Orientation (0=H, 1=V): ");
                int o = scanner.nextInt();
                boat.setOrientation(o == 0 ? BoatOrientation.HORIZONTAL : BoatOrientation.VERTICAL);

                System.out.print("Enter starting row: ");
                int x = scanner.nextInt();
                System.out.print("Enter starting column: ");
                int y = scanner.nextInt();

                if (player.grid.placeBoat(boat, x, y)) {
                    System.out.println(boat.getDescription() + " placed.");
                    break;
                } else {
                    System.out.println("Invalid placement. Boat overlaps or is out of bounds. Try again.");
                }
            }
        }
    }

    private void displayGrids(Player opponent) {
        System.out.println("--- Opponent's Grid (Your Guesses) ---");
        opponent.grid.displayForOpponent();
    }

    public static void main(String[] args) {
        Battleship game = new Battleship("Player 1", "Player 2", 10);
        game.play();
    }
}

// --- Add display methods to the Grid class ---
// (This is a common practice in Java to keep related functionality together)
// We add this extension outside the main class block for clarity.

class Grid {
    // ... (existing fields and methods) ...
    public Grid(int size) {
        this.size = size;
        this.board = new Object[size][size];
    }

    private int size;
    private Object[][] board;

    // ... (existing placeBoat, receiveAttack) ...
    public boolean placeBoat(Boat boat, int x, int y) {
        return true;
    }

    public boolean receiveAttack(int x, int y) {
        return true;
    }

    public void display() {
        System.out.print("  ");
        for (int i = 0; i < size; i++)
            System.out.print(i + " ");
        System.out.println();
        for (int i = 0; i < size; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < size; j++) {
                if (board[i][j] instanceof BoatPart) {
                    System.out.print("B ");
                } else {
                    System.out.print("~ ");
                }
            }
            System.out.println();
        }
    }

    public void displayForOpponent() {
        System.out.print("  ");
        for (int i = 0; i < size; i++)
            System.out.print(i + " ");
        System.out.println();
        for (int i = 0; i < size; i++) {
            System.out.print(i + " ");
            for (int j = 0; j < size; j++) {
                if (board[i][j] instanceof Peg) {
                    Peg peg = (Peg) board[i][j];
                    System.out.print(peg.getColor() == PegColor.RED ? "X " : "O ");
                } else {
                    System.out.print("~ ");
                }
            }
            System.out.println();
        }
    }
}
