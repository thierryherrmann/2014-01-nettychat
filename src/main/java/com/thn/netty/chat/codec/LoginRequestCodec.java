package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.LoginRequest;
import com.thn.netty.chat.primitive.UserName;

/**
 * {@link DelegateCodec} implementation.
 * @author Thierry Herrmann
 */
public class LoginRequestCodec implements DelegateCodec {

    @Override
    public void encode(ChannelHandlerContext aCtx, Command aMsg, ByteBuf aOut) throws Exception {
        LoginRequest cmd = (LoginRequest) aMsg;
        Record rec = Record.forWrite();
        rec.addInt(cmd.getId());
        rec.addString(cmd.getUserName().getName());
        rec.addString(cmd.getPassword());
        rec.write(aOut);
    }

    @Override
    public void decode(ChannelHandlerContext aCtx, ByteBuf aIn, List<Object> aOut) throws Exception {
        Record rec = Record.read(aIn);
        int cmdId = rec.getInt();
        UserName userName = new UserName(rec.getString());
        String password = rec.getString();
        LoginRequest cmd = new LoginRequest(cmdId, userName, password);
        aOut.add(cmd);
    }
}
