package com.nice.core.netty.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public interface Session {

    void setCtx(ChannelHandlerContext ctx);
    ChannelHandlerContext getCtx();
    Channel getChannel();

    boolean channelIsActive();

    boolean channelIsOpen() ;

    boolean channelIsWritable();

    void write(Object msg);

    void writeAndFlush(Object msg);

    void write(Object msg, boolean flush);

    void close();

    String getRemoteAddress();

    String getLocalAddress() ;

    Object getData();

    void setData(Object data);

    void resetNotHeartBreatNums();

    //����1���������������δ�յ������Ĵ������򷵻�true
    boolean incrNotHeartBreatNums();
}
