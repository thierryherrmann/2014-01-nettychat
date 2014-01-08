package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.bind.DatatypeConverter;

import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.GetPendingMessagesResponse;
import com.thn.netty.chat.primitive.MessageInfo;
import com.thn.netty.chat.primitive.UserName;
import com.thn.netty.chat.util.Util;

/**
 * {@link DelegateCodec} implementation.
 * @author Thierry Herrmann
 */
public class GetPendingMessagesResponseCodec implements DelegateCodec {

    @Override
    public void encode(ChannelHandlerContext aCtx, Command aMsg, ByteBuf aOut) throws Exception {
        GetPendingMessagesResponse cmd = (GetPendingMessagesResponse) aMsg;
        Record rec = Record.forWrite();
        rec.addInt(cmd.getId());
        rec.addString(serializeMessageSenders(cmd.getMessages()));
        rec.addString(serializeMessageTexts(cmd.getMessages()));
        rec.write(aOut);
    }

    @Override
    public void decode(ChannelHandlerContext aCtx, ByteBuf aIn, List<Object> aOut) throws Exception {
        Record rec = Record.read(aIn);
        int cmdId = rec.getInt();
        List<UserName> senders = deserializeSenders(rec.getString());
        List<String> messages = deserializeMessages(rec.getString());
        GetPendingMessagesResponse cmd = new GetPendingMessagesResponse(cmdId, buildMsgs(senders, messages));
        aOut.add(cmd);
    }
    
    private static String serializeMessageSenders(List<MessageInfo> aMessageInfos) {
        List<String> list = new ArrayList<>(aMessageInfos.size());
        for(MessageInfo msgInfo : aMessageInfos) {
            list.add(msgInfo.getSender().getName());
        }
        return serializeStrings(list);
    }
    
    private static String serializeMessageTexts(List<MessageInfo> aMessageInfos) {
        List<String> list = new ArrayList<>(aMessageInfos.size());
        for(MessageInfo msgInfo : aMessageInfos) {
            String base64 = DatatypeConverter.printBase64Binary(
                                              msgInfo.getMessage().getBytes(Util.UTF8));
            list.add(base64);
        }
        return serializeStrings(list);
    }
    
    private static String serializeStrings(List<String> aStrings) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<String> iter = aStrings.iterator(); iter.hasNext(); ) {
            String str = iter.next();
            builder.append(str);
            if (iter.hasNext()) {
                builder.append(','); // string must not contain a ','
            }
        }
        return builder.toString();
    }
    
    private static List<String> deserializeStrings(String aStrings) {
        LinkedList<String> strings = new LinkedList<>();
        for(StringTokenizer tokenizer = new StringTokenizer(aStrings, ","); tokenizer.hasMoreTokens(); ) {
            strings.add(tokenizer.nextToken());
        }
        return strings;
    }
    
    private static ArrayList<UserName> deserializeSenders(String aStrings) {
        List<String> strings = deserializeStrings(aStrings);
        ArrayList<UserName> senders = new ArrayList<>(strings.size());
        for (String string : strings) {
            senders.add(new UserName(string));
        }
        return senders;
    }
    
    private static ArrayList<String> deserializeMessages(String aStrings) {
        List<String> strings = deserializeStrings(aStrings);
        ArrayList<String> messages = new ArrayList<>(strings.size());
        for (String base64 : strings) {
            String msg = new String(DatatypeConverter.parseBase64Binary(base64), Util.UTF8);
            messages.add(msg);
        }
        return messages;
    }
    
    private List<MessageInfo> buildMsgs(List<UserName> aSenders, List<String> aMessages) {
        if (aSenders.size() != aMessages.size()) {
            throw new IllegalStateException("aSenders has not same size as aMessages"); // programming error
        }
        ArrayList<MessageInfo> msgs = new ArrayList<>(aSenders.size());
        for (int i = 0; i < aSenders.size(); i++) {
            msgs.add(new MessageInfo(aSenders.get(i), null, aMessages.get(i)));
        }
        return msgs;
    }
}
