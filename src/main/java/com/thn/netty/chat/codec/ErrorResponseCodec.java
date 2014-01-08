package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.ErrorResponse;
import com.thn.netty.chat.primitive.ErrorResponse.Code;

/**
 * {@link DelegateCodec} implementation.
 * @author Thierry Herrmann
 */
public class ErrorResponseCodec implements DelegateCodec {

    @Override
    public void encode(ChannelHandlerContext aCtx, Command aMsg, ByteBuf aOut) throws Exception {
        ErrorResponse cmd = (ErrorResponse) aMsg;
        Record rec = Record.forWrite();
        rec.addInt(cmd.getId());
        rec.addByte(cmd.getCode().id());
        rec.addString(cmd.getDescription());
        rec.write(aOut);
    }

    @Override
    public void decode(ChannelHandlerContext aCtx, ByteBuf aIn, List<Object> aOut) throws Exception {
        Record rec = Record.read(aIn);
        int cmdId = rec.getInt();
        Code code = Code.forId(rec.getByte());
        String description = rec.getString();
        ErrorResponse cmd = new ErrorResponse(cmdId, code, description);
        aOut.add(cmd);
    }
}
