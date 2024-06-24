package com.message.unitedmessageengine.tcp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@SpringBootTest
class BandWidthTest {

    @Value("${agentA.host}")
    private String host;

    @Value("${agentA.port}")
    private Integer port;

    @Test
    public void test() throws IOException {
        var channel = SocketChannel.open();
        channel.socket().connect(new InetSocketAddress(host, port));

        long startTime = System.nanoTime();
        channel.write(ByteBuffer.wrap(new byte[1]));
        long endTime = System.nanoTime();

        long latency = endTime - startTime;
        System.out.println("Latency: " + latency + " nanoseconds");
    }

}
