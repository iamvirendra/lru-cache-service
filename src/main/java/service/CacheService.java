package service;

import cache.LRUCache;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final LRUCache lruCache;

    public CacheService(LRUCache lruCache){
        this.lruCache = lruCache;
    }

//    public Optional get(String key){
//        return lruCache.get(key);
//    }

    public void put(String key, Object value){
        lruCache.put(key, value);
    }

    public void put(String key, Object value, long ttlMs){
        lruCache.put(key, value, ttlMs);
    }

    public boolean delete(String key){
        lruCache.delete(key);
    }

}
