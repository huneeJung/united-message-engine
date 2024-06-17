package com.message.unitedmessageengine.core.queue;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "queue")
public class QueueProperties {
    private int limitFetchSize;
    private int limitMessageSize;
    private int limitKakaoSize;
    private int limitAckSize;
    private int limitResultSize;
}
