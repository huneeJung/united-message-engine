package com.message.unitedmessageengine.core.translater.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Qualifier("B")
public class TranslateServiceB implements TranslateService {
    @Override
    public Map<String, Object> translateToExternalProtocol(Map<String, Object> element) {
        return null;
    }

    @Override
    public Map<String, Object> translateToInternalProtocol(Map<String, Object> element) {
        return null;
    }
}
