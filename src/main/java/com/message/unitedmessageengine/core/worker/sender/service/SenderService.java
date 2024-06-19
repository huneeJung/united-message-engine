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
    public void send(List<ExternalMessageDto> fetchList) {
        senderRepository.batchUpdate(fetchList);

        var channel = channelManager.getMainSendChannel();
        for (ExternalMessageDto messageDto : fetchList) {
            var messagePayload = translator.translateToExternalProtocol(
                    ProtocolType.valueOf(messageDto.getTYPE()), messageDto
            );
            if (messagePayload.isEmpty()) return;
            var messageBuffer = ByteBuffer.wrap(messagePayload.get());
            try {
                channel.write(messageBuffer);
            } catch (IOException e) {
                log.info("[MAIN SEND CHANNEL] 발송 실패 ::: messageId {}", messageDto.getKEY());
                continue;
            }
            log.info("[MAIN SEND CHANNEL] 발송 완료 ::: messageId {}", messageDto.getKEY());
        }
    }
}
