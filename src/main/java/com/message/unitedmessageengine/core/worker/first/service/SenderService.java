package com.message.unitedmessageengine.core.worker.first.service;

import com.google.common.primitives.Bytes;
import com.message.unitedmessageengine.core.socket.manager.first.FirstChannelManager;
import com.message.unitedmessageengine.core.translator.first.FirstImageTranslator;
import com.message.unitedmessageengine.core.translator.first.FirstTranslator;
import com.message.unitedmessageengine.core.worker.first.repository.SenderRepository;
import com.message.unitedmessageengine.core.worker.first.vo.ImageVo;
import com.message.unitedmessageengine.core.worker.first.vo.MMSVo;
import com.message.unitedmessageengine.core.worker.first.vo.SMSVo;
import com.message.unitedmessageengine.entity.MessageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static com.message.unitedmessageengine.constant.FirstConstant.ProtocolType;
import static com.message.unitedmessageengine.constant.FirstConstant.ProtocolType.MMS;
import static com.message.unitedmessageengine.constant.FirstConstant.ProtocolType.SMS;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class SenderService {

    @Qualifier("firstTranslator")
    private final FirstTranslator translator;
    @Qualifier("firstImageTranslator")
    private final FirstImageTranslator fileTranslator;

    private final FirstChannelManager channelManager;
    private final SenderRepository senderRepository;

    @Transactional
    public void findAllMessages(ArrayBlockingQueue<MessageEntity> selectMessageQueue, String serviceDivision, Integer fetchCount) {
        var resultList = senderRepository.findByStatusCodeAndServiceDivision("W", serviceDivision, PageRequest.of(0, fetchCount));
        senderRepository.batchUpdate(resultList);
        selectMessageQueue.addAll(resultList);
    }

    @Transactional
    public void updateAnomalyMessages() {
        var resultList = senderRepository.findByStatusCodeAndResultCodeIsNullAndSendDttBefore("P", LocalDateTime.now().minusHours(1));
        for (MessageEntity message : resultList) {
            message.setStatusCode("W");
        }
    }

    @Transactional
    public void send(Queue<MessageEntity> fetchQueue) {
        while (!fetchQueue.isEmpty()) {
            var messageEntity = fetchQueue.poll();
            try {
                var serviceType = messageEntity.getServiceType();
                var messageVo = serviceType.equals(SMS.name()) ? SMSVo.from(messageEntity) : MMSVo.from(messageEntity);
                var messagePayload = translator.convertToBytes(messageVo);
                if (serviceType.equals(MMS.name())) {
                    var imageList = messageEntity.getImageList();
                    if (imageList == null || imageList.isEmpty()) {
                        log.warn("[SENDER] 발송 실패 ::: MMS 이미지 정보 없음");
                        continue;
                    }
                    for (int i = 0; i < imageList.size(); i++) {
                        var imageDto = ImageVo.from(imageList.get(i));
                        var filePayload = fileTranslator.readFileImage(i + 1, imageDto);
                        messagePayload = Bytes.concat(messagePayload, filePayload);
                    }
                }
                var tcpPayload = translator.addTcpFraming(
                        ProtocolType.valueOf(messageEntity.getServiceType()), messagePayload
                );
                if (tcpPayload.isEmpty()) {
                    log.warn("[SENDER] 내용 전문 NULL");
                    continue;
                }
                channelManager.write(tcpPayload.get());
            } catch (Exception e) {
                messageEntity.setStatusCode("W");
                log.error("[SENDER] 발송 보류 ::: messageId {}", messageEntity.getMessageId());
                log.error("", e);
            }
        }
    }

}
