package com.message.unitedmessageengine.core.socket.service.first;

import com.message.unitedmessageengine.constant.FirstConstant.ProtocolType;
import com.message.unitedmessageengine.core.socket.manager.AbstractChannelManager.ChannelType;
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
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;

import static com.message.unitedmessageengine.constant.FirstConstant.ProtocolType.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Qualifier("firstChannelService")
public class FirstChannelService implements ChannelService {

    private final JdbcTemplate jdbcTemplate;
    @Qualifier("firstTranslator")
    private final FirstTranslator translator;
    private final String statusUpdateSql = """
            UPDATE MESSAGE SET STATUS_CODE=?, RESULT_CODE=?, RESULT_MESSAGE=? 
            where MESSAGE_ID=?
            """;

    @Value("${agentA.connect.username}")
    private String username;
    @Value("${agentA.connect.password}")
    private String password;
    @Value("${agentA.version}")
    private String version;

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

    public void processSendResponse(SocketChannel reportChannel, Queue<String> dataQueue) {
        while (!dataQueue.isEmpty()) {
            var data = dataQueue.poll();
            var mapDataOpt = translator.covertToMap(data);
            if (mapDataOpt.isEmpty()) continue;
            var mapData = mapDataOpt.get();
            var header = mapData.get("BEGIN");
            var key = mapData.getOrDefault("KEY", null);
            // PONG 인 경우
            if (header.equals(PONG.name())) {
//                log.info("[PONG] 수신 ::: key {}", key);
                continue;
            }
            // 인증 응답인 경우
            if (key == null) {
                checkAuth(SEND.name(), mapData);
                continue;
            }

            var statusCode = "";
            if (mapData.getOrDefault("CODE", "100").equals("100")) statusCode = "P";
            else statusCode = "F";
            jdbcTemplate.update(statusUpdateSql, statusCode, mapData.get("CODE"), mapData.get("DATA"), key);
//            log.info("[ACK QUEUE] 메시지 결과 삽입 ::: messageId {}", key);
        }
    }

    public void processReportResponse(SocketChannel reportChannel, Queue<String> dataQueue) {
        while (!dataQueue.isEmpty()) {
            var data = dataQueue.poll();
            var mapDataOpt = translator.covertToMap(data);
            if (mapDataOpt.isEmpty()) continue;
            var mapData = mapDataOpt.get();
            var header = mapData.get("BEGIN");
            var key = mapData.getOrDefault("KEY", null);
            // PONG 인 경우
            if (header.equals(PONG.name())) {
//                log.info("[PONG] 수신 ::: key {}", key);
                continue;
            }
            // 인증 응답인 경우
            if (key == null) {
                checkAuth(REPORT.name(), mapData);
                continue;
            }
//            RESULT_QUEUE.add(
//                    ResultDto.builder()
//                            .messageId(key)
//                            .resultCode(mapData.get("CODE"))
//                            .resultMessage(mapData.get("DATA"))
//                            .build()
//            );
//            log.info("[RESULT QUEUE] 메시지 결과 삽입 ::: messageId {}", key);

            if (!reportChannel.isConnected()) return;

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
//                        log.info("[REPORT CHANNEL] 결과 수신 응답 성공 ::: messageId {}", key);
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
