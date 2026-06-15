package exception;

public class CacheKeyNotFoundException extends RuntimeException{

    public CacheKeyNotFoundException(String key){
        super("Key not found: " + key);
    }
}
