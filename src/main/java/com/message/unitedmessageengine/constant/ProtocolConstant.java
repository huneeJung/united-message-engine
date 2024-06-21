package com.message.unitedmessageengine.constant;

import lombok.Getter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ProtocolConstant {

    @Getter
    public enum ProtocolType {
        CONNECT, SMS, MMS, KKO, REPORT, ACK, PING, PONG
    }

    public static class First {
        public static final Charset CHARSET = StandardCharsets.UTF_8;
        public static final String PROTOCOL_PREFIX = "BEGIN ";
        public static final String PROTOCOL_SUFFIX = "END\r\n";
        public static final String PROTOCOL_DELIMITER = "\r\n";
        public static final String SEND = "SEND";
        public static final String REPORT = "REPORT";

        public static final Map<String, String> CONVERT_TYPE =
                Map.of(
                        "SMS", "SMS",
                        "LMS", "TXT",
                        "MMS", "IMT"
                );
    }

}
