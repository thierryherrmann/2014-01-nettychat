package com.thn.netty.chat.primitive;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Instant message content.
 * @author Thierry Herrmann
 */
public class MessageInfo {
    private static final int MSG_MAX_LENGTH = 1024;
    private final UserName mSender;
    private final UserName mRecipient;
    private final String mMessage;

    /**
     * Chat message.
     * @param aSender sender. Null if sent by the current user.
     * @param aRecipient recipient. Null if received by the current user.
     * @param aMessage message text. Must not be null.
     */
    public MessageInfo(UserName aSender, UserName aRecipient, String aMessage) {
        mSender = aSender;
        mRecipient = aRecipient;
        if (aMessage == null) {
            throw new NullPointerException("message text must not be null");
        }
        if (aMessage.length() > MSG_MAX_LENGTH) {
            throw new IllegalArgumentException("message max length is " + MSG_MAX_LENGTH);
        }
        mMessage = aMessage;
    }

    public UserName getRecipient() {
        return mRecipient;
    }

    public UserName getSender() {
        return mSender;
    }

    public String getMessage()
    {
        return mMessage;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("mSender", mSender)
        .append("mRecipient", mRecipient)
        .append("mMessage", mMessage)
        .toString();
    }
}
