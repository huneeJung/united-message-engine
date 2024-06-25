package com.message.unitedmessageengine.core.socket.manager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.nio.channels.SocketChannel;
import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class MainChannel implements Comparable<MainChannel> {

    private Instant lastUsedTime;
    private SocketChannel socketChannel;

    public static MainChannel create(SocketChannel socketChannel) {
        return MainChannel.builder().lastUsedTime(Instant.now()).socketChannel(socketChannel).build();
    }

    @Override
    public int compareTo(MainChannel channel) {
        return this.lastUsedTime.compareTo(channel.lastUsedTime);
    }
}
