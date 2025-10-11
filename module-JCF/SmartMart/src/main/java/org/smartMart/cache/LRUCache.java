package org.smartMart.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * cache
 * LRUCache<K,V> using LinkedHashMap (accessOrder=true)
 */
public class LRUCache<K,V> extends LinkedHashMap<K,V> {
    private final int capacity;

    public LRUCache(int capacity) {
        super(capacity, 0.75F, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > capacity;
    }
}
