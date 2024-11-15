package com.message.unitedmessageengine.config;

import com.message.unitedmessageengine.core.socket.manager.ChannelManager;
import com.message.unitedmessageengine.core.socket.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChannelManagerConfig {

    @Qualifier("firstChannelService")
    private final ChannelService channelService;

    @Value("${tcp.connect-timeout:30000}")
    private Integer connectTimeout;
    @Value("${tcp.read-timeout:5000}")
    private Integer readTimeout;
    @Value("${tcp.select-timeout:5000}")
    private Integer selectTimeout;
    @Value("${tcp.ping-cycle:30000}")
    private Integer pingCycle;
    @Value("${agentA.host}")
    private String host;
    @Value("${agentA.port}")
    private Integer port;
    @Value("${agentA.connect.senderCnt:1}")
    private Integer senderCnt;
    @Value("${agentA.connect.reportCnt:1}")
    private Integer reportCnt;
    @Value("${agentA.useYN:Y}")
    private String isAliveChannelManager;

    @Bean(name = "firstChannelManager")
    public ChannelManager<ChannelService> channelManager() {
        return ChannelManager.builder()
                .socketChannelService(channelService)
                .host(host)
                .port(port)
                .senderCnt(senderCnt)
                .reportCnt(reportCnt)
                .pingCycle(pingCycle)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .selectTimeout(selectTimeout)
                .build();
    }

}
