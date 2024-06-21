package com.message.unitedmessageengine.core.translator.first;

import com.message.unitedmessageengine.core.worker.first.sender.vo.ImageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Component
public class FirstFileTranslator {

    private final Integer chunkSizeLimit = 256;

    public byte[] readFileImage(Integer index, ImageVo imageVo) {
        StringBuilder sb = new StringBuilder();
        sb.append("FILENAME").append(index).append(":").append(imageVo.getFileName()).append("\r\n");
        try (var br = new BufferedInputStream(new FileInputStream(imageVo.getFilePath()))) {
            var fileBytes = Base64.getEncoder().encode(br.readAllBytes());

            var prefix = "FILE" + index + ":";
            for (int i = 0; i < fileBytes.length; i += chunkSizeLimit) {
                var endIndex = Math.min(fileBytes.length, i + chunkSizeLimit);
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
