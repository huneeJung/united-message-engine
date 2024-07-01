package com.message.unitedmessageengine.core.socket.service;

import com.message.unitedmessageengine.core.socket.manager.ChannelManager.ChannelType;
import org.springframework.scheduling.annotation.Async;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;

public interface ChannelService {

    Queue<String> readPartialData(ByteBuffer buffer, byte[] payload);

    void authenticate(ChannelType line, SocketChannel channel);

    void writePing(SocketChannel channel);

    @Async
    void consumeSendResponse(SocketChannel reportChannel, Queue<String> ackBytes);

    @Async
    void consumeReportResponse(SocketChannel reportChannel, Queue<String> resultBytes);


}
