package com.message.unitedmessageengine.core.translator.first;

import com.google.common.cache.Cache;
import com.message.unitedmessageengine.core.worker.first.sender.vo.ImageVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

import static com.message.unitedmessageengine.constant.FirstConstant.PROTOCOL_DELIMITER;

@Slf4j
@Component
@RequiredArgsConstructor
@Qualifier("firstImageTranslator")
public class FirstImageTranslator {

    private static final Integer IMAGE_DATA_PART_LIMIT = 256;

    private final Cache<Long, byte[]> imageCache;

    public byte[] readFileImage(Integer index, ImageVo imageVo) {
        var imageId = imageVo.getImageId();
        byte[] result = imageCache.getIfPresent(imageId);
        if (result != null) return result;

        StringBuilder sb = new StringBuilder();
        sb.append("FILENAME").append(index).append(":").append(imageVo.getFileName()).append(PROTOCOL_DELIMITER);
        try (var br = new BufferedInputStream(new FileInputStream(imageVo.getFilePath()))) {
            var fileBytes = Base64.getEncoder().encode(br.readAllBytes());
            var prefix = "FILE" + index + ":";
            for (int i = 0; i < fileBytes.length; i += IMAGE_DATA_PART_LIMIT) {
                var endIndex = Math.min(fileBytes.length, i + IMAGE_DATA_PART_LIMIT);
                var chunkBytes = new byte[endIndex - i];
                System.arraycopy(fileBytes, i, chunkBytes, 0, chunkBytes.length);
                sb.append(prefix).append(new String(chunkBytes)).append("\r\n");
            }
            result = sb.toString().getBytes();
            imageCache.put(imageId, result);
            return result;
        } catch (IOException e) {
            log.error("[FILE UTILS] File Read 실패 ::: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
