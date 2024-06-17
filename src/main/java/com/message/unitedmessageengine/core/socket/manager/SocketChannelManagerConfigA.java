package com.message.unitedmessageengine.core.socket.manager;

import com.message.unitedmessageengine.core.socket.service.SocketChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DependsOn("jasyptStringEncryptor")
public class SocketChannelManagerConfigA extends AbstractSocketChannelManager<SocketChannelService> {

    @Autowired
    public SocketChannelManagerConfigA(SocketChannelService socketChannelService) {
        super(socketChannelService);
    }

    @Value("${agentA.useYN:Y}")
    public void setIsAliveProcessor(String isAliveProcessor) {
        if (isAliveProcessor.equals("Y")) super.isAliveProcessor = true;
    }

    @Value("${agentA.host}")
    public void setHost(String host) {
        super.host = host;
    }

    @Value("${agentA.port}")
    public void setPort(Integer port) {
        super.port = port;
    }

}
