package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import com.thn.netty.chat.primitive.Command;

/**
 * Encoder/decoder delegate interface. Used to encode/decode commands between the client and the server.
 * @author Thierry Herrmann
 */
public interface DelegateCodec {
    /**
     * Encodes a command.
     * @param aCtx context.
     * @param aMsg command.
     * @param aOut outbound byte buffer.
     * @throws Exception if anything goes wrong.
     */
    void encode(ChannelHandlerContext aCtx, Command aMsg, ByteBuf aOut) throws Exception;

    /**
     * Decodes a command.
     * @param aCtx context.
     * @param aIn byte buffer containing encoded commands.
     * @param aOut list of decoded objects.
     * @throws Exception if anything goes wrong.
     */
    void decode(ChannelHandlerContext aCtx, ByteBuf aIn, List<Object> aOut) throws Exception;

}
