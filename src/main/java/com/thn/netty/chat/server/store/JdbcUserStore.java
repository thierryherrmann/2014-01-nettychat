package com.thn.netty.chat.server.store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.thn.netty.chat.primitive.MessageInfo;
import com.thn.netty.chat.primitive.UserId;
import com.thn.netty.chat.primitive.UserName;
import com.thn.netty.chat.user.ContactInfo;
import com.thn.netty.chat.user.ContactState;
import com.thn.netty.chat.user.UserInfo;

/**
 * {@link UserStore} default implementation with JDBC. This implementation if VERY simple and uses the embedded
 * H2 JDBC driver. It is not at all optimized (e.g. no connection pools, no batched requests...) as this project
 * focuses on Netty.
 * @author Thierry Herrmann
 */
public class JdbcUserStore implements UserStore {

    @Override
    public void createTables() throws StoreException {
        try {
            Statement stmt = mConn.createStatement();
            stmt.execute("create table User (userid int PRIMARY KEY AUTO_INCREMENT, username varchar(30), "
                    + "password varchar(30)); "
                    + "ALTER TABLE User ADD CONSTRAINT USERNAME_UNIQUE UNIQUE(username)");
            stmt.execute("create table Contact (userid int NOT NULL, contactid int NOT NULL, state tinyint NOT NULL);"
                    + "ALTER TABLE Contact ADD PRIMARY KEY (userid, contactid);"
                    + "ALTER TABLE Contact ADD FOREIGN KEY (userid) REFERENCES User(userId);"
                    + "ALTER TABLE Contact ADD FOREIGN KEY (contactid) REFERENCES User(userId);");
            stmt.execute("create table Message (msgid int PRIMARY KEY AUTO_INCREMENT, "
            		+ "senderid int NOT NULL, recipientid int NOT NULL, message varchar(1024) NOT NULL);"
                    + "ALTER TABLE Message ADD FOREIGN KEY (senderid) REFERENCES User(userId);"
                    + "ALTER TABLE Message ADD FOREIGN KEY (recipientid) REFERENCES User(userId);");
        } catch (SQLException e) {
            throw new StoreException("Could not create user table", e);
        }
    }

    @Override
    public void dropTables() throws StoreException {
        try {
            Statement stmt = mConn.createStatement();
            stmt.execute("drop table if exists Message");
            stmt.execute("drop table if exists Contact");
            stmt.execute("drop table if exists User");
            stmt.close();
        } catch (SQLException e) {
            throw new StoreException("Could not drop user table", e);
        }
    }

    @Override
    public UserInfo persistUser(UserInfo aUser) throws StoreException {
        UserInfo existingUser = getUserByName(aUser.getName());
        UserInfo persistedUser;
        if (existingUser == null) {
            // new user
            persistedUser = insertUser(aUser);
        } else {
            if (aUser.equalsIgnoreContacts(existingUser)) {
                // user without contacts exists and hasn't changed
                persistedUser = aUser;
            } else {
                // user without contacts exists but changed
                if (aUser.getId() == null) { 
                    // trying to recreate an existing user. Re-use its id
                    persistedUser = updateUser(new UserInfo(existingUser.getId(), aUser.getName(), 
                                                            aUser.getPassword()));                    
                } else {
                    persistedUser = updateUser(aUser);
                }
            }
        }
        List<ContactInfo> existingContacts;
        if (existingUser == null) {
            existingContacts = Collections.emptyList();
        } else {
            existingContacts = existingUser.getContacts();
        }
        persistContacts(persistedUser.getId(), aUser.getContacts(), existingContacts);
        return persistedUser;
    }

    private void persistContacts(UserId aUserId, List<ContactInfo> aContactsToPersist, 
                                 List<ContactInfo> aExistingContacts)
            throws StoreException {
        List<UserName> namesToPersist = getContactNames(aContactsToPersist);
        List<UserName> existingNames = getContactNames(aExistingContacts);
        Set<UserName> namesToInsert = subtractNames(namesToPersist, existingNames);
        Set<UserName> namesToDelete = subtractNames(existingNames, namesToPersist);
        Set<UserName> namesToKeep = new HashSet<>(existingNames);
        namesToKeep.retainAll(namesToPersist);
        
        // insert new contacts and delete old contacts
        List<ContactInfo> contactsToInsert = filterContacts(aContactsToPersist, namesToInsert);
        List<ContactInfo> contactsToDelete = filterContacts(aExistingContacts, namesToDelete);
        for(ContactInfo contact : contactsToInsert) {
            insertContact(aUserId, contact.getContact().getId(), contact.getState());
        }
        for(ContactInfo contact : contactsToDelete) {
            deleteContact(aUserId, contact.getContact().getId());
        }
        
        // update contacts that changed
        List<ContactInfo> contactsToPersistToKeep = filterContacts(aContactsToPersist, namesToKeep);
        Map<UserName,ContactInfo> existingContactsToKeep = filterContactsAsMap(aExistingContacts, namesToKeep);
        for(ContactInfo contactToPersist : contactsToPersistToKeep) {
            ContactInfo existingContact = existingContactsToKeep.get(contactToPersist.getContact().getName());
            if (contactToPersist.getState() != existingContact.getState()) {
                updateContact(aUserId, contactToPersist.getContact().getId(), contactToPersist.getState());
            }
        }
    }
    
    private static List<UserName> getContactNames(List<ContactInfo> aContacts) {
        List<UserName> names = new ArrayList<>(aContacts.size());
        for (ContactInfo contactInfo : aContacts) {
            names.add(contactInfo.getContact().getName());
        }
        return names;
    }
    
    private static Set<UserName> subtractNames(List<UserName> aNames, List<UserName> aNamesToSubtract) {
        Set<UserName> names = new HashSet<>(aNames);
        Set<UserName> namesToSubtract = new HashSet<>(aNamesToSubtract);
        names.removeAll(namesToSubtract);
        return names;
    }
    
    private static List<ContactInfo> filterContacts(List<ContactInfo> aUsers, Set<UserName> aNames) {
        List<ContactInfo> users = new LinkedList<>();
        for(ContactInfo user : aUsers) {
            if (aNames.contains(user.getContact().getName())) {
                users.add(user);
            }
        }
        return users;
    }
    
    private static Map<UserName,ContactInfo> filterContactsAsMap(List<ContactInfo> aUsers, Set<UserName> aNames) {
        Map<UserName,ContactInfo> map = new HashMap<>();
        for(ContactInfo user : aUsers) {
            if (aNames.contains(user.getContact().getName())) {
                map.put(user.getContact().getName(), user);
            }
        }
        return map;
    }
    
    public UserInfo insertUser(UserInfo aUser) throws StoreException {
        try {
            PreparedStatement stmt = mConn.prepareStatement("insert into User (username, password) VALUES (?,?)");
            stmt.setString(1, aUser.getName().getName());
            stmt.setString(2, aUser.getPassword());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            UserId userId = null;
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                userId = new UserId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("Creating user failed, no generated key obtained.");
            }
            stmt.close();
            UserInfo insertedUser = new UserInfo(userId, aUser.getName(), aUser.getPassword());
            insertedUser.setContacts(aUser.getContacts());
            return insertedUser;
        } catch (SQLException e) {
            StringBuilder builder = new StringBuilder("Could not insert user: ");
            builder.append(aUser);
            String sqlState = e.getSQLState();
            if (sqlState != null && sqlState.equals("23505")) {
                builder.append("; already exists");
                throw new StoreException.AlreadyExists(builder.toString());
            }
            throw new StoreException(builder.toString());
        }
    }

    private UserInfo updateUser(UserInfo aUser) throws StoreException {
        try {
            PreparedStatement stmt = mConn.prepareStatement(
                    "update user set username = ?, password = ? where userid = ?");
            stmt.setString(1, aUser.getName().getName());
            stmt.setString(2, aUser.getPassword());
            stmt.setLong(3, aUser.getId().getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }
            stmt.close();
            return aUser;
        } catch (SQLException e) {
            StringBuilder builder = new StringBuilder("Could not update user: ");
            builder.append(aUser);
            String sqlState = e.getSQLState();
            if (sqlState != null && sqlState.equals("23505")) {
                builder.append("; already exists");
                throw new StoreException.AlreadyExists(builder.toString());
            }
            throw new StoreException(builder.toString());
        }
    }
    
    @Override
    // TODO ThierryH 2014-01-01 remove if not used anymore
    public UserInfo getUserById(UserId aUserId) throws StoreException {
        try {
            PreparedStatement stmt = mConn.prepareStatement("select userid, username, password from User where userid = ?");
            stmt.setLong(1, aUserId.getId());
            UserInfo user = null;
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UserName name = new UserName(rs.getString(2));
                String password = rs.getString(3);
                user = new UserInfo(aUserId, name, password);
            }
            stmt.close();
            return user;
        } catch (SQLException e) {
            throw new StoreException("Could not get user for id: " + aUserId, e);
        }
    }

    @Override
    public UserInfo getUserByName(UserName aUserName) throws StoreException {
        try {
            PreparedStatement stmt = mConn.prepareStatement("select userid, username, password from User where username = ?");
            UserInfo user = null;
            stmt.setString(1, aUserName.getName());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user = readUserFromResultSet(rs);
            }
            stmt.close();
            if (user != null) {
                List<ContactInfo> contacts = getContacts(user.getId());
                user.setContacts(contacts);
            }
            return user;
        } catch (SQLException e) {
            throw new StoreException("Could not get user for name: " + aUserName, e);
        }
    }

    @Override
    public void deleteUser(UserId aUserId) throws StoreException {
        try {
            PreparedStatement stmt = mConn.prepareStatement("delete from User where userid = ?");
            stmt.setLong(1, aUserId.getId());
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            throw new StoreException("Could not delete user for id: " + aUserId, e);
        }
    }

    private void insertContact(UserId aUserId, UserId aContactId, ContactState aState) throws StoreException {
        try {
            PreparedStatement stmt = mConn.prepareStatement("insert into contact values(?,?,?)");
            stmt.setLong(1, aUserId.getId());
            stmt.setLong(2, aContactId.getId());
            stmt.setByte(3, aState.id());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating contact failed, no rows affected.");
            }
            stmt.close();
        } catch (SQLException e) {
            StringBuilder builder = new StringBuilder("Could not insert contact ");
            builder.append(aContactId);
            builder.append(" for user ");
            builder.append(aUserId);
            String sqlState = e.getSQLState();
            if (sqlState != null && sqlState.equals("23505")) {
                builder.append("; already exists");
                throw new StoreException.AlreadyExists(builder.toString());
            }
            throw new StoreException(builder.toString(),e);
        }
    }

    @Override
    // TODO ThierryH 2014-01-01 make private if not used by another class
    public void updateContact(UserId aUserId, UserId aContactId, ContactState aState) throws StoreException {
        try {
            PreparedStatement stmt = mConn.prepareStatement(
                    "update contact set state = ? where userid = ? and contactid = ?");
            stmt.setByte(1, aState.id());
            stmt.setLong(2, aUserId.getId());
            stmt.setLong(3, aContactId.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating contact failed, no rows affected.");
            }
            stmt.close();
        } catch (SQLException e) {
            StringBuilder builder = new StringBuilder("Could not update contact ");
            builder.append(aContactId);
            builder.append(" for user ");
            builder.append(aUserId);
            String sqlState = e.getSQLState();
            if (sqlState != null && sqlState.equals("23505")) {
                builder.append("; already exists");
                throw new StoreException.AlreadyExists(builder.toString());
            }
            throw new StoreException(builder.toString(),e);
        }
    }

    @Override
    public List<ContactInfo> getContacts(UserId aUserId) throws StoreException {
        try {
            PreparedStatement stmt = mConn.prepareStatement(
                    "select u.userid, u.username, u.password, c.state from " +
                    "user u, contact c where c.contactid = u.userid and c.userid = ?");
            List<ContactInfo> list = new LinkedList<>();
            stmt.setLong(1, aUserId.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UserInfo user = readUserFromResultSet(rs);
                ContactState state = ContactState.forId(rs.getByte(4));
                list.add(new ContactInfo(user, state));
            }
            stmt.close();
            return list;
        } catch (SQLException e) {
            throw new StoreException("Could not get contacts for user: " + aUserId, e);
        }
    }

    
    @Override
    public List<ContactInfo> getContactOfUsers(UserId aUserId, ContactState aContactState) throws StoreException {
        try {
            PreparedStatement stmt = mConn.prepareStatement(
                    "select u.userid, u.username, u.PASSWORD, c.STATE from " +
                    "user u, contact c where c.userid = u.userid and c.contactid = ? and c.state = ?");
            List<ContactInfo> list = new LinkedList<>();
            stmt.setLong(1, aUserId.getId());
            stmt.setByte(2, aContactState.id());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UserInfo user = readUserFromResultSet(rs);
                ContactState state = ContactState.forId(rs.getByte(4));
                list.add(new ContactInfo(user, state));
            }
            stmt.close();
            return list;
        } catch (SQLException e) {
            throw new StoreException("Could not get contacts for user: " + aUserId, e);
        }
    }

    @Override
    public void deleteContact(UserId aUserId, UserId aContactId) throws StoreException {
        try {
            PreparedStatement stmt = mConn.prepareStatement("delete from Contact where userid = ? and contactid = ?");
            stmt.setLong(1, aUserId.getId());
            stmt.setLong(2, aContactId.getId());
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            StringBuilder builder = new StringBuilder("Could not delete contact ");
            builder.append(aContactId);
            builder.append(" for user ");
            builder.append(aUserId);
            throw new StoreException(builder.toString(), e);
        }
    }

    @Override
    public void insertMessage(UserId aSenderId, UserId aRecipientId, String aMessage) throws StoreException {
        try {
            PreparedStatement stmt = mConn.prepareStatement(
                    "insert into message (senderid, recipientid, message) values(?,?,?)");
            stmt.setLong(1, aSenderId.getId());
            stmt.setLong(2, aRecipientId.getId());
            stmt.setString(3, aMessage);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Saving message failed, no rows affected.");
            }
            stmt.close();
        } catch (SQLException e) {
            StringBuilder builder = new StringBuilder("Could not save message for sender ");
            builder.append(aSenderId);
            builder.append(" for recipient ");
            builder.append(aRecipientId);
            String sqlState = e.getSQLState();
            if (sqlState != null && sqlState.equals("23505")) {
                builder.append("; already exists");
                throw new StoreException.AlreadyExists(builder.toString());
            }
            throw new StoreException(builder.toString(),e);
        }
    }

    @Override
    public List<MessageInfo> getMessages(UserId aRecipientId) throws StoreException {
        try {
            PreparedStatement stmt = mConn.prepareStatement(
                "select u.username, m.message from message m, user u where recipientid = ? and m.senderid = u.userid");
            List<MessageInfo> list = new LinkedList<>();
            stmt.setLong(1, aRecipientId.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UserName senderName = new UserName(rs.getString(1));
                String msg = rs.getString(2);
                list.add(new MessageInfo(senderName, null, msg));
            }
            stmt.close();
            return list;
        } catch (SQLException e) {
            throw new StoreException("Could not get contacts for user: " + aRecipientId, e);
        }
    }
    
    @Override
    public void deleteMessagesForRecipient(UserId aRecipientId) throws StoreException {
        try {
            PreparedStatement stmt = mConn.prepareStatement("delete from message where recipientid = ?");
            stmt.setLong(1, aRecipientId.getId());
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            StringBuilder builder = new StringBuilder("Could not delete messages for recipientid ");
            builder.append(aRecipientId);
            throw new StoreException(builder.toString(), e);
        }
    }

    @Override
    public void destroy() throws StoreException  {
        try {
            mConn.close();
        } catch (SQLException e) {
            throw new StoreException("Could not destroy store", e);
        }
    }
    
    private Connection mConn;

    public JdbcUserStore(boolean aRecreateTable) throws StoreException {
        try {
            Class.forName("org.h2.Driver");
            mConn = DriverManager.getConnection("jdbc:h2:user");
            if (aRecreateTable) {
                dropTables();
                createTables();
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new StoreException("Could not create user store", e);
        }
    }

    private UserInfo readUserFromResultSet(ResultSet aRs) throws SQLException {
        UserId id = new UserId(aRs.getLong(1));
        UserName userName = new UserName(aRs.getString(2));
        String password = aRs.getString(3);
        return new UserInfo(id, userName, password);
    }

    
    /**
     * Main method to do some quick tests. Can also be used to recreate the tables to clear them.
     * @param aArgs arguments. Not used.
     * @throws Exception if anything goes wrong.
     */
    @SuppressWarnings("unused")
    public static void main(String[] aArgs) throws Exception {
        UserStore store = new JdbcUserStore(true);
        
        UserInfo user1 = new UserInfo(new UserId(1), new UserName("Bob"), "mypass");
        user1 = store.persistUser(user1);
        System.out.println("inserted userId: " + user1);
        
        UserInfo user2 = new UserInfo(new UserId(2), new UserName("Alice"), "mypass");
        user2 = store.persistUser(user2);
        System.out.println("inserted userId: " + user2);

        UserInfo user3 = new UserInfo(new UserId(3), new UserName("Charlie"), "mypass");
        user3 = store.persistUser(user3);
        System.out.println("inserted userId: " + user3);
        
        System.out.println("got user by name: " + store.getUserByName(new UserName("Bob")));
        System.out.println("got absent by name: " + store.getUserByName(new UserName("invalid")));

        if (false) {
            user1.setContacts(Arrays.asList(new ContactInfo(user2, ContactState.PENDING)));
            store.persistUser(user1);
            System.out.println("got user by name: " + store.getUserByName(new UserName("Bob")));
            
            user1.setContacts(Arrays.asList(new ContactInfo(user2, ContactState.CONTACT), 
                                            new ContactInfo(user3, ContactState.PENDING)));
            store.persistUser(user1);
            System.out.println("got user by name: " + store.getUserByName(new UserName("Bob")));
            
            user1.setContacts(Arrays.asList(new ContactInfo(user2, ContactState.CONTACT), 
                                            new ContactInfo(user3, ContactState.PENDING)));
            store.persistUser(user1);
            
            user1.setContacts(Arrays.asList(new ContactInfo(user2, ContactState.PENDING)));
            store.persistUser(user1);
            
            System.out.println("user 1 contacts: " + store.getContacts(new UserId(1)));
            System.out.println("user deleted");
        }
        
        store.destroy();
    }

}
