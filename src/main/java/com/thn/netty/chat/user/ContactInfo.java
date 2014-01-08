package com.thn.netty.chat.user;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Information about a contact.
 * @author Thierry Herrmann
 */
public class ContactInfo {
    private final UserInfo mContact;
    private final ContactState mState;

    /**
     * Constructor.
     * @param aContact user information about the contact.
     * @param aState contact state. See {@link ContactState}.
     */
    public ContactInfo(UserInfo aContact, ContactState aState) {
        mContact = aContact;
        mState = aState;
    }

    public UserInfo getContact() {
        return mContact;
    }

    public ContactState getState() {
        return mState;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("mContact", mContact)
        .append("mState", mState)
        .toString();
    }

}
