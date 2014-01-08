package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import com.thn.netty.chat.primitive.AddContactInviteCmd;
import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.UserName;

/**
 * {@link DelegateCodec} implementation.
 * @author Thierry Herrmann
 */
public class AddContactInviteCodec implements DelegateCodec {

    @Override
    public void encode(ChannelHandlerContext aCtx, Command aMsg, ByteBuf aOut) throws Exception {
        AddContactInviteCmd cmd = (AddContactInviteCmd) aMsg;
        Record rec = Record.forWrite();
        rec.addInt(cmd.getId());
        
        UserName userName = cmd.getUserName();
        String userNameString = userName == null ? null : userName.getName();
        rec.addString(userNameString);
        
        UserName contactName = cmd.getContactName();
        String contactNameString = contactName == null ? null : contactName.getName();
        rec.addString(contactNameString);
        
        rec.write(aOut);
    }

    @Override
    public void decode(ChannelHandlerContext aCtx, ByteBuf aIn, List<Object> aOut) throws Exception {
        Record rec = Record.read(aIn);
        int cmdId = rec.getInt();
        
        String userNameString = rec.getString();
        UserName userName = null;
        if (userNameString != null) {
            userName = new UserName(userNameString);
        }
        UserName contactName = null;
        String name = rec.getString();
        if (name != null) {
            contactName = new UserName(name);
        }
        
        AddContactInviteCmd cmd = new AddContactInviteCmd(cmdId, userName, contactName);
        aOut.add(cmd);
    }
}
