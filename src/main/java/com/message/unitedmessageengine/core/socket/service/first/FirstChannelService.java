package com.message.unitedmessageengine.core.socket.service.first;

import com.message.unitedmessageengine.core.socket.service.ChannelService;
import com.message.unitedmessageengine.core.socket.vo.FirstConnectVo;
import com.message.unitedmessageengine.core.socket.vo.FirstPingVo;
import com.message.unitedmessageengine.core.translator.Translator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.First.CHARSET;
import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.ProtocolType.CONNECT;
import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.ProtocolType.PING;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirstChannelService implements ChannelService {

    @Qualifier("First")
    private final Translator translator;

    @Value("${agentA.connect.username}")
    private String username;
    @Value("${agentA.connect.password}")
    private String password;
    @Value("${agentA.version}")
    private String version;

    public void authenticate(String line, SocketChannel channel) throws IOException {
        var connectVO = FirstConnectVo.builder().USERNAME(username).PASSWORD(password)
                .LINE(line).VERSION(version).build();
        var authPayload = translator.translateToExternalProtocol(CONNECT, connectVO);
        if (authPayload.isEmpty()) return;
        var authBuffer = ByteBuffer.wrap(authPayload.get());
        channel.write(authBuffer);
    }

    public void sendPing(SocketChannel senderChannel) throws IOException {
        var pingVO = new FirstPingVo();
        var pingPayload = translator.translateToExternalProtocol(PING, pingVO);
        if (pingPayload.isEmpty()) return;
        var pingBuffer = ByteBuffer.wrap(pingPayload.get());
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
