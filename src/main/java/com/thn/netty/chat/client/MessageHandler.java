package com.thn.netty.chat.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.thn.netty.chat.primitive.AddContactInviteCmd;
import com.thn.netty.chat.primitive.AddContactResponseCmd;
import com.thn.netty.chat.primitive.ChatMessageCmd;
import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.MessageInfo;

/**
 * Sends outbound commands to the server and handles inbound responses and
 * notifications sent back by the server to the client. A response is a command
 * with the same ID as a previously sent command. A notification is a command
 * with no known ID.
 * 
 * @author Thierry Herrmann
 */
public class MessageHandler extends SimpleChannelInboundHandler<Command> {
    private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());

    // no need for concurrent hashMap as netty guarantees a single thread is used for each connection
    private final Map<Integer,ResponseListener> mListeners = new HashMap<>();
    private Map<Integer,Future<?>> mTimeoutFutures = new ConcurrentHashMap<>();
    private ChannelHandlerContext mContext; // context to send outbound events in a thread-safe way
    
    /**
     * Sends a command.
     * @param aCmd command.
     * @param aListener response listener. May be null (for fire and forget commands).
     */
    public void sendCommand(final Command aCmd, final ResponseListener aListener) {
        // this method can be called from a thread other than the NIO thread. As it accesses structures also accessed
        // by the NIO thread, execute it in the NIO thread.
        mContext.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                if (aListener != null) {
                    mListeners.put(aCmd.getId(), aListener);
                    setupTimeout(aCmd, aListener);
                }
                ChannelFuture future = mContext.writeAndFlush(aCmd); // thread-safe. Can be called from any thread.
                future.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture aFuture) throws Exception {
                        LOGGER.info("cmd sent: " + aCmd);
                    }
                });
            }
        });
    }
    
    @Override
    public void channelRegistered(ChannelHandlerContext aCtx) throws Exception {
        mContext = aCtx; // store context to send outbound events in a thread-safe way
        super.channelRegistered(aCtx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext aCtx, Command aCmd) throws Exception {
        LOGGER.info("msg received: " + aCmd);

        // first process notifications
        if (aCmd instanceof AddContactInviteCmd) {
            processAddContactInviteNotif(aCtx, (AddContactInviteCmd) aCmd);
            return;
        } else if (aCmd instanceof AddContactResponseCmd) {
            processAddContactResponseNotif(aCtx, (AddContactResponseCmd) aCmd);
            return;
        } else if (aCmd instanceof ChatMessageCmd) {
            processChatMsgNotif(aCtx, (ChatMessageCmd) aCmd);
            return;
        }
        // TODO ThierryH 2014-01-02 process other notifs (NewMessage...)

        // otherwise either we have a response
        int respCmdId = aCmd.getId();
        ResponseListener listener = mListeners.remove(respCmdId);
        if (listener != null) { 
            cancelTimeout(respCmdId);
            listener.onResponse(aCmd);
            return;
        }
        // otherwise (not a notif), we have a late response. Ignore it.
    }

    private void setupTimeout(final Command aCmd, final ResponseListener aListener) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mListeners.remove(aCmd.getId());  // avoid leak for response that would never come
                aListener.onTimeout(aCmd);
            }
        };
        long timeoutMillis = aListener.getTimeoutMillis();
        // ensure the timeout listener is executed in the NIO thread since accesses structures also accessed by 
        // the NIO thread
        Future<?> future = mContext.channel().eventLoop().schedule(runnable, timeoutMillis, TimeUnit.MILLISECONDS);
        mTimeoutFutures.put(aCmd.getId(), future);
    }

    private void cancelTimeout(int respCmdId) {
        Future<?> future = mTimeoutFutures.remove(respCmdId);
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
    }

    private void processAddContactInviteNotif(ChannelHandlerContext aCtx, AddContactInviteCmd aCmd) {
        System.out.println("Received contact request from " + aCmd.getUserName());
    }
    
    private void processAddContactResponseNotif(ChannelHandlerContext aCtx, AddContactResponseCmd aCmd) {
        System.out.println("Received contact response from " + aCmd.getContactName() +". Invitation accepted: " + 
                           aCmd.isAccepted());
    }
    
    private void processChatMsgNotif(ChannelHandlerContext aCtx, ChatMessageCmd aCmd) {
        MessageInfo msgInfo = aCmd.getMessageInfo();
        System.out.println("Received message from " + msgInfo.getSender().getName() +": " + msgInfo.getMessage());
    }
    
    @Override
    public void handlerAdded(ChannelHandlerContext aCtx) throws Exception {
        super.handlerAdded(aCtx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext aCtx) throws Exception {
        LOGGER.info("Connection lost with server");
        aCtx.channel().close();
        super.channelInactive(aCtx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext aCtx, Throwable aCause) throws Exception {
        // TODO ThierryH 2013-12-25 adjust this ?
        if (aCause instanceof IOException) {
            // channel was probably closed. Do nothing here. Will be handled by channelInactive.
            LOGGER.error("IO exception", aCause);
        }
        else {
            LOGGER.error(aCause);
        }
    }
}
