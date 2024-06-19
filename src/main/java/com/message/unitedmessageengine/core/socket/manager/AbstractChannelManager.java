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
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractChannelManager<T extends ChannelService> {

    protected final Set<SocketChannel> sendChannelList = new HashSet<>();
    protected final Set<SocketChannel> reportChannelList = new HashSet<>();

    protected final T socketChannelService;
    private final ByteBuffer ackBuffer = ByteBuffer.allocate(10 * 1024);
    private final ByteBuffer reportBuffer = ByteBuffer.allocate(10 * 1024);

    protected boolean isAliveChannelManager;
    protected String host;
    protected Integer port;
    protected Integer senderCnt;
    protected Integer reportCnt;

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
        for (int i = 0; i < senderCnt; i++) connectSendChannel();
        for (int i = 0; i < reportCnt; i++) connectReportChannel();
        performEventObserver();
        anomalyDetectionObserver = Executors.newSingleThreadScheduledExecutor();
        anomalyDetectionObserver.scheduleAtFixedRate(this::monitoring, pingCycle, pingCycle, TimeUnit.MILLISECONDS);
    }

    protected abstract void connectSendChannel();

    protected abstract void connectReportChannel();

    protected abstract Queue<String> parsePayload(ByteBuffer buffer, byte[] data);

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
                        keyIterator.remove();
                        if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            try {
                                // SEND 수신
                                if (sendChannelList.contains(channel)) {
                                    var readCnt = channel.read(ackBuffer);
                                    if (readCnt <= 0) log.info("[SEND CHANNEL] 이벤트 처리 데이터 없음");
                                    socketChannelService.processSendResponse(getAckPayload());
                                }
                                // REPORT 수신
                                if (reportChannelList.contains(channel)) {
                                    var readCnt = channel.read(reportBuffer);
                                    if (readCnt <= 0) log.info("[REPORT CHANNEL] 이벤트 처리 데이터 없음");
                                    socketChannelService.processReportResponse(channel, getReportPayload());
                                }
                            } catch (IOException e) {
                                key.cancel();
                                channel.close();
                                throw new IOException(e);
                            }
                        }
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

    private Queue<String> getAckPayload() {
        // 버퍼 인덱스 위치 초기화
        ackBuffer.flip();
        var bytes = new byte[ackBuffer.remaining()];
        ackBuffer.get(bytes);
        return parsePayload(ackBuffer, bytes);
    }

    private Queue<String> getReportPayload() {
        // 버퍼 인덱스 위치 초기화
        reportBuffer.flip();
        var bytes = new byte[reportBuffer.remaining()];
        reportBuffer.get(bytes);
        return parsePayload(reportBuffer, bytes);
    }

    private void monitoring() {
        if (!isAliveChannelManager) {
            isAliveChannelManager = true;
            performEventObserver();
            log.warn("[EVENT OBSERVER] 이상 감지 Regenerate 수행 ::: isAliveSelector {}", isAliveChannelManager);
        }
        try {
            var iteratorSendChannel = sendChannelList.iterator();
            while (iteratorSendChannel.hasNext()) {
                var sendChannel = iteratorSendChannel.next();
                if (!sendChannel.isConnected()) {
                    iteratorSendChannel.remove();
                    log.warn("[SEND CHANNEL] Disconnect 감지 ::: host {} , port {}", host, port);
                } else {
                    socketChannelService.sendPing(sendChannel);
                }
            }
            while (sendChannelList.size() < senderCnt) {
                connectSendChannel();
                log.warn("[SEND CHANNEL] Reconnect 수행 ::: host {} , port {}", host, port);
            }
        } catch (IOException e) {
            log.error("[SEND CHANNEL] Reconnect 에러 발생 ::: message {}, host {}, port {}", e.getMessage(), host, port);
            log.error("", e);
        }
        try {
            var iteratorReportChannel = reportChannelList.iterator();
            while (iteratorReportChannel.hasNext()) {
                var reportChannel = iteratorReportChannel.next();
                if (!reportChannel.isConnected()) {
                    iteratorReportChannel.remove();
                    log.warn("[REPORT CHANNEL] Disconnect 감지 ::: host {} , port {}", host, port);
                } else {
                    socketChannelService.sendPing(reportChannel);
                }
            }
            while (reportChannelList.size() < reportCnt) {
                connectReportChannel();
                log.warn("[REPORT CHANNEL] Reconnect 수행 ::: host {} , port {}", host, port);
            }
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
        for (SocketChannel sendChannel : sendChannelList) if (sendChannel != null) sendChannel.close();
        for (SocketChannel reportChannel : reportChannelList) if (reportChannel != null) reportChannel.close();
    }

}
