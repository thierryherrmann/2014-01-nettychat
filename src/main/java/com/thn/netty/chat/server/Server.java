package com.thn.netty.chat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.thn.netty.chat.codec.CommandCodec;

/**
 * Main chat server. Receive connection requests from clients and exchanges messages with them.
 * @author Thierry Herrmann
 */
public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
        
    private static final IdleConnectionHandler IDLENESS_HANDLER = new IdleConnectionHandler();

    private final UserManager mUserMgr = new UserManager();
    private ServerBootstrap mBootstrap;
    private final ExecutorService mScheduler = Executors.newCachedThreadPool();
    private NioEventLoopGroup mEventLoopGroup;
    private Runnable mShutdownAction;

    private Server() {
        mBootstrap = new ServerBootstrap();
        mBootstrap.childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(false));
        ChannelInitializer<Channel> initializer = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel aCh) throws Exception {
                aCh.pipeline().addLast(new ChannelListener());                                    // inbound
                aCh.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 1, 4, 0, 0)); // inbound
                aCh.pipeline().addLast(new CommandCodec());                                   // inbound / outbound
                aCh.pipeline().addLast(new ServerLogicHandler(mUserMgr, mScheduler, mShutdownAction));  // inbound
                aCh.pipeline().addLast(new IdleStateHandler(0, 0, 1, TimeUnit.HOURS));        // inbound / outbound
                aCh.pipeline().addLast(IDLENESS_HANDLER);
            }
        };
        mEventLoopGroup = new NioEventLoopGroup();
        setShutdownAction();
        mBootstrap.group(mEventLoopGroup).channel(NioServerSocketChannel.class)
                .childHandler(initializer);
    }
    
    private void start() {
        ChannelFuture future = mBootstrap.bind(new InetSocketAddress(8080));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture aChannelFuture) throws Exception {
                if (aChannelFuture.isSuccess()) {
                    System.out.println("Server bound");
                } else {
                    System.err.println("Bound attempt failed");
                    aChannelFuture.cause().printStackTrace();
                }
            }
        });
        try {
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void setShutdownAction() {
        mShutdownAction = new Runnable() {
            @Override
            public void run() {
                try {
                    mEventLoopGroup.shutdownGracefully().sync();
                    mScheduler.shutdownNow();
                    mScheduler.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    LOGGER.info("interrupted while waiting for tasks termination", e);
                }
                
            }
        };
        
    }

    /**
     * Main method to start the server. Listens on port 8080.
     * @param aArgs arguments. Not used.
     */
    public static void main(String[] aArgs) {
        Server server = new Server();
        server.start();
    }
}
