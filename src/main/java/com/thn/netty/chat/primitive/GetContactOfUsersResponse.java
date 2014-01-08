package com.thn.netty.chat.primitive;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * Response to {@link GetContactOfUsers} containing the list of all users asking the current user to be a contact.
 * @author Thierry Herrmann
 */
public class GetContactOfUsersResponse extends Command  {
    private final List<UserName> mRequesterNames;

    public GetContactOfUsersResponse(int aCmdId, List<UserName> aRequesterNames) {
        super(CommandType.GET_CONTACT_OF_USERS_RESPONSE, aCmdId);
        if (aRequesterNames == null) {
            throw new NullPointerException("aRequesterNames must not be null");
        }
        mRequesterNames = aRequesterNames;
    }

    public List<UserName> getRequesterNames() {
        return mRequesterNames;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .appendSuper(super.toString())
        .append("mRequesterNames", mRequesterNames)
        .toString();
    }
}
