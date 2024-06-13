package com.message.unitedmessageengine.core.translater.service;

import java.util.Map;

public interface TranslateService {

    Map<String, Object> translateToExternalProtocol(Map<String, Object> element);

    Map<String, Object> translateToInternalProtocol(Map<String, Object> element);

}
