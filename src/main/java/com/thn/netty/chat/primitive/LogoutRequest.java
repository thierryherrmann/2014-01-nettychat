package com.thn.netty.chat.primitive;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Logout request. After a client has logged out, the connection is still established with the server and the client
 * can login with the same or a different user.
 * @author Thierry Herrmann
 */
public class LogoutRequest extends Command {

    public LogoutRequest(int aCmdId) {
        super(CommandType.LOGOUT, aCmdId);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .appendSuper(super.toString())
        .toString();
    }
}
