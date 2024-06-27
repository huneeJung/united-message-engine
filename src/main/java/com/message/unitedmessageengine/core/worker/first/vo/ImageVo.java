package com.message.unitedmessageengine.core.worker.first.vo;

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

    private final Long imageId;
    private final String fileName;
    private final String filePath;

    public static ImageVo from(ImageEntity imageEntity) {
        return ImageVo.builder()
                .imageId(imageEntity.getImageId())
                .fileName(imageEntity.getImageName())
                .filePath(imageEntity.getImagePath())
                .build();
    }

}
