package com.message.unitedmessageengine.core.worker.first.sender.service;

import com.google.common.primitives.Bytes;
import com.message.unitedmessageengine.core.socket.manager.first.FirstChannelManager;
import com.message.unitedmessageengine.core.translator.first.FirstImageTranslator;
import com.message.unitedmessageengine.core.translator.first.FirstTranslator;
import com.message.unitedmessageengine.core.worker.first.sender.repository.SenderRepository;
import com.message.unitedmessageengine.core.worker.first.sender.vo.ImageVo;
import com.message.unitedmessageengine.core.worker.first.sender.vo.MMSVo;
import com.message.unitedmessageengine.core.worker.first.sender.vo.SMSVo;
import com.message.unitedmessageengine.entity.MessageEntity;
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

import static com.message.unitedmessageengine.constant.ProtocolConstant.ProtocolType;
import static com.message.unitedmessageengine.constant.ProtocolConstant.ProtocolType.MMS;
import static com.message.unitedmessageengine.constant.ProtocolConstant.ProtocolType.SMS;

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

    public List<MessageEntity> findAllMessages(String serviceDivision, Integer fetchCount) {
        return senderRepository.findByStatusCodeAndServiceDivision("W", serviceDivision, PageRequest.of(0, fetchCount));
    }

    @Transactional
    public void send(List<MessageEntity> fetchList) {
        senderRepository.batchUpdate(fetchList);

        var sendChannelList = channelManager.getSendChannelList();
        if (sendChannelList.isEmpty()) {
            log.warn("[SENDER] 발송 가능 채널 없음");
            throw new RuntimeException("[SENDER] 발송 가능 채널 없음");
        }

        var distributeCnt = fetchList.size() / sendChannelList.size();

        var cnt = 0;
        var index = 0;
        var sendChannel = sendChannelList.get(index);
        for (MessageEntity messageEntity : fetchList) {
            try {
                cnt++;
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
                // TODO :
                if (tcpPayload.isEmpty()) continue;
                var messageBuffer = ByteBuffer.wrap(tcpPayload.get());
                log.info("length : {}", tcpPayload.get().length);
                sendChannel.write(messageBuffer);
                if (cnt > distributeCnt) {
                    index = Math.min(index + 1, sendChannelList.size() - 1);
                    sendChannel = sendChannelList.get(index);
                    cnt = 0;
                }
            } catch (IOException e) {
                log.error("[SENDER] 발송 실패 ::: messageId {}", messageEntity.getMessageId());
                log.error("", e);
                continue;
            }
//            log.info("[SENDER] 발송 완료 ::: messageId {}", messageEntity.getMessageId());
        }
    }
}
