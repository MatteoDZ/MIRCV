package it.unipi.dii.aide.mircv.index.merge;

import java.util.*;

/**
 * Represents a Least Frequently Used (LFU) cache.
 *
 * @param <K> The type of keys.
 * @param <V> The type of values.
 */
public class LFUCache<K, V> {
    private final int capacity;
    private final Map<K, V> cache;  // Main cache storing key-value pairs
    private final Map<K, Integer> usageCounts;  // Map to store the usage counts of keys
    private final Map<Integer, LinkedHashSet<K>> frequencyLists;  // Map to store keys based on their frequencies
    private final TreeSet<LFUItem> lfuItems;  // TreeSet to maintain LFUItems in ascending order of frequency

    /**
     * Initializes a new instance of LFUCache with the specified capacity.
     *
     * @param capacity The maximum number of elements the cache can hold.
     */
    public LFUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.usageCounts = new HashMap<>();
        this.frequencyLists = new HashMap<>();
        this.lfuItems = new TreeSet<>();
    }

    /**
     * Gets the value associated with the specified key from the cache.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value to which the specified key is mapped, or null if the cache does not contain the key.
     */
    public V get(K key) {
        if (!cache.containsKey(key)) {
            return null;
        }

        int frequency = usageCounts.get(key);
        LFUItem item = new LFUItem(key, frequency);
        lfuItems.remove(item);

        usageCounts.put(key, frequency + 1);

        if (!frequencyLists.containsKey(frequency + 1)) {
            frequencyLists.put(frequency + 1, new LinkedHashSet<>());
        }

        frequencyLists.get(frequency + 1).add(key);

        lfuItems.add(new LFUItem(key, frequency + 1));

        return cache.get(key);
    }
    /**
     * Clears the cache, removing all entries.
     */
    public void clear() {
        cache.clear();
        usageCounts.clear();
        frequencyLists.clear();
        lfuItems.clear();
    }

    /**
     * Puts the specified key-value pair into the cache.
     *
     * @param key   The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     */
    public void put(K key, V value) {
        if (capacity == 0) {
            return;
        }

        if (cache.size() >= capacity && !cache.containsKey(key)) {
            removeLFU();
        }

        if (!cache.containsKey(key)) {
            usageCounts.put(key, 1);
        }

        cache.put(key, value);
        lfuItems.add(new LFUItem(key, 1));
    }

    /**
     * Checks if the cache contains the specified key.
     *
     * @param key The key to be checked.
     * @return true if the cache contains the specified key, false otherwise.
     */
    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    /**
     * Removes the Least Frequently Used (LFU) item from the cache.
     */
    private void removeLFU() {
        LFUItem lfuItem = lfuItems.first();
        K keyToRemove = lfuItem.key;
        lfuItems.remove(lfuItem);
        cache.remove(keyToRemove);
        usageCounts.remove(keyToRemove);
    }

    /**
     * Represents an item in the Least Frequently Used (LFU) cache.
     */
    private class LFUItem implements Comparable<LFUItem> {
        K key;
        int frequency;

        /**
         * Initializes a new instance of LFUItem with the specified key and frequency.
         *
         * @param key       The key of the LFUItem.
         * @param frequency The frequency of the LFUItem.
         */
        public LFUItem(K key, int frequency) {
            this.key = key;
            this.frequency = frequency;
        }

        /**
         * Compares this LFUItem with another LFUItem based on their frequencies and keys.
         *
         * @param other The LFUItem to be compared.
         * @return A negative integer, zero, or a positive integer as this object is less than, equal to,
         *         or greater than the specified object.
         */
        @Override
        public int compareTo(LFUItem other) {
            int frequencyComparison = Integer.compare(this.frequency, other.frequency);
            if (frequencyComparison == 0) {
                // If frequencies are equal, use key comparison
                return key.hashCode() - other.key.hashCode();
            }
            return frequencyComparison;
        }

    }
}
