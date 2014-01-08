package com.thn.netty.chat.util;

/**
 * Simple ID generator returning IDs as integers. 
 * @author Thierry Herrmann
 */
public interface IdGenerator {
    /**
     * Returns an ID.
     * @return an ID.
     */
    int nextId();
}
