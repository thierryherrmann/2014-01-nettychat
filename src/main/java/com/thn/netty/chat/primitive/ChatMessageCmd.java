package com.thn.netty.chat.primitive;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Instant message command.
 * @author Thierry Herrmann
 */
public class ChatMessageCmd extends Command {
    private final MessageInfo mMsgInfo;

    /**
     * Constructor.
     * @param aCmdId command id.
     * @param aMsgInfo details of the message.
     */
    public ChatMessageCmd(int aCmdId, MessageInfo aMsgInfo) {
        super(CommandType.MESSAGE, aCmdId);
        mMsgInfo = aMsgInfo;
    }

    public MessageInfo getMessageInfo() {
        return mMsgInfo;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .appendSuper(super.toString())
        .append("mMsgInfo", mMsgInfo)
        .toString();
    }
}
