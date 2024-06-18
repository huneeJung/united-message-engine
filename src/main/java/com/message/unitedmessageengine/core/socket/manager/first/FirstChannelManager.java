package com.message.unitedmessageengine.core.socket.manager.first;

import com.message.unitedmessageengine.core.socket.manager.AbstractChannelManager;
import com.message.unitedmessageengine.core.socket.service.ChannelService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.nio.channels.SocketChannel;

import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.First.REPORT;
import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.First.SEND;

@Slf4j
@Component
@DependsOn("firstChannelService")
public class FirstChannelManager extends AbstractChannelManager<ChannelService> {

    public void send(Object data) {

    }

    protected void connectSendChannel(){
        sendChannel = tcpConnect();
        socketChannelService.authenticate(SEND, sendChannel);
    }

    protected void connectReportChannel(){
        reportChannel = tcpConnect();
        socketChannelService.authenticate(REPORT, reportChannel);
    }

    @Autowired
    public FirstChannelManager(ChannelService channelService) {
        super(channelService);
    }

    @Value("${agentA.useYN:Y}")
    public void setIsAliveProcessor(String isAliveProcessor) {
        if (isAliveProcessor.equals("Y")) super.isAliveChannelManager = true;
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
