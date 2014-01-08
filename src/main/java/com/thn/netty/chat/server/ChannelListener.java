package com.thn.netty.chat.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.AttributeKey;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Gets and keeps the reference to {@link ChannelInfo} when the connection is established.
 * @author Thierry Herrmann
 */
public class ChannelListener extends ChannelInboundHandlerAdapter {
        
    public static final AttributeKey<ChannelInfo> CHANNEL_INFO = AttributeKey.valueOf("channelInfo");
    @Override
    public void channelActive(ChannelHandlerContext aCtx) throws Exception {
        // save the context of the last handler
        ChannelPipeline pipeline = aCtx.pipeline();
        ChannelHandlerContext context = null;
        for(Iterator<Entry<String, ChannelHandler>>  iter = pipeline.iterator();iter.hasNext();) {
            Entry<String, ChannelHandler> entry = iter.next();
            context = pipeline.context(entry.getValue());
        }
        aCtx.channel().attr(CHANNEL_INFO).set(new ChannelInfo(context));
        super.channelActive(aCtx);
    }
}
