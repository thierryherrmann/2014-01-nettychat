package com.thn.netty.chat.primitive;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Command to remove a contact from the current user's contact list.
 * @author Thierry Herrmann
 */
public class RemoveContactCmd extends Command {
    private final UserName mContactName;

    public RemoveContactCmd(int aCmdId, UserName aContactName) {
        super(CommandType.REMOVE_CONTACT, aCmdId);
        mContactName = aContactName;
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
        .append("mContactName", mContactName)
        .toString();
    }
}
