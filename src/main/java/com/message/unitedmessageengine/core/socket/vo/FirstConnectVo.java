package com.message.unitedmessageengine.core.socket.vo;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@RequiredArgsConstructor
public class FirstConnectVo {

    private final String USERNAME;

    private final String PASSWORD;
    // SEND OR REPORT
    private final String LINE;

    private final String VERSION;

}
