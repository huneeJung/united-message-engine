package com.message.unitedmessageengine.core.socket.vo;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FirstPingVo {

    private String KEY = UUID.randomUUID().toString().replaceAll("-", "");

}
