package com.message.unitedmessageengine.core.socket.vo;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectA {

    @Value("${agentA.version}")
    private String VERSION;

    @Value("${agentA.connect.username}")
    private String USERNAME;

    @Value("${agentA.connect.password}")
    private String PASSWORD;

    // SEND OR REPORT
    private String LINE;

}
