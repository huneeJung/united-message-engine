package com.message.unitedmessageengine.core.socket.service;

import com.message.unitedmessageengine.core.socket.vo.ConnectA;
import com.message.unitedmessageengine.core.socket.vo.PingA;
import com.message.unitedmessageengine.core.translater.service.TranslateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.AgentA.CHARSET;
import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.ProtocolType.CONNECT;
import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.ProtocolType.PING;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocketChannelServiceA implements SocketChannelService {

    private static final Integer BUFFER_SIZE = 10 * 1024; // 10KB

    @Qualifier("A")
    private final TranslateService translateService;
    @Value("${agentA.connect.username}")
    private String username;
    @Value("${agentA.connect.password}")
    private String password;
    @Value("${agentA.version}")
    private String version;

    public void authenticate(String line, SocketChannel channel) throws IOException {
        var connectVO = ConnectA.builder().USERNAME(username).PASSWORD(password)
                .LINE(line).VERSION(version).build();
        var authPayload = translateService.translateToExternalProtocol(CONNECT, connectVO);
        var authBuffer = ByteBuffer.wrap(authPayload);
        channel.write(authBuffer);
    }

    public void sendPing(SocketChannel senderChannel) throws IOException {
        var pingVO = new PingA();
        var pingPayload = translateService.translateToExternalProtocol(PING, pingVO);
        var pingBuffer = ByteBuffer.wrap(pingPayload);
        senderChannel.write(pingBuffer);
    }

    public void processAck(byte[] ackBytes) {
        var ackStr = new String(ackBytes, CHARSET);
        log.info("ackData : {}", ackStr);
    }

    public void processResult(byte[] resultBytes) {
        var ackStr = new String(resultBytes, CHARSET);
        log.info("resultData : {}", ackStr);
    }

}
