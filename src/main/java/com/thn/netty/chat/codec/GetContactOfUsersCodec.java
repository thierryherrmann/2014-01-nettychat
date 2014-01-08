package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.GetContactOfUsers;
import com.thn.netty.chat.user.ContactState;

/**
 * {@link DelegateCodec} implementation.
 * @author Thierry Herrmann
 */
public class GetContactOfUsersCodec implements DelegateCodec {

    @Override
    public void encode(ChannelHandlerContext aCtx, Command aMsg, ByteBuf aOut) throws Exception {
        GetContactOfUsers cmd = (GetContactOfUsers) aMsg;
        Record rec = Record.forWrite();
        rec.addInt(cmd.getId());
        rec.addByte(cmd.getContactState().id());
        rec.write(aOut);
    }

    @Override
    public void decode(ChannelHandlerContext aCtx, ByteBuf aIn, List<Object> aOut) throws Exception {
        Record rec = Record.read(aIn);
        int cmdId = rec.getInt();
        ContactState state = ContactState.forId(rec.getByte());
        GetContactOfUsers cmd = new GetContactOfUsers(cmdId, state);
        aOut.add(cmd);
    }
}
