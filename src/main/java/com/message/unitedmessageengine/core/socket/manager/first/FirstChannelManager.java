package com.message.unitedmessageengine.core.socket.manager.first;

import com.message.unitedmessageengine.core.socket.manager.AbstractChannelManager;
import com.message.unitedmessageengine.core.socket.service.ChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;

import static com.message.unitedmessageengine.constant.FirstConstant.CHARSET;
import static com.message.unitedmessageengine.constant.FirstConstant.PROTOCOL_PREFIX;

@Slf4j
@Component
@DependsOn("firstChannelService")
public class FirstChannelManager extends AbstractChannelManager<ChannelService> {

    @Autowired
    public FirstChannelManager(ChannelService channelService) {
        super(channelService);
    }

    protected Queue<String> parsePayload(ByteBuffer buffer, byte[] payload) {
        // 구분자로 데이터 파싱
        String dataStr = new String(payload, StandardCharsets.UTF_8);
        var dataArr = dataStr.split("END\\r\\n");
        buffer.clear();
        var dataQueue = new ArrayDeque<String>();
        for (int i = 0; i < dataArr.length; i++) {
            var data = dataArr[i];
            if (!data.startsWith(PROTOCOL_PREFIX)) continue;
            if (!data.endsWith("\r\n")) {
                if (i == dataArr.length - 1) buffer.put(data.getBytes(CHARSET));
                continue;
            }
            dataQueue.offer(data);
        }
        return dataQueue;
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
