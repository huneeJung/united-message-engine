package com.message.unitedmessageengine.core.socket.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class PingVo {

    private final String KEY = UUID.randomUUID().toString().replaceAll("-", "");

}
