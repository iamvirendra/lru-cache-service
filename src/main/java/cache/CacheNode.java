package cache;

/*
 * Node is doubly linked list used in LRUCache.
 * Most recently used node sits just after HEAD.
 * Least recently used node sits just before TAIL.
 * HEAD (sentinel) ↔ [most recent] ↔ [older] ↔ [oldest] ↔ TAIL (sentinel)
*/

public class CacheNode {
    public String key;
    public Object value;
    public long expiryTimeMs; // System.currentTimeMillis() + ttl and  0 means no expiry.

    public CacheNode prev;
    public CacheNode next;

    public CacheNode(String key, Object value, long expiryTimeMs) {
        this.key = key;
        this.value = value;
        this.expiryTimeMs = expiryTimeMs;
    }

    // this is for Head and Tail Node
    public CacheNode() {
    }

    public boolean isExpired() {
        return ((expiryTimeMs != 0) && (System.currentTimeMillis() > expiryTimeMs));
    }
}