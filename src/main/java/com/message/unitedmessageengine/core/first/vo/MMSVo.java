package com.message.unitedmessageengine.core.first.vo;

import com.message.unitedmessageengine.entity.MessageEntity;
import lombok.Builder;

import static com.message.unitedmessageengine.constant.FirstConstant.CONVERT_TYPE;

@Builder
public record MMSVo(String TYPE, String KEY, String PHONE, String CALLBACK, String SUBJECT, String MESSAGE) {

    public static MMSVo from(MessageEntity messageEntity) {
        return MMSVo.builder()
                .TYPE(CONVERT_TYPE.get(messageEntity.getServiceType()))
                .KEY(messageEntity.getMessageId())
                .PHONE(messageEntity.getToNumber())
                .CALLBACK(messageEntity.getFromNumber())
                .SUBJECT(messageEntity.getTitle())
                .MESSAGE(messageEntity.getContent())
                .build();
    }

}
