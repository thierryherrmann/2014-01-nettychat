package com.thn.netty.chat.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.thn.netty.chat.client.CommandReader.BadCommandException;
import com.thn.netty.chat.codec.CommandCodec;
import com.thn.netty.chat.primitive.AddContactInviteCmd;
import com.thn.netty.chat.primitive.AddContactResponseCmd;
import com.thn.netty.chat.primitive.ChatMessageCmd;
import com.thn.netty.chat.primitive.Command;
import com.thn.netty.chat.primitive.CreateAccountRequest;
import com.thn.netty.chat.primitive.ExitRequest;
import com.thn.netty.chat.primitive.GetContactOfUsers;
import com.thn.netty.chat.primitive.GetContactOfUsersResponse;
import com.thn.netty.chat.primitive.GetPendingMessages;
import com.thn.netty.chat.primitive.GetPendingMessagesResponse;
import com.thn.netty.chat.primitive.LoginRequest;
import com.thn.netty.chat.primitive.LogoutRequest;
import com.thn.netty.chat.primitive.RemoveContactCmd;
import com.thn.netty.chat.primitive.ShutdownServerRequest;
import com.thn.netty.chat.util.DefaultIdGenerator;

/**
 * Main chat client.
 * @author Thierry
 */
public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private final ExecutorService mScheduler = Executors.newCachedThreadPool();
    private final CommandReader mCmdReader;
    
    private MessageHandler mMessageHandler = new MessageHandler();
    private Bootstrap mBootstrap = new Bootstrap();
    private NioEventLoopGroup mEventLoopGroup;
    private Channel mChannel;  
    
    Client(CommandReader aCmdReader) {
        ChannelInitializer<Channel> initializer = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel aCh) throws Exception {
                aCh.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 1, 4, 0, 0)); // inbound
                aCh.pipeline().addLast(new CommandCodec());                 // inbound / outbound
                aCh.pipeline().addLast(mMessageHandler);            // inbound
            }
        };
        mEventLoopGroup = new NioEventLoopGroup();
        mBootstrap.group(mEventLoopGroup).channel(NioSocketChannel.class).handler(initializer);
        
        mCmdReader = aCmdReader;
    }
    
    void start() {
        ChannelFuture future = mBootstrap.connect(new InetSocketAddress("127.0.0.1", 8080));
        mChannel = future.channel();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture aChannelFuture) throws Exception {
                if (aChannelFuture.isSuccess()) {
                    LOGGER.info("Connection established");
                    mainLoop();
                } else {
                    LOGGER.warn("Connection attempt failed");
                    aChannelFuture.cause().printStackTrace();
                }
            }
        });
        
        try {
            mChannel.closeFuture().sync();
            LOGGER.info("Connection closed. Client shutting down.");
            shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("Client shutdown complete.");
    }
    
    /**
     * Main method. Uses a console command reader.
     * @param aArgs arguments. Not used.
     */
    public static void main(String[] aArgs) {
        Client client = new Client(new ConsoleReader(DefaultIdGenerator.getInstance()));
        client.start();
    }
    
    private void shutdown() {
        try {
            mEventLoopGroup.shutdownGracefully().sync();
            mScheduler.shutdownNow();
            mScheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.info("interrupted while waiting for timeout tasks termination", e);
        }
    }

    private void mainLoop() {
        // getting the next command might block: execute in separate thread to NOT block the netty NIO worker thread
        mScheduler.execute(new Runnable() {
            @Override
            public void run() {
                boolean getCmdFailed = false;
                do {
                    try {
                        Command cmd = mCmdReader.getCommand();
                        if (cmd == null) {
                            return; // exit loop. Should happen in test mode only.
                        }
                        if (cmd instanceof LoginRequest) {
                            sendCommand(cmd, mDefaultListener);
                        } else if (cmd instanceof LogoutRequest) {
                            sendCommand(cmd, mDefaultListener);
                        } else if (cmd instanceof ExitRequest) {
                            sendCommand(cmd, null); // no need for listener
                        } else if (cmd instanceof CreateAccountRequest) {
                            sendCommand(cmd, mDefaultListener);
                        } else if (cmd instanceof AddContactInviteCmd) {
                            sendCommand(cmd, mDefaultListener);
                        } else if (cmd instanceof AddContactResponseCmd) {
                            sendCommand(cmd, mDefaultListener);
                        } else if (cmd instanceof RemoveContactCmd) {
                            sendCommand(cmd, mDefaultListener);
                        } else if (cmd instanceof GetContactOfUsers) {
                            sendCommand(cmd, new PendingInvitesListener());
                        } else if (cmd instanceof ChatMessageCmd) {
                            sendCommand(cmd, mDefaultListener);
                        } else if (cmd instanceof GetPendingMessages) {
                            sendCommand(cmd, new PendingMsgListener());
                        } else if (cmd instanceof ShutdownServerRequest) {
                            sendCommand(cmd, mDefaultListener);
                        } else {
                            getCmdFailed = true;
                            LOGGER.error("unknown command: " + cmd); // programming error. Stop here.
                            System.exit(-1);
                        }
                    } catch (BadCommandException e) {
                        getCmdFailed = true; // user error. Retry.
                        LOGGER.error(e.getMessage());
                    }
                } while(getCmdFailed);
            }
        });
    }
    
    private class DefaultListener implements ResponseListener {
        @Override
        public void onResponse(Command aResponse) {
            mainLoop();
        }

        @Override
        public long getTimeoutMillis() {
            return 1000*1000L; // TODO ThierryH 2014-01-02 reduce to 30
        }

        @Override
        public void onTimeout(Command aRequest) {
            LOGGER.info("timeout for cmd: " + aRequest);
            mainLoop();
        }
    }
    
    private class PendingInvitesListener extends DefaultListener {
        @Override
        public void onResponse(Command aResponse) {
            GetContactOfUsersResponse response = (GetContactOfUsersResponse) aResponse;
            System.out.println("These users ask permission to add you in their contact list: " + 
                               response.getRequesterNames());
            mainLoop();
        }
    }
    private class PendingMsgListener extends DefaultListener {
        @Override
        public void onResponse(Command aResponse) {
            GetPendingMessagesResponse response = (GetPendingMessagesResponse) aResponse;
            System.out.println("New messages: " + response.getMessages());
            mainLoop();
        }
    }
    private DefaultListener mDefaultListener = new DefaultListener();
    
    private void sendCommand(Command aCommand, ResponseListener aListener) {
        mMessageHandler.sendCommand(aCommand, aListener);
    }
}
