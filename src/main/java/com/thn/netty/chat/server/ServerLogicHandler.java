package com.thn.netty.chat.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import com.thn.netty.chat.primitive.AddContactInviteCmd;
import com.thn.netty.chat.primitive.AddContactResponseCmd;
import com.thn.netty.chat.primitive.ChatMessageCmd;
import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.CreateAccountRequest;
import com.thn.netty.chat.primitive.ErrorResponse;
import com.thn.netty.chat.primitive.ErrorResponse.Code;
import com.thn.netty.chat.primitive.ExitRequest;
import com.thn.netty.chat.primitive.GetContactOfUsers;
import com.thn.netty.chat.primitive.GetContactOfUsersResponse;
import com.thn.netty.chat.primitive.GetPendingMessages;
import com.thn.netty.chat.primitive.GetPendingMessagesResponse;
import com.thn.netty.chat.primitive.LoginRequest;
import com.thn.netty.chat.primitive.LogoutRequest;
import com.thn.netty.chat.primitive.MessageInfo;
import com.thn.netty.chat.primitive.OkResponse;
import com.thn.netty.chat.primitive.RemoveContactCmd;
import com.thn.netty.chat.primitive.ShutdownServerRequest;
import com.thn.netty.chat.primitive.UserId;
import com.thn.netty.chat.primitive.UserName;
import com.thn.netty.chat.server.store.StoreException;
import com.thn.netty.chat.user.ContactInfo;
import com.thn.netty.chat.user.ContactState;
import com.thn.netty.chat.user.UserInfo;
import com.thn.netty.chat.util.DefaultIdGenerator;
import com.thn.netty.chat.util.IdGenerator;

/**
 * Processes all client requests and responds to them. This may involve access to persistent data which is
 * done in a thread safe way.
 * @author Thierry Herrmann
 */
public class ServerLogicHandler extends SimpleChannelInboundHandler<Command> {
    private static final Logger LOGGER = Logger.getLogger(ServerLogicHandler.class.getName());
    private final UserManager mUserMgr;
    private IdGenerator mNotifIdGen = DefaultIdGenerator.getInstance();
    private final ExecutorService mScheduler;
    private final Runnable mShutdownAction;

    /**
     * Constructor.
     * @param aUserMgr user manager to access persistent data.
     * @param aScheduler scheduler to execute blocking requests (e.g. JDBC) outside of NIO worker threads.
     * @param aShutdownAction action to execute to shutdown the server.
     */
    public ServerLogicHandler(UserManager aUserMgr, ExecutorService aScheduler, Runnable aShutdownAction) {
        mUserMgr = aUserMgr;
        mScheduler = aScheduler;
        mShutdownAction = aShutdownAction;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext aCtx) throws Exception {
        aCtx.flush();
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext aCtx) throws Exception {
        super.channelInactive(aCtx);
        ChannelInfo channelInfo = aCtx.channel().attr(ChannelListener.CHANNEL_INFO).get();
        StringBuilder builder = new StringBuilder();
        UserInfo userInfo = channelInfo.getUserInfo();
        if (userInfo != null) {
            builder.append(" (username: ").append(userInfo.getName().getName()).append(')');
            mUserMgr.userLoggedOut(userInfo.getName());
        }
        LOGGER.info("Connection lost with client" + builder.toString());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext aCtx, Command aMsg) throws Exception {
        LOGGER.info("cmd received: " + aMsg);
        int cmdId = aMsg.getId();
        ChannelInfo channelInfo = aCtx.channel().attr(ChannelListener.CHANNEL_INFO).get();
        if (aMsg instanceof CreateAccountRequest) {
            processCreateAccount(aCtx, aMsg);
        }
        else if (aMsg instanceof LoginRequest) {
            processLogin(aCtx, aMsg, channelInfo);
        }
        else if (aMsg instanceof ExitRequest) {
            processExit(aCtx, channelInfo);
        }
        else if (!mUserMgr.isUserLoggedIn(channelInfo)) { // the next commands require the user to be logged in
            aCtx.writeAndFlush(new ErrorResponse(cmdId, Code.NOT_LOGGED_IN));
        }
        else if (aMsg instanceof LogoutRequest) {
            processLogout(aCtx, aMsg, channelInfo);
        }
        else if (aMsg instanceof AddContactInviteCmd) {
            processAddContact(aCtx, aMsg, channelInfo);
        }
        else if (aMsg instanceof AddContactResponseCmd) {
            processAddContactResponse(aCtx, aMsg, channelInfo);
        }
        else if (aMsg instanceof RemoveContactCmd) {
            processRemoveContact(aCtx, aMsg, channelInfo);
        }
        else if (aMsg instanceof GetContactOfUsers) {
            processGetContactInvites(aCtx, aMsg, channelInfo);
        }
        else if (aMsg instanceof ChatMessageCmd) {
            processChatMessage(aCtx, aMsg, channelInfo);
        }
        else if (aMsg instanceof GetPendingMessages) {
            processGetMessages(aCtx, aMsg, channelInfo);
        }
        else if (aMsg instanceof ShutdownServerRequest) {
            new Thread(mShutdownAction).start(); // start shutdown in other thread to no deadlock
        }
        else {
            aCtx.writeAndFlush(new ErrorResponse(cmdId, Code.BAD_REQUEST, "unknown comand: " + aMsg));
        }
    }

    private void processLogout(ChannelHandlerContext aCtx, Command aMsg, ChannelInfo aChannelInfo) {
        int cmdId = aMsg.getId();
        UserInfo userInfo = aChannelInfo.getUserInfo(); // not null since user logged in (verified earlier)
        mUserMgr.userLoggedOut(userInfo.getName());
        aChannelInfo.setUserInfo(null);
        LOGGER.info("user logged out (still connected): " + userInfo.getName().getName());
        aCtx.writeAndFlush(new OkResponse(cmdId));
    }

    private void processExit(ChannelHandlerContext aCtx, ChannelInfo aChannelInfo) {
        UserInfo userInfo = aChannelInfo.getUserInfo();
        if (userInfo != null) {
            // user still logged in: log it out
            mUserMgr.userLoggedOut(userInfo.getName());
            aChannelInfo.setUserInfo(null);
            LOGGER.info("user logged out (still connected): " + userInfo.getName().getName());
        }
        // close connection
        aCtx.channel().close();
    }
    
    private void processLogin(final ChannelHandlerContext aCtx, final Command aMsg, final ChannelInfo aChannelInfo)
    {
        final int cmdId = aMsg.getId();
        if (mUserMgr.isUserLoggedIn(aChannelInfo)) {
            LOGGER.info("user already logged in: " + aChannelInfo.getUserInfo());
            aCtx.writeAndFlush(new OkResponse(cmdId));
            return;
        }
        // the next steps do blocking calls (e.g. access User DB): execute them in other thread to not block 
        // the NIO thread and use ChannelHandlerContext to send outbound messages in a thread-safe way
        mScheduler.execute(new Runnable() {
            @Override
            public void run() {
                LoginRequest loginCmd = (LoginRequest) aMsg;
                UserName userName = loginCmd.getUserName();
                
                UserInfo userInfo = mUserMgr.getUserByName(userName);
                if (userInfo == null) {
                    aCtx.writeAndFlush(new ErrorResponse(cmdId, Code.INVALID_USER_OR_PASS));
                    return;
                }
                if (!userInfo.getPassword().equals(loginCmd.getPassword())) {
                    aCtx.writeAndFlush(new ErrorResponse(cmdId, Code.INVALID_USER_OR_PASS));
                    return;
                }
                mUserMgr.userLoggedIn(userInfo, aChannelInfo);
                aCtx.writeAndFlush(new OkResponse(cmdId));
            }
        });
    }

    private void processCreateAccount(final ChannelHandlerContext aCtx, final Command aMsg)
    {
        // this method does blocking calls (e.g. access User DB): execute them in other thread to not block 
        // the NIO thread and use ChannelHandlerContext to send outbound messages in a thread-safe way
        mScheduler.execute(new Runnable() {
            @Override
            public void run() {
                CreateAccountRequest cmd = (CreateAccountRequest) aMsg;
                int cmdId = aMsg.getId();
                try
                {
                    mUserMgr.createUser(cmd.getUserName(), cmd.getPassword());
                    LOGGER.info("user account created: " + cmd);
                }
                catch (StoreException.AlreadyExists e)
                {
                    aCtx.writeAndFlush(new ErrorResponse(cmdId, Code.USER_ALREADY_EXISTS));
                    LOGGER.info("user already exists");
                    return;
                }
                catch (StoreException e)
                {
                    aCtx.writeAndFlush(new ErrorResponse(cmdId, Code.INTERNAL_ERROR, e.getMessage()));
                    LOGGER.error("internal error: " + e.getMessage());
                    return;
                }
                aCtx.writeAndFlush(new OkResponse(cmdId));
            }
        });
    }
    
    private void processAddContact(final ChannelHandlerContext aCtx, final Command aMsg, final ChannelInfo aChannelInfo)
    {
        // this method does blocking calls (e.g. access User DB): execute them in other thread to not block 
        // the NIO thread and use ChannelHandlerContext to send outbound messages in a thread-safe way
        mScheduler.execute(new Runnable() {
            @Override
            public void run() {
                int cmdId = aMsg.getId();
                AddContactInviteCmd cmd = (AddContactInviteCmd) aMsg;
                UserName contactName = cmd.getContactName();
                if (contactName == null) {
                    aCtx.writeAndFlush(new ErrorResponse(cmdId, 
                            Code.BAD_REQUEST, "add contact request must have a non null contact name"));
                    return;
                }
                // verify the contact exists
                UserInfo contact = mUserMgr.getUserByName(contactName);
                if (contact == null) {
                    // contact doesn't exist
                    aCtx.writeAndFlush(new ErrorResponse(cmdId, Code.BAD_REQUEST, "contact does not exist: " + 
                            contactName));
                    return;
                }
                // see if the contact to add is not already a contact or a pending contact
                UserInfo requesterUser = aChannelInfo.getUserInfo();
                if (getContact(requesterUser.getContacts(), contactName) != null) {
                    aCtx.writeAndFlush(new OkResponse(cmdId)); // already a contact
                    return;
                }
                
                // persist the invitation
                requesterUser.getContacts().add(new ContactInfo(contact, ContactState.PENDING));
                try {
                    mUserMgr.persistUser(requesterUser);
                } catch (StoreException e) {
                    aCtx.writeAndFlush(new ErrorResponse(cmdId, Code.INTERNAL_ERROR, e.getMessage()));
                    LOGGER.error("internal error: " + e);
                    return;
                }
                
                ChannelInfo contactChannelInfo = mUserMgr.getLoggedInUserChannel(contactName);
                if (contactChannelInfo != null) {
                    // contact existing and logged in: send the addContact as a notification
                    AddContactInviteCmd notif = new AddContactInviteCmd(mNotifIdGen.nextId(), 
                                                                        requesterUser.getName(), null);
                    ChannelHandlerContext contactContext = contactChannelInfo.getContext();
                    sendNotif(notif, contactContext);
                }
                
                aChannelInfo.getContext().writeAndFlush(new OkResponse(cmdId)); // confirm the message was processed
            }
        });
    }

    private void processRemoveContact(final ChannelHandlerContext aCtx, final Command aMsg, 
                                      final ChannelInfo aChannelInfo)
    {
        // this method does blocking calls (e.g. access User DB): execute them in other thread to not block 
        // the NIO thread and use ChannelHandlerContext to send outbound messages in a thread-safe way
        mScheduler.execute(new Runnable() {
            @Override
            public void run() {
                int cmdId = aMsg.getId();
                RemoveContactCmd cmd = (RemoveContactCmd) aMsg;
                UserName contactName = cmd.getContactName();
                if (contactName == null) {
                    aCtx.writeAndFlush(new ErrorResponse(cmdId, 
                            Code.BAD_REQUEST, "remove contact request must have a non null contact name"));
                    return;
                }
                // remove the contact
                UserInfo requesterUser = aChannelInfo.getUserInfo();
                List<ContactInfo> existingContacts = requesterUser.getContacts();
                List<ContactInfo> newContacts = removeContact(existingContacts, contactName);
                if (existingContacts.size() == newContacts.size()) {
                    // already not a contact
                    aCtx.writeAndFlush(new OkResponse(cmdId)); // contact already removed
                    return;
                }
                // persist the new contacts
                requesterUser.setContacts(newContacts);
                try {
                    mUserMgr.persistUser(requesterUser);
                } catch (StoreException e) {
                    aCtx.writeAndFlush(new ErrorResponse(cmdId, Code.INTERNAL_ERROR, e.getMessage()));
                    LOGGER.error("internal error: " + e);
                    return;
                }
                // everything went well
                aCtx.writeAndFlush(new OkResponse(cmdId)); // contact already removed
            }
        });
    }
    
    private void processGetContactInvites(final ChannelHandlerContext aCtx, final Command aMsg, 
            final ChannelInfo aChannelInfo)
    {
        // this method does blocking calls (e.g. access User DB): execute them in other thread to not block 
        // the NIO thread and use ChannelHandlerContext to send outbound messages in a thread-safe way
        mScheduler.execute(new Runnable() {
            @Override
            public void run() {
                int cmdId = aMsg.getId();
                UserId contactId = aChannelInfo.getUserInfo().getId();
                GetContactOfUsers cmd = (GetContactOfUsers) aMsg;
                List<ContactInfo> contactOfUsers;
                try {
                    contactOfUsers = mUserMgr.getContactOfUsers(contactId, cmd.getContactState());
                } catch (StoreException e) {
                    aCtx.writeAndFlush(new ErrorResponse(cmdId, Code.INTERNAL_ERROR, e.getMessage()));
                    LOGGER.error("internal error: " + e);
                    return;
                }
                LinkedList<UserName> pendingInvites = new LinkedList<>();
                for (ContactInfo contactInfo : contactOfUsers) {
                    if (contactInfo.getState() == ContactState.PENDING) {
                        pendingInvites.add(contactInfo.getContact().getName());
                    }
                }
                // everything went well
                aCtx.writeAndFlush(new GetContactOfUsersResponse(cmdId, pendingInvites));
            }
        });
    }

    private void processChatMessage(final ChannelHandlerContext aCtx, final Command aMsg, 
            final ChannelInfo aChannelInfo)
    {
        // this method does blocking calls (e.g. access User DB): execute them in other thread to not block 
        // the NIO thread and use ChannelHandlerContext to send outbound messages in a thread-safe way
        mScheduler.execute(new Runnable() {
            @Override
            public void run() {
                int cmdId = aMsg.getId();
                ChatMessageCmd cmd = (ChatMessageCmd) aMsg;
                MessageInfo msgInfo = cmd.getMessageInfo();
                UserName recipientName = msgInfo.getRecipient();
                if (recipientName == null) {
                    aCtx.writeAndFlush(new ErrorResponse(cmdId, 
                            Code.BAD_REQUEST, "chat message must have a non null recipient name"));
                    return;
                }
                // verify the recipient exists
                UserInfo recipient = mUserMgr.getUserByName(recipientName);
                if (recipient == null) {
                    // contact doesn't exist
                    aCtx.writeAndFlush(new ErrorResponse(cmdId, Code.BAD_REQUEST, "recipient does not exist: " + 
                            recipientName));
                    return;
                }
                UserInfo requesterUser = aChannelInfo.getUserInfo();
                ChannelInfo recipientChannelInfo = mUserMgr.getLoggedInUserChannel(recipientName);
                if (recipientChannelInfo != null) {
                    // recipient existing and logged in: send the message as a notification
                    ChatMessageCmd notif = new ChatMessageCmd(mNotifIdGen.nextId(), 
                                           new MessageInfo(requesterUser.getName(), null,msgInfo.getMessage()));
                    ChannelHandlerContext recipientContext = recipientChannelInfo.getContext();
                    sendNotif(notif, recipientContext);
                } else {
                    // persist the message (offline message) to later deliver it when the recipient logs in
                    try {
                        mUserMgr.insertMessage(requesterUser.getId(), recipient.getId(), msgInfo.getMessage());
                    } catch (StoreException e) {
                        aCtx.writeAndFlush(new ErrorResponse(cmdId, Code.INTERNAL_ERROR, e.getMessage()));
                        LOGGER.error("internal error: " + e);
                        return;
                    }
                }
                aCtx.writeAndFlush(new OkResponse(cmdId)); // respond to sender that the msg has been processed
            }
        });
    }

    private void processGetMessages(final ChannelHandlerContext aCtx, final Command aMsg, 
                                    final ChannelInfo aChannelInfo)
    {
        // this method does blocking calls (e.g. access User DB): execute them in other thread to not block 
        // the NIO thread and use ChannelHandlerContext to send outbound messages in a thread-safe way
        mScheduler.execute(new Runnable() {
            @Override
            public void run() {
                int cmdId = aMsg.getId();
                UserId recipientId = aChannelInfo.getUserInfo().getId();
                List<MessageInfo> pendingMsgs;
                try {
                    pendingMsgs = mUserMgr.getMessages(recipientId);
                    mUserMgr.deleteMessagesForRecipient(recipientId);
                } catch (StoreException e) {
                    aCtx.writeAndFlush(new ErrorResponse(cmdId, Code.INTERNAL_ERROR, e.getMessage()));
                    LOGGER.error("internal error: " + e);
                    return;
                }
                // everything went well
                aCtx.writeAndFlush(new GetPendingMessagesResponse(cmdId, pendingMsgs));
            }
        });
    }

    private static ContactInfo getContact(List<ContactInfo> aContacts, UserName aContactName) {
        for(ContactInfo contact : aContacts) {
            if (contact.getContact().getName().equals(aContactName)) {
                return contact;
            }
        }
        return null;
    }
    
    private static List<ContactInfo> removeContact(List<ContactInfo> aContacts, UserName aContactName) {
        List<ContactInfo> newList = new LinkedList<>();
        for(ContactInfo contact : aContacts) {
            if (!contact.getContact().getName().equals(aContactName)) {
                newList.add(contact);
            }
        }
        return newList;
    }
    
    private void processAddContactResponse(final ChannelHandlerContext aCtx, final Command aMsg, 
                                           final ChannelInfo aChannelInfo)
    {
        // this method does blocking calls (e.g. access User DB): execute them in other thread to not block 
        // the NIO thread and use ChannelHandlerContext to send outbound messages in a thread-safe way
        mScheduler.execute(new Runnable() {
            @Override
            public void run() {
                int cmdId = aMsg.getId();
                AddContactResponseCmd cmd = (AddContactResponseCmd) aMsg;
                UserName requesterUserName = cmd.getUserName();
                if (requesterUserName == null) {
                    aCtx.channel().writeAndFlush(new ErrorResponse(cmdId, Code.BAD_REQUEST, 
                            "Invite response must have a non null requester user name: " + aMsg));
                    return;
                }
                
                // verify that the add contact requester exists
                UserInfo requesterUser = mUserMgr.getUserByName(requesterUserName);
                if (requesterUser == null) {
                    aCtx.channel().writeAndFlush(new ErrorResponse(cmdId, Code.BAD_REQUEST, 
                            "Invite response requester does not exist: " + aMsg));
                    return;
                }
                
                // verify that there's a pending addContact request
                UserInfo contact = aChannelInfo.getUserInfo();
                ContactInfo pendingContact = getContact(requesterUser.getContacts(), contact.getName());
                if (pendingContact == null || pendingContact.getState() != ContactState.PENDING) {
                    aCtx.channel().writeAndFlush(new ErrorResponse(cmdId, Code.BAD_REQUEST, 
                            "No pending add contact request for this requester: " + aMsg));
                    return;
                }
                
                List<ContactInfo> newContacts = removeContact(requesterUser.getContacts(), contact.getName());
                if (cmd.isAccepted()) {
                    newContacts.add(new ContactInfo(pendingContact.getContact(), ContactState.CONTACT));
                    // make sure the requester is also added to the contact's contacts
                    List<ContactInfo> contactNewContacts = removeContact(contact.getContacts(), requesterUserName);
                    contactNewContacts.add(new ContactInfo(requesterUser, ContactState.CONTACT));
                    contact.setContacts(contactNewContacts);
                }
                
                // persist any change
                try {
                    requesterUser.setContacts(newContacts);
                    mUserMgr.persistUser(requesterUser);
                    mUserMgr.persistUser(contact);
                } catch (StoreException e) {
                    aCtx.writeAndFlush(new ErrorResponse(cmdId, Code.INTERNAL_ERROR, e.getMessage()));
                    LOGGER.error("internal error: " + e);
                    return;
                }
                
                ChannelInfo requesterChannelInfo = mUserMgr.getLoggedInUserChannel(requesterUserName);
                if (requesterChannelInfo != null) {
                    // requester user existing and logged in. Forward response to the requester
                    AddContactResponseCmd responseCmd = new AddContactResponseCmd(cmdId, null, 
                                                                                contact.getName(), cmd.isAccepted());
                    ChannelHandlerContext requesterContext = requesterChannelInfo.getContext();
                    requesterContext.writeAndFlush(responseCmd);
                }
                // respond to caller
                aCtx.writeAndFlush(new OkResponse(cmdId));
            }
        });
    }

    private void sendNotif(final Command aCommand, ChannelHandlerContext aTargetContext) {
        ChannelFuture future = aTargetContext.writeAndFlush(aCommand);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture aFuture) throws Exception {
                LOGGER.info("Sent notif: " + aCommand);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext aCtx, Throwable aCause) throws Exception {
        if (aCause instanceof IOException) {
            // channel was probably closed. Do nothing here. Will be handled by channelInactive.
            ChannelInfo channelInfo = aCtx.channel().attr(ChannelListener.CHANNEL_INFO).get();
            StringBuilder builder = new StringBuilder();
            UserInfo userInfo = channelInfo.getUserInfo();
            if (userInfo != null) {
                builder.append(" (username: ").append(userInfo.getName().getName()).append(')');
            }
            LOGGER.info(aCause.getMessage() + ": client probably disconnected" + builder.toString());
        }
        else {
            LOGGER.error(aCause);
        }
    }
}
