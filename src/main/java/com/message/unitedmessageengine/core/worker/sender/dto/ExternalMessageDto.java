package com.message.unitedmessageengine.core.worker.sender.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalMessageDto {

    private String TYPE;
    private String KEY;
    private String PHONE;
    private String CALLBACK;
    private String MESSAGE;

}
