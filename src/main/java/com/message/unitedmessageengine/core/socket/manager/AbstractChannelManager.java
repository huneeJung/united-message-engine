package com.message.unitedmessageengine.core.socket.manager;

import com.message.unitedmessageengine.core.socket.service.ChannelService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.message.unitedmessageengine.core.socket.manager.AbstractChannelManager.ChannelType.REPORT;
import static com.message.unitedmessageengine.core.socket.manager.AbstractChannelManager.ChannelType.SEND;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractChannelManager<T extends ChannelService> {

    protected final T socketChannelService;
    protected final Set<SocketChannel> sendChannelSet = new HashSet<>();
    protected final Set<SocketChannel> reportChannelSet = new HashSet<>();
    private final ByteBuffer ackBuffer = ByteBuffer.allocate(10 * 1024);
    private final ByteBuffer reportBuffer = ByteBuffer.allocate(10 * 1024);
    protected PriorityBlockingQueue<MainChannel> mainSendChannelQueue;
    protected boolean isAliveChannelManager;
    protected String host;
    protected Integer port;
    protected Integer senderCnt;
    protected Integer reportCnt;
    protected Selector sendSelector;
    protected Selector reportSelector;
    private boolean isAliveSendSelector;
    private boolean isAliveReportSelector;
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
        isAliveSendSelector = true;
        isAliveReportSelector = true;
        mainSendChannelQueue = new PriorityBlockingQueue<>(senderCnt);
        sendSelector = Selector.open();
        reportSelector = Selector.open();
        for (int i = 0; i < senderCnt; i++) tcpConnect(SEND);
        for (int i = 0; i < reportCnt; i++) tcpConnect(REPORT);
        detectSelectorEvent(SEND);
        detectSelectorEvent(REPORT);
        anomalyDetectionObserver = Executors.newSingleThreadScheduledExecutor();
        anomalyDetectionObserver.scheduleAtFixedRate(this::detectAnomaly, pingCycle, pingCycle, TimeUnit.MILLISECONDS);
    }

    protected abstract Queue<String> parsePayload(ByteBuffer buffer, byte[] data);

    protected void tcpConnect(ChannelType type) {
        try {
            // SEND TCP CONNECT
            var channel = SocketChannel.open();
            channel.socket().setSoTimeout(readTimeout);
            channel.socket().setSendBufferSize(1024 * 1024);
            channel.socket().setReceiveBufferSize(1024 * 1024);
            channel.socket().connect(new InetSocketAddress(host, port), connectTimeout);
            channel.configureBlocking(false);
            socketChannelService.authenticate(type, channel);
            if (type.equals(SEND)) {
                channel.register(sendSelector, SelectionKey.OP_READ);
                mainSendChannelQueue.add(MainChannel.create(channel));
                sendChannelSet.add(channel);
            } else if (type.equals(REPORT)) {
                channel.register(reportSelector, SelectionKey.OP_READ);
                reportChannelSet.add(channel);
            }
        } catch (IOException e) {
            log.error("[SOCKET CHANNEL] Connect 에러 발생 ::: message {}, host {}, port {}", e.getMessage(), host, port);
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

    // 네트워크 IO 속도보다 CPU 처리 속도가 빠르므로, 비동기로 처리할 이유가 없음
    // 하지만 부하가 크지 않은 선에서 가상 쓰레드로써 비동기로 write 처리해주면 소켓 채널의 일정량의 버퍼 데이터로 계속해서 유지할 수 있게 됨
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(byte[] payload) throws Exception {
        while (true) {
            if (mainSendChannelQueue.isEmpty()) continue;
            var mainSendChannel = mainSendChannelQueue.poll();
            var sendChannel = mainSendChannel.getSocketChannel();
            if (!sendChannel.isConnected()) {
                throw new Exception("[MAIN CHANNEL] 연결 끊김");
            }
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                executor.submit(() -> {
                    try {
                        var sendBuffer = ByteBuffer.wrap(payload);
                        var cnt = 0;
                        while (cnt < payload.length) cnt += sendChannel.write(sendBuffer);
                        mainSendChannel.setLastUsedTime(Instant.now());
                        mainSendChannelQueue.add(mainSendChannel);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            return;
        }
    }

    private void detectSelectorEvent(ChannelType type) {
        var sendChannelEventObserver = new Thread(() -> {
            var isAliveSelector = type.equals(SEND) ? isAliveSendSelector : isAliveReportSelector;
            var selector = type.equals(SEND) ? sendSelector : reportSelector;
            var channelSet = type.equals(SEND) ? sendChannelSet : reportChannelSet;
            while (isAliveSelector) {
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
                                if (type.equals(SEND)) {
                                    channel.read(ackBuffer);
                                    socketChannelService.processSendResponse(channel, getAckPayload());
                                } else {
                                    channel.read(reportBuffer);
                                    socketChannelService.processReportResponse(channel, getReportPayload());
                                }
                            } catch (IOException e) {
                                channelSet.remove(channel);
                                key.cancel();
                                channel.close();
                                log.warn("[{} Channel] Read Event 처리중 발생 ::: message {}", type, e.getMessage());
                                log.warn("", e);
                            }
                        }
                    }
                } catch (ClosedSelectorException e) {
                    isAliveSelector = false;
                    log.info("[{} SELECTOR] 종료 작업 수행", type);
                } catch (Exception e) {
                    isAliveSelector = false;
                    log.error("[{} SELECTOR] 수신 이벤트 처리 에러 발생 ::: message {}", type, e.getMessage());
                    log.error("", e);
                }
            }
        }
        );
        sendChannelEventObserver.start();
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

    private void detectAnomaly() {
        if (!isAliveSendSelector) {
            log.warn("[SEND SELECTOR] 이상 감지 Regenerate 수행 ::: isAliveSelector {}", isAliveSendSelector);
            isAliveSendSelector = true;
            detectSelectorEvent(SEND);
        }
        if (!isAliveReportSelector) {
            log.warn("[REPORT SELECTOR] 이상 감지 Regenerate 수행 ::: isAliveSelector {}", isAliveReportSelector);
            isAliveReportSelector = true;
            detectSelectorEvent(REPORT);
        }
        if (sendChannelSet.size() != senderCnt || reportChannelSet.size() != reportCnt) {
            while (sendChannelSet.size() < senderCnt) {
                tcpConnect(SEND);
                log.warn("[SEND CHANNEL] Reconnect 수행 ::: host {} , port {}", host, port);
            }
            while (reportChannelSet.size() < reportCnt) {
                tcpConnect(REPORT);
                log.warn("[REPORT CHANNEL] Reconnect 수행 ::: host {} , port {}", host, port);
            }
        } else {
            sendChannelSet.forEach(socketChannelService::writePing);
            reportChannelSet.forEach(socketChannelService::writePing);
        }
    }

    @PreDestroy
    public void destroy() throws IOException {
        if (anomalyDetectionObserver != null) anomalyDetectionObserver.shutdown();
        isAliveChannelManager = false;
        isAliveSendSelector = false;
        isAliveReportSelector = false;
        if (sendSelector != null) sendSelector.close();
        if (reportSelector != null) reportSelector.close();
        for (SocketChannel sendChannel : sendChannelSet) if (sendChannel != null) sendChannel.close();
        for (SocketChannel reportChannel : reportChannelSet) if (reportChannel != null) reportChannel.close();
    }

    public enum ChannelType {
        SEND, REPORT
    }

}
