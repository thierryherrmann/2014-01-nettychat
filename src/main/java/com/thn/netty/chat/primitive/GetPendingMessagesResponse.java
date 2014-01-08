package com.thn.netty.chat.primitive;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Response to {@link GetPendingMessages} command.
 * @author Thierry Herrmann
 */
public class GetPendingMessagesResponse extends Command  {
    private final List<MessageInfo> mMessages;

    public GetPendingMessagesResponse(int aCmdId, List<MessageInfo> aMessages) {
        super(CommandType.GET_PENDING_MESSAGES_RESPONSE, aCmdId);
        if (aMessages == null) {
            throw new NullPointerException("aMessages must not be null");
        }
        mMessages = aMessages;
    }

    public List<MessageInfo> getMessages() {
        return mMessages;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .appendSuper(super.toString())
        .append("mMessages", mMessages)
        .toString();
    }
}
