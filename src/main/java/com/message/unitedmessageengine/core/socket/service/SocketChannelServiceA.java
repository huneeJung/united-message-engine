package com.message.unitedmessageengine.core.socket.service;

import com.message.unitedmessageengine.core.translater.service.TranslateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.channels.SocketChannel;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocketChannelServiceA implements SocketChannelService {

    @Qualifier("A")
    private final TranslateService translateService;

    @Async
    public void processAck(SocketChannel channel) {

    }

    @Async
    public void processResult(SocketChannel channel) {

    }

}
