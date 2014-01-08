package com.thn.netty.chat.user;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.thn.netty.chat.primitive.UserId;
import com.thn.netty.chat.primitive.UserName;

/**
 * Information about a user.
 * @author Thierry Herrmann
 */
public class UserInfo {
    private final UserId mId;
    private final UserName mName;
    private final String mPassword;
    // contacts list makes this class not immutable but this can be easily fixed.
    private final List<ContactInfo> mContacts = new LinkedList<>();
    
    /**
     * Constructor.
     * @param aId user id.
     * @param aName user name.
     * @param aPassword password.
     */
    public UserInfo(UserId aId, UserName aName, String aPassword) {
        mId = aId;
        mName = aName;
        mPassword = aPassword;
    }

    public UserId getId() {
        return mId;
    }

    public UserName getName() {
        return mName;
    }

    public String getPassword()
    {
        return mPassword;
    }

    public void setContacts(List<ContactInfo> aContacts) {
        mContacts.clear();
        mContacts.addAll(aContacts);
    }
    
    public List<ContactInfo> getContacts() {
        return mContacts;
    }

    public boolean equalsIgnoreContacts(UserInfo aObject) {
        if (this == aObject) {
            return true;
        }
        if (aObject == null) {
            return false;
        }
        if (getClass() != aObject.getClass()) {
            return false;
        }
        UserInfo other = (UserInfo) aObject;
        if (mId == null) {
            if (other.mId != null) {
                return false;
            }
        } else if (!mId.equals(other.mId)) {
            return false;
        }
        if (mName == null) {
            if (other.mName != null) {
                return false;
            }
        } else if (!mName.equals(other.mName)) {
            return false;
        }
        if (mPassword == null) {
            if (other.mPassword != null) {
                return false;
            }
        } else if (!mPassword.equals(other.mPassword)) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("mId", mId)
        .append("mName", mName)
        .append("mPassword", mPassword)
        .append("mContacts", mContacts)
        .toString();
    }
    
}
