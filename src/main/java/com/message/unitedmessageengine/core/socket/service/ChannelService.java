package com.message.unitedmessageengine.core.socket.service;

import com.message.unitedmessageengine.core.socket.manager.AbstractChannelManager.ChannelType;
import org.springframework.scheduling.annotation.Async;

import java.nio.channels.SocketChannel;
import java.util.Queue;

public interface ChannelService {

    void authenticate(ChannelType line, SocketChannel channel);

    void writePing(SocketChannel channel);

    @Async
    void processSendResponse(SocketChannel reportChannel, Queue<String> ackBytes);

    @Async
    void processReportResponse(SocketChannel reportChannel, Queue<String> resultBytes);

}
