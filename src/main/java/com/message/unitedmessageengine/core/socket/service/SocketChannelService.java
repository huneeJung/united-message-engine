package com.message.unitedmessageengine.core.socket.service;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface SocketChannelService {

    void authenticate(String line, SocketChannel channel) throws IOException;

    void sendPing(SocketChannel channel) throws IOException;

    void receivePong(SocketChannel receiverChannel) throws IOException;

    void processAck(SocketChannel channel);

    void processResult(SocketChannel channel);

}
