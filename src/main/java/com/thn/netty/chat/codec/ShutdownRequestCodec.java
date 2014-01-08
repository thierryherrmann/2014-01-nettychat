package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.ShutdownServerRequest;

/**
 * {@link DelegateCodec} implementation.
 * @author Thierry Herrmann
 */
public class ShutdownRequestCodec implements DelegateCodec {

    @Override
    public void encode(ChannelHandlerContext aCtx, Command aMsg, ByteBuf aOut) throws Exception {
        ShutdownServerRequest cmd = (ShutdownServerRequest) aMsg;
        Record rec = Record.forWrite();
        rec.addInt(cmd.getId());
        rec.write(aOut);
    }

    @Override
    public void decode(ChannelHandlerContext aCtx, ByteBuf aIn, List<Object> aOut) throws Exception {
        Record rec = Record.read(aIn);
        int cmdId = rec.getInt();
        ShutdownServerRequest cmd = new ShutdownServerRequest(cmdId);
        aOut.add(cmd);
    }
}
