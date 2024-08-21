package com.message.unitedmessageengine.core.first.vo;

import com.message.unitedmessageengine.entity.MessageImageEntity;
import lombok.Builder;

@Builder
public record ImageVo(Long imageId, String fileName, String filePath) {

    public static ImageVo from(MessageImageEntity messageImageEntity) {
        return ImageVo.builder()
                .imageId(messageImageEntity.getImageId())
                .fileName(messageImageEntity.getImageName())
                .filePath(messageImageEntity.getImagePath())
                .build();
    }

}
