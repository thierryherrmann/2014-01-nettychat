package com.thn.netty.chat.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple {@link IdGenerator} implementation returning IDs as incrementing integers.
 * @author Thierry Herrmann
 */
public class DefaultIdGenerator implements IdGenerator {
    private AtomicInteger mCurrentId = new AtomicInteger(1);
    private static final DefaultIdGenerator INSTANCE = new DefaultIdGenerator();
    private DefaultIdGenerator() {
        // prevents instantiation
    }
    
    /**
     * Returns the singleton instance of the generator.
     * @return the singleton instance of the generator.
     */
    public static DefaultIdGenerator getInstance() {
        return INSTANCE;
    }

    @Override
    public int nextId() {
        return mCurrentId.getAndIncrement();
    }
}
