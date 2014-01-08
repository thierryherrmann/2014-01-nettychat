package com.thn.netty.chat.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.apache.log4j.Logger;

/**
 * Used to close idle connections.
 * @author Thierry Herrmann
 */
@Sharable
public final class IdleConnectionHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(IdleConnectionHandler.class.getName());
        
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        ChannelFuture future = ctx.close();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture aFuture) throws Exception {
                LOGGER.info("Closing connection due to idleness");
                
            }
        });
    }
}