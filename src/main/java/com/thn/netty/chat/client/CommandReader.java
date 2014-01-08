package com.thn.netty.chat.client;

import com.thn.netty.chat.primitive.Command;

/**
 * Interface defining how to get commands.
 * @author Thierry Herrmann
 */
public interface CommandReader {
    
    /**
     * Returns the next command.
     * @return the next command or null if end of stream of commands reached.
     * @throws BadCommandException if a problem was encountered when getting the next command.
     */
    Command getCommand() throws BadCommandException;
    
    /**
     * Exception thrown when creating a command failed (bad arguments...).
     */
    public static class BadCommandException extends Exception {
        public BadCommandException(String aMessage) {
            super(aMessage);
        }
    }
}
