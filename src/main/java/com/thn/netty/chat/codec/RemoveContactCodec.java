package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.RemoveContactCmd;
import com.thn.netty.chat.primitive.UserName;

/**
 * {@link DelegateCodec} implementation.
 * @author Thierry Herrmann
 */
public class RemoveContactCodec implements DelegateCodec {

    @Override
    public void encode(ChannelHandlerContext aCtx, Command aMsg, ByteBuf aOut) throws Exception {
        RemoveContactCmd cmd = (RemoveContactCmd) aMsg;
        Record rec = Record.forWrite();
        rec.addInt(cmd.getId());
        UserName contactName = cmd.getContactName();
        String contactNameString = contactName == null ? null : contactName.getName();
        rec.addString(contactNameString);
        rec.write(aOut);
    }

    @Override
    public void decode(ChannelHandlerContext aCtx, ByteBuf aIn, List<Object> aOut) throws Exception {
        Record rec = Record.read(aIn);
        int cmdId = rec.getInt();
        UserName contactName = null;
        String name = rec.getString();
        if (name != null) {
            contactName = new UserName(name);
        }
        RemoveContactCmd cmd = new RemoveContactCmd(cmdId, contactName);
        aOut.add(cmd);
    }
}
