package com.message.unitedmessageengine.core.socket.service;

import java.nio.channels.SocketChannel;

public interface SocketChannelService {

    void processAck(SocketChannel channel);

    void processResult(SocketChannel channel);

}
