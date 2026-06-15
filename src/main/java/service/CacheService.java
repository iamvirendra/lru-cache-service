package service;

import cache.CacheStats;
import cache.LRUCache;
import exception.CacheKeyNotFoundException;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final LRUCache lruCache;

    public CacheService(LRUCache lruCache){
        this.lruCache = lruCache;
    }

    public Object get(String key){
        return lruCache.get(key)
                .orElseThrow(() -> new CacheKeyNotFoundException(key));
    }

    public void put(String key, Object value){
        lruCache.put(key, value);
    }

    public void put(String key, Object value, long ttlMs){
        lruCache.put(key, value, ttlMs);
    }

    public void delete(String key){
        boolean res = lruCache.delete(key);
        if(!res)
            throw new CacheKeyNotFoundException(key);
    }

    public void clear(){
        lruCache.clear();
    }

    public CacheStats getStats(){
        return lruCache.getStats();
    }

    @PreDestroy
    public void onShutDown(){
        lruCache.shutdown();
    }
}
