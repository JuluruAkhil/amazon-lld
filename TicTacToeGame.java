import java.util.*;
import java.util.List;
import java.util.Scanner;

/**
 * Represents a player in the Tic Tac Toe game.
 */
class Player {
    private final String name;
    private final char symbol;

    public Player(String name, char symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public char getSymbol() {
        return symbol;
    }
}

/**
 * Represents the game board.
 */
class Board {
    private final int size;
    private final char[][] grid;
    private int movesMade;

    public Board(int size) {
        this.size = size;
        this.grid = new char[size][size];
        this.movesMade = 0;
        // Initialize the grid with empty spaces
        for (int i = 0; i < size; i++) {
            Arrays.fill(grid[i], ' ');
        }
    }

    public int getSize() {
        return size;
    }

    /**
     * Gets the symbol at a specific cell.
     * 
     * @param row The row of the cell.
     * @param col The column of the cell.
     * @return The character symbol at the given cell.
     */
    public char getSymbolAt(int row, int col) {
        return grid[row][col];
    }

    /**
     * Displays the current state of the board.
     */
    public void display() {
        System.out.println();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(" " + grid[i][j] + " ");
                if (j < size - 1) {
                    System.out.print("|");
                }
            }
            System.out.println();
            if (i < size - 1) {
                for (int k = 0; k < size; k++) {
                    System.out.print("----");
                }
                System.out.println();
            }
        }
        System.out.println();
    }

    /**
     * Checks if a move is valid (within bounds and on an empty cell).
     */
    public boolean isValidMove(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size && grid[row][col] == ' ';
    }

    /**
     * Places a player's symbol on the board.
     */
    public void placeSymbol(int row, int col, char symbol) {
        if (isValidMove(row, col)) {
            grid[row][col] = symbol;
            movesMade++;
        }
    }

    /**
     * Checks if the board is full.
     */
    public boolean isFull() {
        return movesMade == size * size;
    }
}

/**
 * The main class that encapsulates the Tic Tac Toe game logic.
 */
public class TicTacToeGame {
    private final Board board;
    private final List<Player> players;
    private int currentPlayerIndex;
    private Player winner;

    public TicTacToeGame(int size, List<Player> players) {
        this.board = new Board(size);
        this.players = players;
        this.currentPlayerIndex = 0;
        this.winner = null;
    }

    /**
     * Starts and manages the main game loop.
     */
    public void play() {
        Scanner scanner = new Scanner(System.in);

        while (winner == null && !board.isFull()) {
            board.display();
            Player currentPlayer = players.get(currentPlayerIndex);
            System.out.println(currentPlayer.getName() + "'s turn (" + currentPlayer.getSymbol() + ")");

            int row, col;
            while (true) {
                System.out.print("Enter row (0-" + (board.getSize() - 1) + "): ");
                row = scanner.nextInt();
                System.out.print("Enter column (0-" + (board.getSize() - 1) + "): ");
                col = scanner.nextInt();

                if (board.isValidMove(row, col)) {
                    break;
                } else {
                    System.out.println("Invalid move. The cell is already taken or out of bounds. Please try again.");
                }
            }

            board.placeSymbol(row, col, currentPlayer.getSymbol());

            if (checkWin(row, col, currentPlayer.getSymbol())) {
                winner = currentPlayer;
            } else {
                switchPlayer();
            }
        }

        board.display();
        if (winner != null) {
            System.out.println("Congratulations, " + winner.getName() + "! You have won the game!");
        } else {
            System.out.println("The game is a tie! No more moves possible.");
        }
        scanner.close();
    }

    /**
     * Switches to the next player's turn.
     */
    private void switchPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    /**
     * Checks if the last move resulted in a win. This is an optimized check
     * that only evaluates the lines intersecting with the last move.
     * 
     * @param row    The row of the last move.
     * @param col    The column of the last move.
     * @param symbol The symbol of the current player.
     * @return true if the current player has won, false otherwise.
     */
    private boolean checkWin(int row, int col, char symbol) {
        int n = board.getSize();

        // Check row for a win
        boolean rowWin = true;
        for (int j = 0; j < n; j++) {
            if (board.getSymbolAt(row, j) != symbol) {
                rowWin = false;
                break;
            }
        }
        if (rowWin)
            return true;

        // Check column for a win
        boolean colWin = true;
        for (int i = 0; i < n; i++) {
            if (board.getSymbolAt(i, col) != symbol) {
                colWin = false;
                break;
            }
        }
        if (colWin)
            return true;

        // Check main diagonal for a win (only if the move was on the diagonal)
        if (row == col) {
            boolean mainDiagWin = true;
            for (int i = 0; i < n; i++) {
                if (board.getSymbolAt(i, i) != symbol) {
                    mainDiagWin = false;
                    break;
                }
            }
            if (mainDiagWin)
                return true;
        }

        // Check anti-diagonal for a win (only if the move was on the anti-diagonal)
        if (row + col == n - 1) {
            boolean antiDiagWin = true;
            for (int i = 0; i < n; i++) {
                if (board.getSymbolAt(i, n - 1 - i) != symbol) {
                    antiDiagWin = false;
                    break;
                }
            }
            if (antiDiagWin)
                return true;
        }

        return false;
    }

    /**
     * Main method to set up and start the Tic Tac Toe game.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("--- Welcome to Tic Tac Toe ---");
        System.out.print("Enter the size of the board (e.g., 3 for 3x3): ");
        int size = scanner.nextInt();

        System.out.print("Enter the number of players: ");
        int numPlayers = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            System.out.print("Enter name for Player " + (i + 1) + ": ");
            String name = scanner.nextLine();
            System.out.print("Enter symbol for " + name + ": ");
            char symbol = scanner.nextLine().charAt(0);
            players.add(new Player(name, symbol));
        }

        TicTacToeGame game = new TicTacToeGame(size, players);
        game.play();

        scanner.close();
    }
}
