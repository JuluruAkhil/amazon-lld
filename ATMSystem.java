import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple record to hold transaction data immutably.
 * Replaces the C++ std::pair.
 */
record Transaction(String type, int amount) {
}

/**
 * Represents a bank user with a balance, PIN, and transaction history.
 */
class User {
    private int balance;
    private final int pin;
    private final int id;
    private final List<Transaction> transactionHistory;

    private static final String WITHDRAW_CT = "withdrew";
    private static final String DEPOSIT_CT = "deposited";

    public User(int balance, int pin, int id) {
        this.balance = balance;
        this.pin = pin;
        this.id = id;
        this.transactionHistory = new ArrayList<>();
    }

    /**
     * Attempts to withdraw a specified amount from the user's balance.
     * 
     * @return true if the withdrawal was successful, false otherwise.
     */
    public boolean withdrawMoney(int amount) {
        if (amount > balance) {
            System.out.println("User " + id + ": Insufficient funds for withdrawal.");
            return false;
        }
        balance -= amount;
        transactionHistory.add(new Transaction(WITHDRAW_CT, amount));
        System.out.println("User " + id + " " + WITHDRAW_CT + " $" + amount + ". New balance: $" + balance);
        return true;
    }

    /**
     * Deposits a specified amount into the user's balance.
     */
    public void depositMoney(int amount) {
        balance += amount;
        transactionHistory.add(new Transaction(DEPOSIT_CT, amount));
        System.out.println("User " + id + " " + DEPOSIT_CT + " $" + amount + ". New balance: $" + balance);
    }

    public int getPin() {
        return pin;
    }

    public void printHistory() {
        System.out.println("\n--- Transaction History for User " + id + " ---");
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            for (Transaction t : transactionHistory) {
                System.out.println("- " + t.type() + " $" + t.amount());
            }
        }
        System.out.println("------------------------------------");
    }
}

/**
 * Manages all user accounts and acts as a central database.
 */
class Bank {
    private final Map<Integer, User> userData;

    public Bank() {
        this.userData = new HashMap<>();
    }

    public void addUser(int id, int pin, int initialAmount) {
        userData.put(id, new User(initialAmount, pin, id));
        System.out.println("Bank: New user added with ID " + id);
    }

    public boolean depositToUser(int id, int pin, int amount) {
        User user = userData.get(id);
        if (user == null) {
            System.out.println("Bank Error: User with ID " + id + " not found.");
            return false;
        }
        if (user.getPin() != pin) {
            System.out.println("Bank Error: Incorrect PIN for user " + id);
            return false;
        }
        user.depositMoney(amount);
        return true;
    }

    public boolean withdrawFromUser(int id, int pin, int amount) {
        User user = userData.get(id);
        if (user == null) {
            System.out.println("Bank Error: User with ID " + id + " not found.");
            return false;
        }
        if (user.getPin() != pin) {
            System.out.println("Bank Error: Incorrect PIN for user " + id);
            return false;
        }
        return user.withdrawMoney(amount);
    }

    public void printTransactionHistory(int id, int pin) {
        User user = userData.get(id);
        if (user == null) {
            System.out.println("Bank Error: User with ID " + id + " not found.");
            return;
        }
        if (user.getPin() != pin) {
            System.out.println("Bank Error: Incorrect PIN for user " + id);
            return;
        }
        user.printHistory();
    }
}

/**
 * Represents a physical ATM that interacts with the Bank.
 */
class ATM {
    private int atmCashBalance;
    private final Bank bank;

    public ATM(Bank bank, int initialCash) {
        this.bank = bank;
        this.atmCashBalance = initialCash;
    }

    public void depositMoney(int id, int pin, int amount) {
        // In a real scenario, the ATM would verify the cash deposited.
        // For this simulation, we assume the deposit is always successful if the user
        // is valid.
        if (bank.depositToUser(id, pin, amount)) {
            // For simplicity, we don't add deposited cash to the ATM balance,
            // as it needs to be processed first. The user's bank account is updated.
            System.out.println("ATM: Deposit of $" + amount + " for user " + id + " processed by bank.");
        }
    }

    public void withdrawMoney(int id, int pin, int amount) {
        if (atmCashBalance < amount) {
            System.out.println("ATM Error: Insufficient funds in this ATM.");
            return;
        }
        // Attempt to withdraw from the bank first.
        if (bank.withdrawFromUser(id, pin, amount)) {
            // If bank withdrawal is successful, dispense cash from ATM.
            atmCashBalance -= amount;
            System.out.println("ATM: Dispensed $" + amount + ". Remaining ATM balance: $" + atmCashBalance);
        }
    }

    public void printHistory(int id, int pin) {
        bank.printTransactionHistory(id, pin);
    }
}

/**
 * Main class to run the ATM system simulation.
 */
public class ATMSystem {
    public static void main(String[] args) {
        // Create a central bank
        Bank bank = new Bank();

        // Create two ATMs connected to the same bank
        ATM atm1 = new ATM(bank, 10000);
        ATM atm2 = new ATM(bank, 20000);

        // A user signs up with the bank
        bank.addUser(1, 1234, 20000);

        System.out.println("\n--- Performing Transactions ---");

        // Incorrect PIN at ATM 1
        atm1.withdrawMoney(1, 9999, 100);

        // Successful withdrawal from ATM 2
        atm2.withdrawMoney(1, 1234, 10000);

        // Unsuccessful withdrawal due to insufficient funds in ATM 1
        atm1.withdrawMoney(1, 1234, 15000);

        // Successful withdrawal from ATM 1
        atm1.withdrawMoney(1, 1234, 1000);

        // Print transaction history with incorrect PIN
        atm1.printHistory(1, 1111);

        // Print transaction history with correct PIN
        atm1.printHistory(1, 1234);
    }
}
