package com.message.unitedmessageengine.core.translater.service;

import com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.ProtocolType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.AgentA.*;
import static com.message.unitedmessageengine.core.translater.service.TranslateService.convertToByteArray;

@Slf4j
@Component
@Qualifier("A")
public class TranslateServiceA implements TranslateService {

    @Override
    public byte[] translateToExternalProtocol(ProtocolType type, Object oriPayload) {
        if (ObjectUtils.isEmpty(oriPayload)) {
            log.error("[Payload 변환 실패]  oriPayload 빈값");
            return null;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // OBJECT 바이너리화
            var convertedPayload = convertToByteArray(oriPayload);
            String begin = null;
            switch (type) {
                case CONNECT:
                    begin = PROTOCOL_PREFIX + "CONNECT" + PROTOCOL_DELIMITER;
                    break;
                case PING:
                    begin = PROTOCOL_PREFIX + "PING" + PROTOCOL_DELIMITER;
                    break;
                case SMS:
                    begin = PROTOCOL_PREFIX + "SMS" + PROTOCOL_DELIMITER;
                    break;
                case MMS:
                    begin = PROTOCOL_PREFIX + "MMS" + PROTOCOL_DELIMITER;
                    break;
                case KKO:
                    begin = PROTOCOL_PREFIX + "KKO" + PROTOCOL_DELIMITER;
                    break;
                case ACK:
                    begin = PROTOCOL_PREFIX + "ACK" + PROTOCOL_DELIMITER;
                    break;
                default:
                    throw new RuntimeException("지원하지 않는 헤더타입입니다.");
            }
            out.write(begin.getBytes(CHARSET));
            out.write(convertedPayload);
            out.write(PROTOCOL_SUFFIX.getBytes(CHARSET));
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
