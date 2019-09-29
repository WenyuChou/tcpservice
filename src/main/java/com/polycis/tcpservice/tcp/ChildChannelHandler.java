package com.polycis.tcpservice.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author : Wenyu Zhou
 * @version : v1.0
 * @date : 2019/7/29
 * description : 描述
 */
@Slf4j
@Component
public class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

    @Resource
    private DiscardServerHandler discardServerHandler;

    @Override
    public void initChannel(SocketChannel socketChannel) throws Exception {
        String ip = socketChannel.remoteAddress().getAddress().getHostAddress();
        log.info("信息：有一TCP客户端链接到本服务端 Name:" + socketChannel.localAddress().getHostName() + " IP:" +
                ip + " Port:" + socketChannel.localAddress().getPort());
        socketChannel.pipeline().addLast(discardServerHandler);
    }
}
