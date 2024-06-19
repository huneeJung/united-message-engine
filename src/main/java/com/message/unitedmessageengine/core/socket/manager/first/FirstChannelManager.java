package com.message.unitedmessageengine.core.socket.manager.first;

import com.message.unitedmessageengine.core.socket.manager.AbstractChannelManager;
import com.message.unitedmessageengine.core.socket.service.ChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.First.REPORT;
import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.First.SEND;

@Slf4j
@Component
@DependsOn("firstChannelService")
public class FirstChannelManager extends AbstractChannelManager<ChannelService> {

    @Autowired
    public FirstChannelManager(ChannelService channelService) {
        super(channelService);
    }
    
    protected void connectSendChannel() {
        var sendChannel = tcpConnect();
        socketChannelService.authenticate(SEND, sendChannel);
        sendChannelList.add(sendChannel);
    }

    protected void connectReportChannel() {
        var reportChannel = tcpConnect();
        socketChannelService.authenticate(REPORT, reportChannel);
        reportChannelList.add(reportChannel);
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

    @Value("${agentA.connect.senderCnt:1}")
    public void setSenderCnt(Integer senderCnt) {
        super.senderCnt = senderCnt;
    }

    @Value("${agentA.connect.reportCnt:1}")
    public void setReportCnt(Integer reportCnt) {
        super.reportCnt = reportCnt;
    }

}
