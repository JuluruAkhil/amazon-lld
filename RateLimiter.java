import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Defines the contract for any rate limiting algorithm. This allows for
 * different
 * strategies (like token bucket, leaky bucket, etc.) to be used
 * interchangeably.
 */
interface RateLimitAlgo {
    /**
     * Checks if a request from a given client is allowed. If it is, the
     * internal state is updated to reflect the new request.
     * 
     * @param clientId The unique identifier for the client making the request.
     * @return true if the request is allowed, false otherwise.
     */
    boolean allow(String clientId);

    /**
     * Gets the number of remaining requests a client can make within the current
     * window.
     * 
     * @param clientId The unique identifier for the client.
     * @return The number of remaining allowed requests.
     */
    int getRemaining(String clientId);
}

/**
 * A concrete implementation of the RateLimitAlgo using the Sliding Window Log
 * algorithm.
 * It tracks request timestamps for each client in a deque.
 */
class SlidingWindowAlgo implements RateLimitAlgo {
    private final int capacity; // Maximum number of requests allowed
    private final long windowDurationMillis; // The time window duration in milliseconds
    private final Map<String, Deque<Long>> clientRequests; // Stores timestamps of requests for each client

    public SlidingWindowAlgo(int capacity, int duration, TimeUnit timeUnit) {
        this.capacity = capacity;
        this.windowDurationMillis = timeUnit.toMillis(duration);
        this.clientRequests = new HashMap<>();
    }

    @Override
    public synchronized boolean allow(String clientId) {
        long currentTimeMillis = Instant.now().toEpochMilli();
        Deque<Long> timestamps = clientRequests.computeIfAbsent(clientId, k -> new ArrayDeque<>());

        // Remove timestamps that are outside the current sliding window
        removeExpired(timestamps, currentTimeMillis);

        // If the number of requests is less than the capacity, allow and record the new
        // request
        if (timestamps.size() < capacity) {
            timestamps.addLast(currentTimeMillis);
            return true;
        }

        // Otherwise, block the request
        return false;
    }

    @Override
    public synchronized int getRemaining(String clientId) {
        long currentTimeMillis = Instant.now().toEpochMilli();
        Deque<Long> timestamps = clientRequests.getOrDefault(clientId, new ArrayDeque<>());

        // Ensure we only count requests within the current window
        removeExpired(timestamps, currentTimeMillis);

        return Math.max(0, capacity - timestamps.size());
    }

    /**
     * Helper method to remove timestamps from the deque that are older than the
     * window duration.
     * 
     * @param timestamps        The deque of request timestamps for a client.
     * @param currentTimeMillis The current time in milliseconds.
     */
    private void removeExpired(Deque<Long> timestamps, long currentTimeMillis) {
        long windowStartMillis = currentTimeMillis - windowDurationMillis;
        while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStartMillis) {
            timestamps.pollFirst();
        }
    }
}

/**
 * A concrete implementation of the RateLimitAlgo using the Fixed Window
 * algorithm.
 * It tracks the count of requests per client in each fixed window interval.
 */
class FixedWindowAlgo implements RateLimitAlgo {
    private static class WindowInfo {
        long windowStartMillis;
        int requestCount;
    }

    private final int capacity; // Maximum number of requests allowed per window
    private final long windowDurationMillis; // The time window duration in milliseconds
    private final Map<String, WindowInfo> clientWindows;

    public FixedWindowAlgo(int capacity, int duration, TimeUnit timeUnit) {
        this.capacity = capacity;
        this.windowDurationMillis = timeUnit.toMillis(duration);
        this.clientWindows = new HashMap<>();
    }

    @Override
    public synchronized boolean allow(String clientId) {
        long currentTimeMillis = Instant.now().toEpochMilli();
        WindowInfo info = clientWindows.computeIfAbsent(clientId, k -> new WindowInfo());
        long windowStart = info.windowStartMillis;
        if (windowStart == 0 || currentTimeMillis - windowStart >= windowDurationMillis) {
            // New window
            info.windowStartMillis = currentTimeMillis;
            info.requestCount = 1;
            return true;
        } else {
            if (info.requestCount < capacity) {
                info.requestCount++;
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public synchronized int getRemaining(String clientId) {
        long currentTimeMillis = Instant.now().toEpochMilli();
        WindowInfo info = clientWindows.getOrDefault(clientId, new WindowInfo());
        long windowStart = info.windowStartMillis;
        if (windowStart == 0 || currentTimeMillis - windowStart >= windowDurationMillis) {
            return capacity;
        } else {
            return Math.max(0, capacity - info.requestCount);
        }
    }
}

/**
 * The RateLimiter class acts as a facade, providing a simple interface to the
 * underlying rate limiting algorithm.
 */
public class RateLimiter {
    private final RateLimitAlgo algorithm;

    public RateLimiter(RateLimitAlgo algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Determines if a request from a client should be allowed.
     * 
     * @param clientId The ID of the client making the request.
     * @return true if the request is allowed, false if it is blocked.
     */
    public boolean isAllowed(String clientId) {
        return algorithm.allow(clientId);
    }

    public int getRemainingRequests(String clientId) {
        return algorithm.getRemaining(clientId);
    }

    /**
     * Main method to demonstrate the RateLimiter functionality.
     */
    public static void main(String[] args) throws InterruptedException {
        // --- Setup ---
        // Create a rate limiter that allows 5 requests every 10 seconds.
        RateLimitAlgo algo = new SlidingWindowAlgo(5, 10, TimeUnit.SECONDS);
        RateLimiter limiter = new RateLimiter(algo);
        String client1 = "user-123";
        String client2 = "user-456";

        System.out.println("--- Testing Rate Limiter (5 requests per 10 seconds) ---");

        // --- Test 1: Single client hitting the limit ---
        System.out.println("\n--- Test 1: " + client1 + " making requests ---");
        for (int i = 0; i < 7; i++) {
            boolean allowed = limiter.isAllowed(client1);
            int remaining = limiter.getRemainingRequests(client1);
            System.out.println("Request " + (i + 1) + " for " + client1 + ": " + (allowed ? "ALLOWED" : "BLOCKED")
                    + " (Remaining: " + remaining + ")");
            TimeUnit.MILLISECONDS.sleep(500); // 0.5-second delay between requests
        }

        // --- Test 2: Different clients have their own limits ---
        System.out.println("\n--- Test 2: " + client2 + " has a separate limit ---");
        boolean client2Allowed = limiter.isAllowed(client2);
        System.out.println("First request for " + client2 + ": " + (client2Allowed ? "ALLOWED" : "BLOCKED")
                + " (Remaining: " + limiter.getRemainingRequests(client2) + ")");

        // --- Test 3: Waiting for the window to slide ---
        System.out.println("\n--- Test 3: Waiting for window to slide... ---");
        System.out.println("Waiting for 8 seconds...");
        TimeUnit.SECONDS.sleep(8);

        System.out.println(client1 + " trying again after waiting...");
        for (int i = 0; i < 3; i++) {
            boolean allowed = limiter.isAllowed(client1);
            int remaining = limiter.getRemainingRequests(client1);
            System.out.println("Request " + (i + 1) + " for " + client1 + ": " + (allowed ? "ALLOWED" : "BLOCKED")
                    + " (Remaining: " + remaining + ")");
        }
    }
}
