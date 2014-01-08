package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.LogoutRequest;

/**
 * {@link DelegateCodec} implementation.
 * @author Thierry Herrmann
 */
public class LogoutRequestCodec implements DelegateCodec {

    @Override
    public void encode(ChannelHandlerContext aCtx, Command aMsg, ByteBuf aOut) throws Exception {
        Record rec = Record.forWrite();
        rec.addInt(aMsg.getId());
        rec.write(aOut);
    }

    @Override
    public void decode(ChannelHandlerContext aCtx, ByteBuf aIn, List<Object> aOut) throws Exception {
        Record rec = Record.read(aIn); // even if no data read, read the record length field
        int cmdId = rec.getInt();
        aOut.add(new LogoutRequest(cmdId));
    }
}
