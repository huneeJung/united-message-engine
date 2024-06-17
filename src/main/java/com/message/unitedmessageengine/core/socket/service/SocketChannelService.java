package com.message.unitedmessageengine.core.socket.service;

import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface SocketChannelService {

    void authenticate(String line, SocketChannel channel) throws IOException;

    void sendPing(SocketChannel channel) throws IOException;

    @Async
    void processAck(byte[] ackBytes) throws IOException;

    @Async
    void processResult(byte[] resultBytes) throws IOException;

}
