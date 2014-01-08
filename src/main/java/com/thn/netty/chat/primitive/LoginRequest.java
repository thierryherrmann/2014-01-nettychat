package com.thn.netty.chat.primitive;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Login request.
 * @author Thierry Herrmann
 */
public class LoginRequest extends Command {
    private final UserName mUserName;
    private final String mPassword;

    public LoginRequest(int aCmdId, UserName aUserName, String aPassword) {
        super(CommandType.LOGIN, aCmdId);
        mUserName = aUserName;
        mPassword = aPassword;
    }

    public UserName getUserName() {
        return mUserName;
    }

    
    public String getPassword()
    {
        return mPassword;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .appendSuper(super.toString())
        .append("mUserName", mUserName)
        .append("mPassword", mPassword)
        .toString();
    }
}
