package com.message.unitedmessageengine.core.socket.manager;

import com.message.unitedmessageengine.core.socket.service.SocketChannelServiceA;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
@Component
public class SocketChannelManagerA extends AbstractSocketChannelManager<SocketChannelServiceA> {

    private final ByteBuffer readBuffer = ByteBuffer.allocate(10 * 1024);

    @Autowired
    public SocketChannelManagerA(SocketChannelServiceA socketChannelService) {
        super(socketChannelService);
    }

    @PostConstruct
    public void init() throws IOException {
        host = System.getProperty("agentA.host");
        port = Integer.parseInt(System.getProperty("agentA.port"));
        isAliveSelector = true;
        tcpConnect();
        authenticate();
        monitoring();
    }


}
