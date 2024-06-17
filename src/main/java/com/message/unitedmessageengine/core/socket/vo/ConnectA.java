package com.message.unitedmessageengine.core.socket.vo;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ConnectA {


    private String USERNAME;

    private String PASSWORD;

    // SEND OR REPORT
    private String LINE;

    private String VERSION;

}
