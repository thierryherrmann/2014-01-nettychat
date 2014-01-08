package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.GetPendingMessages;

/**
 * {@link DelegateCodec} implementation.
 * @author Thierry Herrmann
 */
public class GetPendingMessagesCodec implements DelegateCodec {

    @Override
    public void encode(ChannelHandlerContext aCtx, Command aMsg, ByteBuf aOut) throws Exception {
        GetPendingMessages cmd = (GetPendingMessages) aMsg;
        Record rec = Record.forWrite();
        rec.addInt(cmd.getId());
        rec.write(aOut);
    }

    @Override
    public void decode(ChannelHandlerContext aCtx, ByteBuf aIn, List<Object> aOut) throws Exception {
        Record rec = Record.read(aIn);
        int cmdId = rec.getInt();
        GetPendingMessages cmd = new GetPendingMessages(cmdId);
        aOut.add(cmd);
    }
}
