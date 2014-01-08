package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.GetContactOfUsersResponse;
import com.thn.netty.chat.primitive.UserName;

/**
 * {@link DelegateCodec} implementation.
 * @author Thierry Herrmann
 */
public class GetContactOfUsersResponseCodec implements DelegateCodec {

    @Override
    public void encode(ChannelHandlerContext aCtx, Command aMsg, ByteBuf aOut) throws Exception {
        GetContactOfUsersResponse cmd = (GetContactOfUsersResponse) aMsg;
        Record rec = Record.forWrite();
        rec.addInt(cmd.getId());
        rec.addString(serializeUserNames(cmd.getRequesterNames()));
        rec.write(aOut);
    }

    @Override
    public void decode(ChannelHandlerContext aCtx, ByteBuf aIn, List<Object> aOut) throws Exception {
        Record rec = Record.read(aIn);
        int cmdId = rec.getInt();
        List<UserName> userNames = deserializeUserNames(rec.getString());
        GetContactOfUsersResponse cmd = new GetContactOfUsersResponse(cmdId, userNames);
        aOut.add(cmd);
    }
    
    private static String serializeUserNames(List<UserName> aUserNames) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<UserName> iter = aUserNames.iterator(); iter.hasNext(); ) {
            UserName userName = iter.next();
            builder.append(userName.getName());
            if (iter.hasNext()) {
                builder.append(','); // username must not contain a ','
            }
        }
        return builder.toString();
    }
    
    private static List<UserName> deserializeUserNames(String aUserNames) {
        LinkedList<UserName> userNames = new LinkedList<>();
        for(StringTokenizer tokenizer = new StringTokenizer(aUserNames, ","); tokenizer.hasMoreTokens(); ) {
            userNames.add(new UserName(tokenizer.nextToken()));
        }
        return userNames;
    }
}
