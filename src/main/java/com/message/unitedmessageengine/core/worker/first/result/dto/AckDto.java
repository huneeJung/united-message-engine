package com.message.unitedmessageengine.core.worker.first.result.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AckDto {

    private String messageId;
    private String resultCode;
    private String resultMessage;

}
