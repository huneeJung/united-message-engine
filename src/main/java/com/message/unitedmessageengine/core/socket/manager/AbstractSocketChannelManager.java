package com.message.unitedmessageengine.core.socket.manager;

import com.message.unitedmessageengine.core.socket.service.SocketChannelService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSocketChannelManager<T extends SocketChannelService> {

    protected final T socketChannelService;
    protected boolean isAliveSelector;
    protected SocketChannel senderChannel;
    protected SocketChannel receiverChannel;
    protected String host;
    protected Integer port;

    private Selector selector;
    private Thread eventCatchThread;
    @Value("")
    private Integer connectTimeout;
    @Value("")
    private Integer readTimeout;

    public abstract void init() throws IOException;

    protected void monitoring() throws IOException {
        try {
            eventCatchThread = new Thread(() -> {
                while (true) {
                    var selectedKeys = selector.selectedKeys();
                    var keyIterator = selectedKeys.iterator();
                    while (keyIterator.hasNext()) {
                        var key = keyIterator.next();
                        if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            // ACK 수신
                            if (senderChannel.equals(channel)) {
                                socketChannelService.processAck(channel);
                            }
                            // RESULT 수신
                            if (receiverChannel.equals(channel)) {
                                socketChannelService.processResult(channel);
                            }
                        }
                        keyIterator.remove();
                    }
                }
            });
            eventCatchThread.setDaemon(true);
            eventCatchThread.start();
        } catch (Exception e) {
            selector.close();
            isAliveSelector = false;
            log.error("[SELECTOR 수신 이벤트 처리중 이상 발생] ::: message {}", e.getMessage());
            log.error("", e);
        }
    }

    protected void tcpConnect() {
        try {
            selector = Selector.open();
            // SEND TCP CONNECT
            senderChannel = SocketChannel.open();
            senderChannel.socket().connect(new InetSocketAddress(host, port), connectTimeout);
            senderChannel.socket().setSoTimeout(readTimeout);
            senderChannel.register(selector, SelectionKey.OP_READ);
            // RECEIVER TCP CONNECT
            receiverChannel = SocketChannel.open();
            receiverChannel.socket().connect(new InetSocketAddress(host, port), connectTimeout);
            receiverChannel.socket().setSoTimeout(readTimeout);
            receiverChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            log.info("TCP 연결중 에러 발생 ::: message {}, host {}, port {}", e.getMessage(), host, port);
            log.info("", e);
        }
    }

    @Scheduled(fixedDelayString = "1000L")
    public void healthCheck() {
        try {
            if (!isAliveSelector || !eventCatchThread.isAlive()) {
                selector = Selector.open();
                senderChannel.register(selector, SelectionKey.OP_READ);
                receiverChannel.register(selector, SelectionKey.OP_READ);
                monitoring();
                log.error("[SELECTOR 초기화 수행] ::: isAliveSelector {}", isAliveSelector);
            }
            if (!senderChannel.isConnected()) {
                senderChannel.socket().connect(new InetSocketAddress(host, port), connectTimeout);
                senderChannel.socket().setSoTimeout(readTimeout);
                log.error("[sendSocketChannel 연결 끊김 Reconnect 수행] ::: host {} , port {}", host, port);
            }
            if (!receiverChannel.isConnected()) {
                receiverChannel.socket().connect(new InetSocketAddress(host, port), connectTimeout);
                receiverChannel.socket().setSoTimeout(readTimeout);
                log.error("[receiverSocketChannel 연결 끊김 Reconnect 수행] ::: host {} , port {}", host, port);
            }
        } catch (IOException e) {
            log.info("TCP 연결중 에러 발생 ::: message {}, host {}, port {}", e.getMessage(), host, port);
            log.info("", e);
        }
    }

    @PreDestroy
    public void destroy() throws IOException {
        selector.close();
        senderChannel.close();
        receiverChannel.close();
    }

}
