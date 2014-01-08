package com.thn.netty.chat.server;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.thn.netty.chat.primitive.MessageInfo;
import com.thn.netty.chat.primitive.UserId;
import com.thn.netty.chat.primitive.UserName;
import com.thn.netty.chat.server.store.JdbcUserStore;
import com.thn.netty.chat.server.store.StoreException;
import com.thn.netty.chat.server.store.UserStore;
import com.thn.netty.chat.user.ContactInfo;
import com.thn.netty.chat.user.ContactState;
import com.thn.netty.chat.user.UserInfo;

/**
 * Performs all accesses to persistent data. Uses the {@link UserStore}.
 * @author Thierry Herrmann
 */
public class UserManager {
    private static final Logger LOGGER = Logger.getLogger(UserManager.class.getName());
    private final Map<UserName, ChannelInfo> mLoggedInUsers = new ConcurrentHashMap<>();
    private UserStore mStore;
    
    /**
     * Constructor. Creates underlying {@link UserStore}.
     */
    public UserManager() {
        try {
            mStore = new JdbcUserStore(false);
        } catch (StoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a {@link UserInfo}
     * @param aName
     * @return
     */
    public UserInfo getUserByName(UserName aName) {
        try {
            return mStore.getUserByName(aName);
        } catch (StoreException e) {
            throw new RuntimeException(e);
        }
    }
    
    public UserInfo createUser(UserName aName, String aPassword) throws StoreException {
        UserInfo user = mStore.persistUser(new UserInfo(null, aName, aPassword));
        return user;
    }

    public void persistUser(UserInfo aUser) throws StoreException {
        mStore.persistUser(aUser);
    }
    
    public void userLoggedIn(UserInfo aUserInfo, ChannelInfo aChannelInfo) {
        aChannelInfo.setUserInfo(aUserInfo);
        UserName name = aUserInfo.getName();
        mLoggedInUsers.put(name, aChannelInfo);
        LOGGER.info("User " + name.getName() + " logged in");
    }

    public boolean isUserLoggedIn(ChannelInfo aChannelInfo) {
        return aChannelInfo.getUserInfo() != null;
    }
    
    public void userLoggedOut(UserName aUserName) {
        mLoggedInUsers.remove(aUserName);
        LOGGER.info("User " + aUserName.getName() + " logged out");
    }
    
    public ChannelInfo getLoggedInUserChannel(UserName aUserName) {
        ChannelInfo channelInfo = mLoggedInUsers.get(aUserName);
        if (channelInfo != null) {
            // user not logged in or not existing
            return channelInfo;
        }
        // user existing and logged in
        return null;
    }

    public List<ContactInfo> getContactOfUsers(UserId aContactId, ContactState aContactState) throws StoreException {
        try {
            return mStore.getContactOfUsers(aContactId, aContactState);
        } catch (StoreException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void insertMessage(UserId aSenderId, UserId aRecipientId, String aMessage) throws StoreException {
        try {
            mStore.insertMessage(aSenderId, aRecipientId, aMessage);
        } catch (StoreException e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<MessageInfo> getMessages(UserId aRecipientId) throws StoreException {
        try {
            return mStore.getMessages(aRecipientId);
        } catch (StoreException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void deleteMessagesForRecipient(UserId aRecipientId) throws StoreException {
        try {
            mStore.deleteMessagesForRecipient(aRecipientId);
        } catch (StoreException e) {
            throw new RuntimeException(e);
        }
    }

}
