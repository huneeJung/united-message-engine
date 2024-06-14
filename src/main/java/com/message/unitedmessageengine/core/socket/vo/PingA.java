package com.message.unitedmessageengine.core.socket.vo;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PingA {

    private String KEY = UUID.randomUUID().toString().replaceAll("-", "");
    
}
