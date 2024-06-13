package com.message.unitedmessageengine.core.socket.manager;

import com.message.unitedmessageengine.core.socket.service.SocketChannelServiceA;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SocketChannelManagerA extends AbstractSocketChannelManager<SocketChannelServiceA> {

    @Autowired
    public SocketChannelManagerA(SocketChannelServiceA socketChannelService) {
        super(socketChannelService);
    }

    @PostConstruct
    public void init() throws IOException {
        host = System.getProperty("");
        port = Integer.parseInt(System.getProperty(""));
        isAliveSelector = true;
        tcpConnect();
        monitoring();
    }

}
