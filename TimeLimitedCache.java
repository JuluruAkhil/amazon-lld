import java.util.HashMap;
import java.util.Map;

public class TimeLimitedCache {

    private static class CacheEntry {
        int value;
        long expiryTime;

        CacheEntry(int value, int duration) {
            this.value = value;
            this.expiryTime = System.currentTimeMillis() + duration;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final Map<Integer, CacheEntry> cache;

    public TimeLimitedCache() {
        cache = new HashMap<>();
    }

    public boolean set(int key, int value, int duration) {
        boolean existedAndNotExpired = false;
        if (cache.containsKey(key)) {
            CacheEntry existing = cache.get(key);
            if (!existing.isExpired()) {
                existedAndNotExpired = true;
            }
        }
        cache.put(key, new CacheEntry(value, duration));
        return existedAndNotExpired;
    }

    public int get(int key) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            cache.remove(key);
            return -1;
        }
        return entry.value;
    }

    public int count() {
        int count = 0;
        for (Map.Entry<Integer, CacheEntry> entry : cache.entrySet()) {
            if (!entry.getValue().isExpired()) {
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) throws InterruptedException {
        TimeLimitedCache cache = new TimeLimitedCache();

        System.out.println("Set key=1, value=42, duration=100ms: " + cache.set(1, 42, 100)); // false
        Thread.sleep(50);
        System.out.println("Get key=1 at 50ms: " + cache.get(1)); // 42
        System.out.println("Count at 50ms: " + cache.count()); // 1
        Thread.sleep(100);
        System.out.println("Get key=1 at 150ms: " + cache.get(1)); // -1
        System.out.println("Count at 150ms: " + cache.count()); // 0

        System.out.println("\nSet key=1, value=42, duration=50ms: " + cache.set(1, 42, 50)); // false
        Thread.sleep(40);
        System.out.println("Set key=1, value=50, duration=100ms: " + cache.set(1, 50, 100)); // true
        Thread.sleep(10);
        System.out.println("Get key=1 at 50ms: " + cache.get(1)); // 50
        Thread.sleep(70);
        System.out.println("Get key=1 at 120ms: " + cache.get(1)); // 50
        Thread.sleep(80);
        System.out.println("Get key=1 at 200ms: " + cache.get(1)); // -1
        System.out.println("Count at 250ms: " + cache.count()); // 0
    }
}
