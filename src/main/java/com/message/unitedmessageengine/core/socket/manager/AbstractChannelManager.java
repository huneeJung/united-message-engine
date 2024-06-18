package com.message.unitedmessageengine.core.socket.manager;

import com.message.unitedmessageengine.core.socket.service.ChannelService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractChannelManager<T extends ChannelService> {

    protected final T socketChannelService;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(10 * 1024);
    protected boolean isAliveChannelManager;
    protected SocketChannel sendChannel;
    protected SocketChannel reportChannel;
    protected String host;
    protected Integer port;

    private Selector selector;
    private ScheduledExecutorService anomalyDetectionObserver;
    @Value("${tcp.connect-timeout}")
    private Integer connectTimeout;
    @Value("${tcp.read-timeout:5000}")
    private Integer readTimeout;
    @Value("${tcp.select-timeout:5000}")
    private Integer selectTimeout;
    @Value("${tcp.ping-cycle:30000}")
    private Integer pingCycle;

    @PostConstruct
    public void init() throws IOException {
        if (!isAliveChannelManager) return;
        selector = Selector.open();
        connectSendChannel();
        connectReportChannel();
        performEventObserver();
        anomalyDetectionObserver = Executors.newSingleThreadScheduledExecutor();
        anomalyDetectionObserver.scheduleAtFixedRate(this::monitoring, pingCycle, pingCycle, TimeUnit.MILLISECONDS);
    }

    public abstract void send(Object data);

    protected abstract void connectSendChannel();

    protected abstract void connectReportChannel();

    protected SocketChannel tcpConnect() {
        try {
            // SEND TCP CONNECT
            var channel = SocketChannel.open();
            channel.socket().connect(new InetSocketAddress(host, port), connectTimeout);
            channel.socket().setSoTimeout(readTimeout);
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            return channel;
        } catch (IOException e) {
            log.error("[SOCKET CHANNEL] Connect 에러 발생 ::: message {}, host {}, port {}", e.getMessage(), host, port);
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

    private void performEventObserver() {
        var eventObserver = new Thread(() -> {
            while (isAliveChannelManager) {
                try {
                    selector.select(selectTimeout);

                    var selectedKeys = selector.selectedKeys();
                    var keyIterator = selectedKeys.iterator();
                    while (keyIterator.hasNext()) {
                        var key = keyIterator.next();
                        if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            if(!channel.isConnected()){
                                key.cancel();
                                channel.close();
                                continue;
                            }
                            // SEND 수신
                            if (sendChannel.equals(channel)) {
                                var readCnt = channel.read(readBuffer);
                                if (readCnt <= 0) log.info("[SEND CHANNEL] 이벤트 처리 데이터 없음");
                                socketChannelService.processSendResponse(getPayload(readCnt));
                            }
                            // REPORT 수신
                            if (reportChannel.equals(channel)) {
                                var readCnt = channel.read(readBuffer);
                                if (readCnt <= 0) log.info("[REPORT CHANNEL] 이벤트 처리 데이터 없음");
                                socketChannelService.processReportResponse(getPayload(readCnt));
                            }
                        }
                        readBuffer.clear();
                        keyIterator.remove();
                    }
                } catch (ClosedSelectorException e) {
                    isAliveChannelManager = false;
                    log.info("[SELECTOR] 종료 작업 수행");
                } catch (Exception e) {
                    isAliveChannelManager = false;
                    log.error("[SELECTOR] 수신 이벤트 처리 에러 발생 ::: message {}", e.getMessage());
                    log.error("", e);
                }
            }
        });
        eventObserver.start();
    }

    private byte[] getPayload(int readCnt) {
        var bytes = new byte[readCnt];
        // 버퍼 인덱스 위치 초기화
        readBuffer.flip();
        readBuffer.get(bytes);
        return bytes;
    }

    private void monitoring() {
        if (!isAliveChannelManager) {
            isAliveChannelManager = true;
            performEventObserver();
            log.warn("[EVENT OBSERVER] 이상 감지 Regenerate 수행 ::: isAliveSelector {}", isAliveChannelManager);
        }
        try {
            if (!sendChannel.isConnected()) {
                connectSendChannel();
                log.warn("[SEND CHANNEL] Disconnect - Reconnect 수행 ::: host {} , port {}", host, port);
            }
            socketChannelService.sendPing(sendChannel);
        } catch (IOException e) {
            log.error("[SEND CHANNEL] Reconnect 에러 발생 ::: message {}, host {}, port {}", e.getMessage(), host, port);
            log.error("", e);
        }
        try {
            if (!reportChannel.isConnected()) {
                connectReportChannel();
                log.warn("[REPORT CHANNEL] Disconnect - Reconnect 수행 ::: host {} , port {}", host, port);
            }
            socketChannelService.sendPing(reportChannel);
        } catch (IOException e) {
            log.error("[REPORT CHANNEL] Reconnect 에러 발생 ::: message {}, host {}, port {}", e.getMessage(), host, port);
            log.error("", e);
        }
    }

    @PreDestroy
    public void destroy() throws IOException {
        if (anomalyDetectionObserver != null) anomalyDetectionObserver.shutdown();
        isAliveChannelManager = false;
        if (selector != null) selector.close();
        if (sendChannel != null) sendChannel.close();
        if (reportChannel != null) reportChannel.close();
    }

}
