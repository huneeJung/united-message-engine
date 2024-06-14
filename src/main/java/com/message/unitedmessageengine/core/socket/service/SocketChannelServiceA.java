package com.message.unitedmessageengine.core.socket.service;

import com.message.unitedmessageengine.core.socket.vo.ConnectA;
import com.message.unitedmessageengine.core.socket.vo.PingA;
import com.message.unitedmessageengine.core.translater.service.TranslateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.ProtocolType.CONNECT;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocketChannelServiceA implements SocketChannelService {

    @Qualifier("A")
    private final TranslateService translateService;

    public void authenticate(String line, SocketChannel channel) throws IOException {
        var connectVO = ConnectA.builder().LINE(line);
        var authPayload = translateService.translateToExternalProtocol(CONNECT, connectVO);
        var authBuffer = ByteBuffer.wrap(authPayload);
        channel.write(authBuffer);
    }

    public void sendPing(SocketChannel senderChannel) throws IOException {
        var pingVO = PingA.builder().build();
        var pingPayload = translateService.translateToExternalProtocol(CONNECT, pingVO);
        var pingBuffer = ByteBuffer.wrap(pingPayload);
        senderChannel.write(pingBuffer);
    }

    public void receivePong(SocketChannel receiverChannel) throws IOException {

    }

    @Async
    public void processAck(SocketChannel channel) {

    }

    @Async
    public void processResult(SocketChannel channel) {

    }

}
