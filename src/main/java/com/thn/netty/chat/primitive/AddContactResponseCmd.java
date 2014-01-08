package com.thn.netty.chat.primitive;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Add contact response. See {@link AddContactInviteCmd} for messages exchanges.
 * @author Thierry Herrmann
 */
public class AddContactResponseCmd extends BaseAddContactCmd {
    private boolean mAccepted;

    /**
     * Invite response to add a contact.
     * @param aCmdId command id.
     * @param aUserName name of the requester user. Null for the current user.
     * @param aContactName name of the new contact. Null for the current user.  
     * @param aAccepted true if the contact has accepted the request, false if has declined.
     */
    public AddContactResponseCmd(int aCmdId, UserName aUserName, UserName aContactName, 
                                       boolean aAccepted) {
        super(CommandType.ADD_CONTACT_RESPONSE, aCmdId, aUserName, aContactName);
        mAccepted = aAccepted;
    }
    
    public boolean isAccepted() {
        return mAccepted;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("mAccepted", mAccepted).toString();
    }
}
