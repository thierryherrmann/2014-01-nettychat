package com.thn.netty.chat.primitive;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Base class for add contact scenarios.
 * @author Thierry Herrmann
 */
public abstract class BaseAddContactCmd extends Command {
    private final UserName mUserName;
    private final UserName mContactName;

    public BaseAddContactCmd(CommandType aType, int aCmdId, UserName aUserName, UserName aContactName) {
        super(aType, aCmdId);
        mUserName = aUserName;
        mContactName = aContactName;
    }

    public UserName getUserName() {
        return mUserName;
    }

    public UserName getContactName() {
        return mContactName;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .appendSuper(super.toString())
        .append("mUserName", mUserName)
        .append("mContactName", mContactName)
        .toString();
    }
}
