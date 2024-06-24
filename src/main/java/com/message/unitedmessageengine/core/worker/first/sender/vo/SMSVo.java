package com.message.unitedmessageengine.core.worker.first.sender.vo;

import com.message.unitedmessageengine.entity.MessageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.message.unitedmessageengine.constant.ProtocolConstant.First.CONVERT_TYPE;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class SMSVo {

    private final String TYPE;
    private final String KEY;
    private final String PHONE;
    private final String CALLBACK;
    private final String MESSAGE;

    public static SMSVo from(MessageEntity messageEntity) {
        return SMSVo.builder()
                .TYPE(CONVERT_TYPE.get(messageEntity.getServiceType()))
                .KEY(messageEntity.getMessageId())
                .PHONE(messageEntity.getToNumber())
                .CALLBACK(messageEntity.getFromNumber())
                .MESSAGE(messageEntity.getContent())
                .build();
    }

}
