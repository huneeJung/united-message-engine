package com.message.unitedmessageengine.core.first.service;

import com.google.common.primitives.Bytes;
import com.message.unitedmessageengine.core.first.repository.MessageRepository;
import com.message.unitedmessageengine.core.first.vo.ImageVo;
import com.message.unitedmessageengine.core.first.vo.MMSVo;
import com.message.unitedmessageengine.core.first.vo.SMSVo;
import com.message.unitedmessageengine.core.socket.manager.ChannelManager;
import com.message.unitedmessageengine.core.translator.first.FirstImageTranslator;
import com.message.unitedmessageengine.core.translator.first.FirstTranslator;
import com.message.unitedmessageengine.entity.MessageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    @Qualifier("firstChannelManager")
    private final ChannelManager<?> channelManager;
    private final MessageRepository messageRepository;

    @Transactional
    public List<MessageEntity> findAllMessages(String serviceDivision, Integer fetchCount) {
        // TODO 메시지 상태를 특정 건수를 들고와서 메시지 상태를 업데이트하고 발송하게되면, 발송중 서버가 꺼졌을때 발송이 안되는 문제가 생길 수 있음
        var messageList = messageRepository.findByStatusCodeAndServiceDivisionOrderBySendDttAsc("W", serviceDivision, PageRequest.of(0, fetchCount));
        messageRepository.batchUpdate(messageList);
        return messageList;
    }

    @Transactional
    public void messageSend(MessageEntity message) {
        var serviceType = message.getServiceType();
        var messageVo = serviceType.equals(SMS.name()) ? SMSVo.from(message) : MMSVo.from(message);
        var messagePayload = translator.convertToBytes(messageVo);
        if (serviceType.equals(MMS.name())) {
            var imageList = message.getImageList();
            // TODO MMS 이미지 없을 시 실패 처리
            if (imageList == null || imageList.isEmpty()) {
            }
            for (int i = 0; i < imageList.size(); i++) {
                var imageDto = ImageVo.from(imageList.get(i));
                var filePayload = fileTranslator.readFileImage(i + 1, imageDto);
                messagePayload = Bytes.concat(messagePayload, filePayload);
            }
        }
        var tcpPayload = translator.addTcpFraming(ProtocolType.valueOf(message.getServiceType()), messagePayload);
        try {
            channelManager.write(tcpPayload);
        } catch (Exception e) {
            message.setStatusCode("W");
            messageRepository.save(message);
            log.error("발송 보류 ::: messageId {}", message.getMessageId());
            log.error("", e);
        }
    }

}
