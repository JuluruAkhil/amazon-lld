import java.util.*;

/**
 * A helper class to represent a directed conversion rate from a source
 * currency.
 */
class Rate {
    String currency;
    double rateValue;

    public Rate(String currency, double rateValue) {
        this.currency = currency;
        this.rateValue = rateValue;
    }

    public String getCurrency() {
        return currency;
    }

    public double getRateValue() {
        return rateValue;
    }
}

/**
 * Main class to handle currency conversion calculations.
 */
public class CurrencyConverter {

    /**
     * A private class to hold the state for our BFS traversal.
     * It stores the current currency and the cumulative rate to reach it.
     */
    private static class State {
        String currency;
        double cumulativeRate;

        public State(String currency, double cumulativeRate) {
            this.currency = currency;
            this.cumulativeRate = cumulativeRate;
        }
    }

    /**
     * Calculates the conversion rate between two currencies given a list of known
     * rates.
     *
     * @param rates        A list of conversion rates, where each entry is a list of
     *                     [fromCurrency, toCurrency, rate].
     * @param fromCurrency The starting currency code (e.g., "GBP").
     * @param toCurrency   The target currency code (e.g., "AUD").
     * @return The calculated conversion rate, or -1.0 if no path is found.
     */
    public double findConversionRate(List<List<Object>> rates, String fromCurrency, String toCurrency) {
        // 1. Build the graph (adjacency list)
        Map<String, List<Rate>> adjList = buildGraph(rates);

        // Check if from and to currencies exist in our graph
        if (!adjList.containsKey(fromCurrency) || !adjList.containsKey(toCurrency)) {
            return -1.0;
        }

        // If converting to the same currency, the rate is 1.
        if (fromCurrency.equals(toCurrency)) {
            return 1.0;
        }

        // 2. Perform Breadth-First Search (BFS) to find the conversion path
        Queue<State> queue = new LinkedList<>();
        queue.add(new State(fromCurrency, 1.0));

        Set<String> visited = new HashSet<>();
        visited.add(fromCurrency);

        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            String currentCurrency = currentState.currency;
            double currentRate = currentState.cumulativeRate;

            if (currentCurrency.equals(toCurrency)) {
                // Correcting the typo from the original Python code: currentRate instead of
                // CurrentRate
                return currentRate;
            }

            // Explore neighbors
            if (adjList.containsKey(currentCurrency)) {
                for (Rate neighborRate : adjList.get(currentCurrency)) {
                    String nextCurrency = neighborRate.getCurrency();
                    if (!visited.contains(nextCurrency)) {
                        visited.add(nextCurrency);
                        double newCumulativeRate = currentRate * neighborRate.getRateValue();
                        queue.add(new State(nextCurrency, newCumulativeRate));
                    }
                }
            }
        }

        // 3. Return -1 if no path was found
        return -1.0;
    }

    /**
     * Builds a graph representation from the raw list of rates.
     */
    private Map<String, List<Rate>> buildGraph(List<List<Object>> rates) {
        Map<String, List<Rate>> adjList = new HashMap<>();

        for (List<Object> rateInfo : rates) {
            String from = (String) rateInfo.get(0);
            String to = (String) rateInfo.get(1);
            double rate = ((Number) rateInfo.get(2)).doubleValue();

            // Add forward and reverse rates to the adjacency list
            adjList.computeIfAbsent(from, k -> new ArrayList<>()).add(new Rate(to, rate));
            adjList.computeIfAbsent(to, k -> new ArrayList<>()).add(new Rate(from, 1.0 / rate));
        }
        return adjList;
    }

    public static void main(String[] args) {
        CurrencyConverter converter = new CurrencyConverter();

        // Example from the prompt
        List<List<Object>> rates = new ArrayList<>();
        rates.add(Arrays.asList("USD", "JPY", 110.0));
        rates.add(Arrays.asList("USD", "AUD", 1.45));
        rates.add(Arrays.asList("JPY", "GBP", 0.0070));

        String from = "GBP";
        String to = "AUD";

        double result = converter.findConversionRate(rates, from, to);

        System.out.println("Finding conversion rate from " + from + " to " + to + "...");
        if (result != -1.0) {
            // Path: GBP -> JPY -> USD -> AUD
            // GBP to JPY = 1 / 0.0070
            // JPY to USD = 1 / 110
            // USD to AUD = 1.45
            // Total: (1/0.0070) * (1/110) * 1.45 = ~1.88
            System.out.printf("The conversion rate is: %.2f%n", result);
        } else {
            System.out.println("A conversion path could not be found.");
        }

        // Another test case
        double usdToGbp = converter.findConversionRate(rates, "USD", "GBP");
        System.out.printf("\nUSD to GBP conversion rate is: %.4f%n", usdToGbp); // 110 * 0.0070 = 0.77
    }
}