package com.message.unitedmessageengine.core.worker.first.sender.vo;

import com.message.unitedmessageengine.entity.ImageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ImageVo {

    private final String fileName;
    private final String filePath;

    public static ImageVo from(ImageEntity imageEntity) {
        return ImageVo.builder()
                .fileName(imageEntity.getImageName())
                .filePath(imageEntity.getImagePath())
                .build();
    }

}
