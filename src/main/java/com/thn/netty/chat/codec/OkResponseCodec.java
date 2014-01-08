package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.OkResponse;

/**
 * {@link DelegateCodec} implementation.
 * @author Thierry Herrmann
 */
public class OkResponseCodec implements DelegateCodec {

    @Override
    public void encode(ChannelHandlerContext aCtx, Command aMsg, ByteBuf aOut) throws Exception {
        // Empty record (to write record length). Command type is enough.
        Record rec = Record.forWrite();
        rec.addInt(aMsg.getId());
        rec.write(aOut);
    }

    @Override
    public void decode(ChannelHandlerContext aCtx, ByteBuf aIn, List<Object> aOut) throws Exception {
        Record rec = Record.read(aIn); // even if no data read, read the record length field
        int cmdId = rec.getInt();
        aOut.add(new OkResponse(cmdId));
    }
}
