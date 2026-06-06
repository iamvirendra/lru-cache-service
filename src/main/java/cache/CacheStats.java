package cache;

import java.util.concurrent.atomic.AtomicLong;

public class CacheStats {

    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final AtomicLong eviction = new AtomicLong();
    private final AtomicLong expiration = new AtomicLong();

    public void recordHits(){
        hits.incrementAndGet();
    }
    public void recordMisses(){
        misses.incrementAndGet();
    }
    public void recordEviction(){
        eviction.incrementAndGet();
    }
    public void recordExpiration(){
        expiration.incrementAndGet();
    }

    public long getHits(){
        return hits.get();
    }
    public long getMisses(){
        return misses.get();
    }
    public long getEviction(){
        return eviction.get();
    }
    public long getExpiration(){
        return expiration.get();
    }

    public void reset(){
        hits.set(0);
        misses.set(0);
        eviction.set(0);
        expiration.set(0);
    }

}
