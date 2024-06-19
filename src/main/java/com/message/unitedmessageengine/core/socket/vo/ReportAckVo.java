package com.message.unitedmessageengine.core.socket.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReportAckVo {

    private final String KEY;
    private final String CODE;
    private final String DATA;

    public ReportAckVo(String messageId) {
        KEY = messageId;
        CODE = "100";
        DATA = "Success";
    }

}
