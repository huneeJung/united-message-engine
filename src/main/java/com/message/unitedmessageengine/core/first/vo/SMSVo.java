package com.message.unitedmessageengine.core.first.vo;

import com.message.unitedmessageengine.entity.MessageEntity;
import lombok.Builder;

import static com.message.unitedmessageengine.constant.FirstConstant.CONVERT_TYPE;

@Builder
public record SMSVo(String TYPE, String KEY, String PHONE, String CALLBACK, String MESSAGE) {

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
