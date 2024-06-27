package com.message.unitedmessageengine.core.worker.first.vo;

import com.message.unitedmessageengine.entity.MessageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.message.unitedmessageengine.constant.FirstConstant.CONVERT_TYPE;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class MMSVo {

    private final String TYPE;
    private final String KEY;
    private final String PHONE;
    private final String CALLBACK;
    private final String SUBJECT;
    private final String MESSAGE;

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
