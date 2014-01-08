package com.thn.netty.chat.primitive;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Command to create a user account.
 * @author Thierry Herrmann
 */
public class CreateAccountRequest extends Command {
    private final UserName mUserName;
    private final String mPassword;

    public CreateAccountRequest(int aCmdId, UserName aUserName, String aPassword) {
        super(CommandType.CREATE_ACCOUNT, aCmdId);
        if (aUserName == null) {
            throw new NullPointerException("user name must not be null");
        }
        if (aPassword == null) {
            throw new NullPointerException("password must not be null");
        }
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
