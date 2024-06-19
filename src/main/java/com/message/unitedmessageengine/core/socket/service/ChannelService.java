package com.message.unitedmessageengine.core.socket.service;

import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Queue;

public interface ChannelService {

    void authenticate(String line, SocketChannel channel);

    void sendPing(SocketChannel channel) throws IOException;

    @Async
    void processSendResponse(Queue<String> ackBytes) throws IOException;

    @Async
    void processReportResponse(SocketChannel reportChannel, Queue<String> resultBytes) throws IOException;

}
