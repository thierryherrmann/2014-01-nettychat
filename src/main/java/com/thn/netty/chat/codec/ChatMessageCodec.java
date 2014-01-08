package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import com.thn.netty.chat.primitive.ChatMessageCmd;
import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.MessageInfo;
import com.thn.netty.chat.primitive.UserName;

/**
 * {@link DelegateCodec} implementation.
 * @author Thierry Herrmann
 */
public class ChatMessageCodec implements DelegateCodec {

    @Override
    public void encode(ChannelHandlerContext aCtx, Command aMsg, ByteBuf aOut) throws Exception {
        ChatMessageCmd cmd = (ChatMessageCmd) aMsg;
        MessageInfo msgInfo = cmd.getMessageInfo();
        Record rec = Record.forWrite();
        rec.addInt(cmd.getId());
        
        UserName senderName = msgInfo.getSender();
        String senderNameString = senderName == null ? null : senderName.getName();
        rec.addString(senderNameString);
        
        UserName recipientName = msgInfo.getRecipient();
        String recipientNameString = recipientName == null ? null : recipientName.getName();
        rec.addString(recipientNameString);

        rec.addString(msgInfo.getMessage());
        rec.write(aOut);
    }

    @Override
    public void decode(ChannelHandlerContext aCtx, ByteBuf aIn, List<Object> aOut) throws Exception {
        Record rec = Record.read(aIn);
        int cmdId = rec.getInt();

        String senderNameString = rec.getString();
        UserName senderName = null;
        if (senderNameString != null) {
            senderName = new UserName(senderNameString);
        }

        String recipientNameString = rec.getString();
        UserName recipientName = null;
        if (recipientNameString != null) {
            recipientName = new UserName(recipientNameString);
        }
        
        String message = rec.getString();
        MessageInfo msgInfo = new MessageInfo(senderName, recipientName, message);
        ChatMessageCmd cmd = new ChatMessageCmd(cmdId, msgInfo);
        aOut.add(cmd);
    }
}
