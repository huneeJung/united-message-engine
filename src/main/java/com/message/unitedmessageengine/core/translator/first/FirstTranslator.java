package com.message.unitedmessageengine.core.translator.first;

import com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.ProtocolType;
import com.message.unitedmessageengine.core.translator.Translator;
import com.message.unitedmessageengine.core.worker.result.dto.ResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.First.*;

@Slf4j
@Component
@Qualifier("firstTranslator")
public class FirstTranslator implements Translator {


    @Override
    public ResultDto translateToInternalProtocol(ProtocolType type, Object element) {
        Map<String, String> result = (Map<String, String>) element;
        return ResultDto.builder()
                .messageId(result.get("KEY"))
                .resultCode(result.get("CODE"))
                .resultMessage(result.get("MESSAGE"))
                .build();
    }

    @Override
    public Optional<byte[]> translateToExternalProtocol(ProtocolType type, Object oriPayload) throws IOException {
        if (ObjectUtils.isEmpty(oriPayload)) {
            log.warn("[Translator] 외부 규격 변환 실패 - Payload 빈값");
            return Optional.empty();
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // OBJECT 바이너리화
            var convertedPayload = convertToByteArray(oriPayload);
            String begin;
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
                    log.warn("[Translator] 외부 규격 변환 실패 - 지원하지 않는 헤더 타입");
                    return Optional.empty();
            }
            out.write(begin.getBytes(CHARSET));
            out.write(convertedPayload);
            out.write(PROTOCOL_SUFFIX.getBytes(CHARSET));
            return Optional.of(out.toByteArray());
        }
    }

    private byte[] convertToByteArray(Object obj) {
        StringBuilder sb = new StringBuilder();

        // 리플렉션을 사용하여 객체의 필드와 값을 맵에 저장
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(obj.getClass(), field.getName());
            if (pd != null) {
                BeanWrapper beanWrapper = new BeanWrapperImpl(obj);
                Object value = beanWrapper.getPropertyValue(pd.getName());
                sb.append(field.getName()).append(":").append(value).append("\r\n");
            }
        }
        return sb.toString().getBytes(CHARSET);
    }

    public Map<String, String> covertToMap(String data) {
        var st = new StringTokenizer(data, "\r\n");
        var header = st.nextToken().split(" ");
        var dataMap = new HashMap<String, String>();
        dataMap.put(header[0], header[1]);
        while (st.hasMoreTokens()) {
            var token = st.nextToken().split(":");
            if (token.length != 2) continue;
            dataMap.put(token[0], token[1]);
        }
        return dataMap;
    }

}
