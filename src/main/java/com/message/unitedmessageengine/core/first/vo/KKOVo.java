package com.message.unitedmessageengine.core.first.vo;

import com.message.unitedmessageengine.entity.KakaoEntity;
import lombok.Builder;

import static com.message.unitedmessageengine.constant.FirstConstant.CONVERT_TYPE;

@Builder
public record KKOVo(String TYPE, String KEY, String PHONE, String CALLBACK, String SENDERKEY, String TMPCODE,
                    String ADV, String BUTTON, String SUBJECT, String MESSAGE, String TITLE) {

    public static KKOVo from(KakaoEntity kakaoEntity) {
        return KKOVo.builder()
                .TYPE(CONVERT_TYPE.get(kakaoEntity.getServiceType()))
                .KEY(kakaoEntity.getKakaoId())
                .PHONE(kakaoEntity.getToNumber())
                .CALLBACK(kakaoEntity.getFromNumber())
                .SENDERKEY(kakaoEntity.getSenderKey())
                .TMPCODE(kakaoEntity.getTmpCode())
                .ADV(kakaoEntity.getAdv())
                .BUTTON(kakaoEntity.getButton())
                .SUBJECT(kakaoEntity.getSubject())
                .MESSAGE(kakaoEntity.getContent())
                .TITLE(kakaoEntity.getTitle())
                .build();
    }

}
