package cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Data structure:
 *   HashMap<String, CacheNode>  →  O(1) key lookup
 *   Doubly Linked List          →  O(1) move-to-front and evict-from-tail
 *
 *   Thread safety:
 *   ReentrantReadWriteLock
 *     readLock  — multiple concurrent GET threads share this lock
 *     writeLock — single writer for PUT / DELETE / eviction
 */
public class LRUCache {
    private final int capacity;
    private final long defaultTtlMs;
    private Map<String, CacheNode> map;
    private final CacheNode head;
    private final CacheNode tail;
    private int size;

    private final ReentrantReadWriteLock rwLock    = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    private final CacheStats stats = new CacheStats();

    public LRUCache(int cap, long defaultTtlMs){
        if(cap<=0)
            throw new IllegalArgumentException("Capacity must greater than 0");

        this.size = 0;
        this.capacity = cap;
        this.defaultTtlMs = defaultTtlMs;
        this.map = new HashMap<>(cap*2);

        this.head = new CacheNode();
        this.tail = new CacheNode();

        head.next  = tail;
        tail.prev = head;
    }

    /**
     * Insert node immediately after HEAD (MRU position).
     */
    private void addToFront(CacheNode node){
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    /**
     * Unlink node from its current position.
     */
    private void removeNode(CacheNode node){
        node.prev.next = node.next;
        node.next.prev = node.prev;
        node.prev = null;
        node.next = null;
    }

    /**
     * Unlink node from current position and re-link at MRU (after HEAD).
     */
    private void moveToFront(CacheNode node) {
        removeNode(node);
        addToFront(node);
    }

    /**
     * Evict the least recently used node (the node just before TAIL).
     * Caller must hold writeLock.
     */
    private void evictLRU(){
        CacheNode lru = tail.prev;
        if(lru == head)
            return ;

        removeNode(lru);
        map.remove(lru.key);
        size--;
        stats.recordEviction();
    }

    /**
     * PUT — insert or update a key with the default TTL.
     */
    public void put(String key, Object value){
        put(key, value, defaultTtlMs);
    }

    /**
     * PUT — insert or update a key with a custom TTL in milliseconds.
     * ttlMs = 0 means no expiry.
     */
    public void put(String key, Object value, long ttlMs){
        long expiry = (ttlMs > 0) ? System.currentTimeMillis() + ttlMs : 0;

        writeLock.lock();
        try{
            CacheNode existing = map.get(key);
            if(existing != null){
                existing.value = value;
                existing.expiryTimeMs = expiry;
                moveToFront(existing);
            }else{
                if(size == capacity)
                    evictLRU();
                CacheNode node = new CacheNode(key, value, expiry);
                addToFront(node);
                map.put(key, node);
                size++;
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * GET — retrieve value for key.
     * Hit path:
     *   1. readLock: find node in map
     *   2. If expired → upgrade to writeLock, evict, return empty (lazy expiry)
     *   3. Upgrade to writeLock: move node to front (it's now MRU)
     *   4. Return value
     * Miss path: return empty, record miss.
     */
    public Optional<Object> get(String key) {
        //phase 1 lock
        readLock.lock();
        CacheNode node;
        try {
            node = map.get(key);
            if (node == null) {
                stats.recordMisses();
                return Optional.empty();
            }
        } finally {
            readLock.unlock();
        }

        //phase 2 lock
        writeLock.lock();
        try {
            node = map.get(key);
            if (node == null) {
                stats.recordMisses();
                return Optional.empty();
            }

            if (node.isExpired()) {
                removeNode(node);
                map.remove(key);
                size--;
                stats.recordExpiration();
                stats.recordMisses();
                return Optional.empty();
            }

            moveToFront(node);
            stats.recordHits();
            return Optional.of(node.value);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * DELETE — remove a key explicitly. Returns true if key existed.
     */
    public boolean delete(String key){
        CacheNode node;
        writeLock.lock();
        try{
            node = map.get(key);
            if(node == null){
                return false;
            }
            removeNode(node);
            map.remove(key);
            size--;
            return true;
        }finally {
            writeLock.unlock();
        }
    }

    // Current number of entries
    public int size(){
        readLock.lock();
        try{
            return size;
        }finally {
            readLock.unlock();
        }
    }

    public CacheStats getStats(){
        return stats;
    }

    public void clear(){
        writeLock.lock();
        try{
            map.clear();
            head.next = tail;
            tail.prev = head;
            size = 0;
        }finally {
            writeLock.unlock();
        }
    }
}
