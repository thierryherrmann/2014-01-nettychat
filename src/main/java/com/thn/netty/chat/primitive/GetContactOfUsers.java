package com.thn.netty.chat.primitive;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.thn.netty.chat.user.ContactState;

/**
 * Command to get the users that the current user is a contact of, for a given contact state (e.g. PENDING to
 * get from the server (after login) all contact requests from other users).
 * @author Thierry Herrmann
 */
public class GetContactOfUsers extends Command  {
    private final ContactState mState;
    public GetContactOfUsers(int aCmdId, ContactState aState) {
        super(CommandType.GET_CONTACT_OF_USERS, aCmdId);
        if (aState == null) {
            throw new NullPointerException("contact state must not be null");
        }
        mState = aState;
    }
    public ContactState getContactState() {
        return mState;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .appendSuper(super.toString())
        .append("mState", mState)
        .toString();
    }
}
