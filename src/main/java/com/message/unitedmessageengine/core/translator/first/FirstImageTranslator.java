package com.message.unitedmessageengine.core.translator.first;

import com.message.unitedmessageengine.constant.ProtocolConstant;
import com.message.unitedmessageengine.core.worker.first.sender.vo.ImageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import static com.message.unitedmessageengine.constant.ProtocolConstant.First.PROTOCOL_DELIMITER;

@Slf4j
@Component
@Qualifier("firstImageTranslator")
public class FirstImageTranslator {

    private static final Integer IMAGE_DATA_PART_LIMIT = 256;

    public byte[] readFileImage(Integer index, ImageVo imageVo) {
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
            return sb.toString().getBytes();
        } catch (IOException e) {
            log.error("[FILE UTILS] File Read 실패 ::: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
