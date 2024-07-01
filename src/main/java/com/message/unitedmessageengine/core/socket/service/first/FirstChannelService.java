package com.message.unitedmessageengine.core.socket.service.first;

import com.message.unitedmessageengine.constant.FirstConstant.ProtocolType;
import com.message.unitedmessageengine.core.socket.manager.ChannelManager.ChannelType;
import com.message.unitedmessageengine.core.socket.service.ChannelService;
import com.message.unitedmessageengine.core.socket.vo.ConnectVo;
import com.message.unitedmessageengine.core.socket.vo.PingVo;
import com.message.unitedmessageengine.core.socket.vo.ReportAckVo;
import com.message.unitedmessageengine.core.translator.first.FirstTranslator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;

import static com.message.unitedmessageengine.constant.FirstConstant.CHARSET;
import static com.message.unitedmessageengine.constant.FirstConstant.PROTOCOL_PREFIX;
import static com.message.unitedmessageengine.constant.FirstConstant.ProtocolType.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Qualifier("firstChannelService")
public class FirstChannelService implements ChannelService {

    @Qualifier("firstTranslator")
    private final FirstTranslator translator;
    private final JdbcTemplate jdbcTemplate;
    private final String statusUpdateSql = """
            UPDATE MESSAGE SET STATUS_CODE=?, RESULT_CODE=?, RESULT_MESSAGE=? 
            where MESSAGE_ID=? AND STATUS_CODE!='C'
            """;

    @Value("${agentA.connect.username}")
    private String username;
    @Value("${agentA.connect.password}")
    private String password;
    @Value("${agentA.version}")
    private String version;

    public Queue<String> readPartialData(ByteBuffer buffer, byte[] payload) {
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
                break;
            }
            dataQueue.offer(data);
        }
        return dataQueue;
    }

    public void authenticate(ChannelType type, SocketChannel channel) {
        try {
            var line = type.equals(ChannelType.SEND) ? ProtocolType.SEND : ProtocolType.REPORT;
            var connectVO = ConnectVo.builder().USERNAME(username).PASSWORD(password)
                    .LINE(line.name()).VERSION(version).build();
            var payload = translator.convertToBytes(connectVO);
            var authPayload = translator.addTcpFraming(CONNECT, payload);
            if (authPayload.isEmpty()) return;
            var authBuffer = ByteBuffer.wrap(authPayload.get());
            channel.write(authBuffer);
        } catch (IOException e) {
            log.info("[{} Channel] 인증 에러 발생", type);
        }
    }

    public void writePing(SocketChannel senderChannel) {
        try {
            var pingVO = new PingVo();
            var payload = translator.convertToBytes(pingVO);
            var pingPayload = translator.addTcpFraming(PING, payload);
            if (pingPayload.isEmpty()) return;
            var pingBuffer = ByteBuffer.wrap(pingPayload.get());
            senderChannel.write(pingBuffer);
        } catch (IOException e) {
            log.warn("[PING] 처리 에러 발생 ::: message {}", e.getMessage());
            log.warn("", e);
        }
    }

    public void consumeSendResponse(SocketChannel sendChannel, Queue<String> dataQueue) {
        while (!dataQueue.isEmpty()) {
            var data = dataQueue.poll();
            var mapDataOpt = translator.covertToMap(data);
            if (mapDataOpt.isEmpty()) continue;
            var mapData = mapDataOpt.get();
            var header = mapData.get("BEGIN");
            var key = mapData.getOrDefault("KEY", null);
            // PONG 인 경우
            if (header.equals(PONG.name())) continue;
            // 인증 응답인 경우
            if (key == null) {
                checkAuth(SEND.name(), mapData);
                continue;
            }
            var statusCode = "";
            if (mapData.getOrDefault("CODE", "100").equals("100")) statusCode = "P";
            else statusCode = "F";
            jdbcTemplate.update(statusUpdateSql, statusCode, mapData.get("CODE"), mapData.get("DATA"), key);
        }
    }

    public void consumeReportResponse(SocketChannel reportChannel, Queue<String> dataQueue) {
        if (!reportChannel.isConnected()) throw new RuntimeException("Report Channel is disconnected");
        while (!dataQueue.isEmpty()) {
            var data = dataQueue.poll();
            var mapDataOpt = translator.covertToMap(data);
            if (mapDataOpt.isEmpty()) continue;
            var mapData = mapDataOpt.get();
            var header = mapData.get("BEGIN");
            var key = mapData.getOrDefault("KEY", null);
            // PONG 인 경우
            if (header.equals(PONG.name())) continue;
            // 인증 응답인 경우
            if (key == null) {
                checkAuth(REPORT.name(), mapData);
                continue;
            }
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                executor.submit(() -> {
                    jdbcTemplate.update(statusUpdateSql, "C", mapData.get("CODE"), mapData.get("DATA"), key);
                    try {
                        var reportAckVo = new ReportAckVo(key);
                        var payload = translator.convertToBytes(reportAckVo);
                        var reportAckPayload = translator.addTcpFraming(ACK, payload);
                        if (reportAckPayload.isEmpty()) return;
                        var reportAckBuffer = ByteBuffer.wrap(reportAckPayload.get());
                        reportChannel.write(reportAckBuffer);
                    } catch (
                            IOException e) {
                        log.info("[REPORT CHANNEL] 결과 수신 응답 실패 ::: messageId {}", key);
                    }
                });
            }
        }
    }

    private void checkAuth(String channelType, Map<String, String> authData) {
        var code = authData.get("CODE");
        var messageEntity = authData.get("DATA");
        if (code.equals("100")) {
            log.info("[{} Channel] 인증 완료 ::: code {}, messageEntity {}", channelType, code, messageEntity);
        } else {
            throw new RuntimeException(String.format("[Channel] 인증 실패 ::: code %s, messageEntity %s", code, messageEntity));
        }
    }

}
