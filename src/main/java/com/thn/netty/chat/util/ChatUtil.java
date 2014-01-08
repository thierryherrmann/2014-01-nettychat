package com.thn.netty.chat.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public final class ChatUtil {
    private ChatUtil() {
        // prevents instantiation
    }
    
    public static void inspectBuf(ByteBuf aBuffer) {
        ByteBuf buffer = Unpooled.buffer();
        int readerIndex = aBuffer.readerIndex();
        buffer.writeBytes(aBuffer);
        aBuffer.readerIndex(readerIndex);
    }
}
