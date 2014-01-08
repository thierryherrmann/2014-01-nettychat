package com.thn.netty.chat.server.store;

import java.util.List;

import com.thn.netty.chat.primitive.MessageInfo;
import com.thn.netty.chat.primitive.UserId;
import com.thn.netty.chat.primitive.UserName;
import com.thn.netty.chat.user.ContactInfo;
import com.thn.netty.chat.user.ContactState;
import com.thn.netty.chat.user.UserInfo;

/**
 * Persistent store to persist everything about users (contacts, contact requests, offline messages...)
 * @author Thierry Herrmann
 */
public interface UserStore {

    void createTables() throws StoreException;
    void dropTables() throws StoreException;

    UserInfo persistUser(UserInfo aUser) throws StoreException;
    UserInfo getUserById(UserId aUserId) throws StoreException;
    UserInfo getUserByName(UserName aUserName) throws StoreException;
    void deleteUser(UserId aUserId) throws StoreException;

    void updateContact(UserId aUserId, UserId aContactId, ContactState aState) throws StoreException;
    List<ContactInfo> getContacts(UserId aUserId) throws StoreException;
    List<ContactInfo> getContactOfUsers(UserId aUserId, ContactState aContactState) throws StoreException;
    void deleteContact(UserId aUserId, UserId aContactId) throws StoreException;
    
    void insertMessage(UserId aSenderId, UserId aRecipientId, String aMessage) throws StoreException;
    List<MessageInfo> getMessages(UserId aRecipientId) throws StoreException;
    void deleteMessagesForRecipient(UserId aRecipientId) throws StoreException;

    void destroy() throws StoreException;
}
