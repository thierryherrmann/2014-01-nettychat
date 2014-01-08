package com.thn.netty.chat.server.store;

/**
 * Exception thrown by {@link UserStore} if anything goes wrong.
 * @author Thierry Herrmann
 */
public class StoreException extends Exception {

    public StoreException(String aMessage, Throwable aCause) {
        super(aMessage, aCause);
    }

    public StoreException(Throwable aCause) {
        super(aCause);
    }

    public StoreException(String aMessage) {
        super(aMessage);
    }
    
    public static class AlreadyExists extends StoreException {
        public AlreadyExists(String aMessage)
        {
            super(aMessage);
        }
        
    }
}
