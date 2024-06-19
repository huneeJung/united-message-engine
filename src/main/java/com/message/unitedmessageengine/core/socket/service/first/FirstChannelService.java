package com.message.unitedmessageengine.core.socket.service.first;

import com.message.unitedmessageengine.core.socket.service.ChannelService;
import com.message.unitedmessageengine.core.socket.vo.FirstConnectVo;
import com.message.unitedmessageengine.core.socket.vo.FirstPingVo;
import com.message.unitedmessageengine.core.translator.first.FirstTranslator;
import com.message.unitedmessageengine.core.worker.result.dto.ResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

import static com.message.unitedmessageengine.core.queue.QueueManager.RESULT_QUEUE;
import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.First.REPORT;
import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.First.*;
import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.ProtocolType.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Qualifier("firstChannelService")
public class FirstChannelService implements ChannelService {

    @Qualifier("firstTranslator")
    private final FirstTranslator translator;

    @Value("${agentA.connect.username}")
    private String username;
    @Value("${agentA.connect.password}")
    private String password;
    @Value("${agentA.version}")
    private String version;

    public void authenticate(String channelType, SocketChannel channel) {
        try {
            var connectVO = FirstConnectVo.builder().USERNAME(username).PASSWORD(password)
                    .LINE(channelType).VERSION(version).build();
            var authPayload = translator.translateToExternalProtocol(CONNECT, connectVO);
            if (authPayload.isEmpty()) return;
            var authBuffer = ByteBuffer.wrap(authPayload.get());
            channel.write(authBuffer);
        } catch (IOException e) {
            log.info("[{} Channel] 인증 에러 발생", channelType);
        }
    }

    public void sendPing(SocketChannel senderChannel) throws IOException {
        var pingVO = new FirstPingVo();
        var pingPayload = translator.translateToExternalProtocol(PING, pingVO);
        if (pingPayload.isEmpty()) return;
        var pingBuffer = ByteBuffer.wrap(pingPayload.get());
        senderChannel.write(pingBuffer);
    }

    public void processSendResponse(byte[] payload) {
        process(SEND, payload);
    }

    public void processReportResponse(byte[] payload) {
        process(REPORT, payload);
    }

    private void process(String channelType, byte[] payload) {
        var payloadStr = new String(payload, CHARSET);
        var resultData = translator.covertToMap(payloadStr);
        var header = resultData.get("BEGIN");
        var key = resultData.getOrDefault("KEY", null);
        // PONG 인 경우
        if (header.equals(PONG.name())) {
            log.info("[PONG] 수신 ::: key {}", key);
            return;
        }
        // 인증 응답인 경우
        if (key == null) {
            checkAuth(channelType, resultData);
            return;
        }
        RESULT_QUEUE.add(
                ResultDto.builder()
                        .messageId(resultData.get("KEY"))
                        .resultCode(resultData.get("CODE"))
                        .resultMessage(resultData.get("DATA"))
                        .build()
        );
        log.info("[RESULT QUEUE] 메시지 결과 삽입 ::: queueSize {}", RESULT_QUEUE.size());
    }

    private void checkAuth(String channelType, Map<String, String> authData) {
        var code = authData.get("CODE");
        var message = authData.get("DATA");
        if (code.equals("100")) {
            log.info("[{} Channel] 인증 완료 ::: code {}, message {}", channelType, code, message);
        } else {
            throw new RuntimeException(String.format("[Channel] 인증 실패 ::: code %s, message %s", code, message));
        }
    }

}
