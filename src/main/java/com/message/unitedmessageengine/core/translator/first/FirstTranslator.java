package com.message.unitedmessageengine.core.translator.first;

import com.message.unitedmessageengine.constant.ProtocolConstant.ProtocolType;
import com.message.unitedmessageengine.core.translator.Translator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import static com.message.unitedmessageengine.constant.ProtocolConstant.First.*;
import static com.message.unitedmessageengine.utils.ByteUtils.mergeByteArrays;

@Slf4j
@Component
@RequiredArgsConstructor
@Qualifier("firstTranslator")
public class FirstTranslator implements Translator {

    private final FirstFileTranslator fileTranslator;

    @Override
    public Object translateToInternalProtocol(ProtocolType type, Object element) {
        return null;
    }

    @Override
    public Optional<byte[]> addTcpFraming(ProtocolType type, byte[] payload) {
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
        var result = mergeByteArrays(begin.getBytes(CHARSET), payload);
        result = mergeByteArrays(result, PROTOCOL_SUFFIX.getBytes(CHARSET));
        return Optional.of(result);
    }

    public byte[] convertToBytes(Object obj) {
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

    public Optional<Map<String, String>> covertToMap(String data) {
        var dataMap = new HashMap<String, String>();
        try {
            var st = new StringTokenizer(data, "\r\n");
            var header = st.nextToken().split(" ");
            dataMap.put(header[0], header[1]);
            while (st.hasMoreTokens()) {
                var token = st.nextToken().split(":");
                if (token.length != 2) continue;
                dataMap.put(token[0], token[1]);
            }
        } catch (Exception e) {
            log.info("[수신] 전문 이상 발생 ::: payload {}", data);
            return Optional.empty();
        }
        return Optional.of(dataMap);
    }

}
