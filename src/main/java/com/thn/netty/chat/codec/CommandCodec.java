package com.thn.netty.chat.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.EnumMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.CommandType;

/**
 * {@link ByteToMessageCodec} implementation to encode/decode commands. Knows the relationship between the
 * command types and the {@link DelegateCodec} implementation and calls the right delegate.
 * @author Thierry Herrmann
 */
public class CommandCodec extends ByteToMessageCodec<Command> {
    private static final Logger LOGGER = Logger.getLogger(CommandCodec.class.getName());
    
    private static final EnumMap<CommandType, DelegateCodec> DELEGATES = new EnumMap<>(CommandType.class);
    static {
       DELEGATES.put(CommandType.CREATE_ACCOUNT, new CreateAccountRequestCodec());
       DELEGATES.put(CommandType.CHANGE_PASSWORD, new ChangePasswordRequestCodec());
       DELEGATES.put(CommandType.LOGOUT, new LogoutRequestCodec());
       DELEGATES.put(CommandType.EXIT, new ExitRequestCodec());
       DELEGATES.put(CommandType.LOGIN, new LoginRequestCodec());
       DELEGATES.put(CommandType.ADD_CONTACT_INVITE, new AddContactInviteCodec());
       DELEGATES.put(CommandType.ADD_CONTACT_RESPONSE, new AddContactResponseCodec());
       DELEGATES.put(CommandType.REMOVE_CONTACT, new RemoveContactCodec());
       DELEGATES.put(CommandType.GET_CONTACT_OF_USERS, new GetContactOfUsersCodec());
       DELEGATES.put(CommandType.GET_CONTACT_OF_USERS_RESPONSE, new GetContactOfUsersResponseCodec());
       DELEGATES.put(CommandType.MESSAGE, new ChatMessageCodec());
       DELEGATES.put(CommandType.GET_PENDING_MESSAGES, new GetPendingMessagesCodec());
       DELEGATES.put(CommandType.GET_PENDING_MESSAGES_RESPONSE, new GetPendingMessagesResponseCodec());
       DELEGATES.put(CommandType.SHUTDOWN_SERVER, new ShutdownRequestCodec());
       DELEGATES.put(CommandType.OK, new OkResponseCodec());
       DELEGATES.put(CommandType.ERROR, new ErrorResponseCodec());
    }
    @Override
    protected void encode(ChannelHandlerContext aCtx, Command aMsg, ByteBuf aOut) throws Exception {
        CommandType cmdType = aMsg.getType();
        DelegateCodec codec = DELEGATES.get(cmdType);
        if (codec == null) {
            LOGGER.warn("no codec for command type: " + cmdType);
            return;
        }
        aOut.writeByte(cmdType.id());
        codec.encode(aCtx, aMsg, aOut);
    }

    @Override
    protected void decode(ChannelHandlerContext aCtx, ByteBuf aIn, List<Object> aOut) throws Exception {
        if (aIn.readableBytes() == 0) {
            return;
        }
        byte cmdTypeInt = aIn.readByte();
        CommandType cmdType;
        try {
            cmdType = CommandType.forId(cmdTypeInt);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("unknown command type", e);
            return;
        }
        DelegateCodec codec = DELEGATES.get(cmdType);
        if (codec == null) {
            LOGGER.warn("no codec for command type: " + cmdType);
            return;
        }
        codec.decode(aCtx, aIn, aOut);
    }

}
