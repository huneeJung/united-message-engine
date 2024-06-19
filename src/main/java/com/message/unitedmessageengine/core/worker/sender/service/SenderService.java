package com.message.unitedmessageengine.core.worker.sender.service;

import com.message.unitedmessageengine.core.socket.manager.first.FirstChannelManager;
import com.message.unitedmessageengine.core.translator.first.FirstTranslator;
import com.message.unitedmessageengine.core.worker.sender.dto.ExternalMessageDto;
import com.message.unitedmessageengine.core.worker.sender.repository.SenderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.ProtocolType;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class SenderService {

    @Qualifier("firstTranslator")
    private final FirstTranslator translator;
    private final FirstChannelManager channelManager;

    private final SenderRepository senderRepository;

    public List<ExternalMessageDto> findAllMessages(String serviceDivision, Integer fetchCount) {
        return senderRepository.findAllMessages(serviceDivision, PageRequest.of(0, fetchCount));
    }

    @Transactional
    public void send(List<ExternalMessageDto> fetchList) throws IOException {
        senderRepository.batchUpdate(fetchList);

        var channelList = channelManager.getSendChannelList();
        if (channelList.isEmpty()) {
            log.warn("[SENDER] 발송 가능 채널 없음");
            throw new RuntimeException("[SENDER] 발송 가능 채널 없음");
        }

        var distributeCnt = fetchList.size() / channelList.size();

        var cnt = 0;
        var index = 0;
        var channel = channelList.get(index);
        for (ExternalMessageDto messageDto : fetchList) {
            try {
                cnt++;
                var messagePayload = translator.translateToExternalProtocol(
                        ProtocolType.valueOf(messageDto.getTYPE()), messageDto
                );
                // TODO :
                if (messagePayload.isEmpty()) continue;
                var messageBuffer = ByteBuffer.wrap(messagePayload.get());
                channel.write(messageBuffer);
                if (cnt > distributeCnt) {
                    index = Math.min(index + 1, channelList.size() - 1);
                    channel = channelList.get(index);
                    cnt = 0;
                }
            } catch (IOException e) {
                log.error("[SENDER] 발송 실패 ::: messageId {}", messageDto.getKEY());
                log.error("", e);
                continue;
            }
//            log.info("[MAIN SEND CHANNEL] 발송 완료 ::: messageId {}", messageDto.getKEY());
        }
    }
}
