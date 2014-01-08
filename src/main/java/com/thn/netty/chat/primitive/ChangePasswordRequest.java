package com.thn.netty.chat.primitive;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Change password command.
 * @author Thierry Herrmann
 */
public class ChangePasswordRequest extends Command {
    private final UserName mUserName;
    private final String mOldPassword;
    private final String mNewPassword;

    public ChangePasswordRequest(int aCmdId, UserName aUserName, String aOldPassword, String aNewPassword) {
        super(CommandType.CREATE_ACCOUNT, aCmdId);
        mUserName = aUserName;
        mOldPassword = aOldPassword;
        mNewPassword = aNewPassword;
    }

    public UserName getUserName() {
        return mUserName;
    }

    public String getOldPassword()
    {
        return mOldPassword;
    }
    
    public String getNewPassword() {
        return mNewPassword;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .appendSuper(super.toString())
        .append("mUserName", mUserName)
        .append("mOldPassword", mOldPassword)
        .append("mNewPassword", mNewPassword)
        .toString();
    }
}
