package com.message.unitedmessageengine.core.translater;

import com.message.unitedmessageengine.core.translater.service.TranslateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslateRouter {

    @Qualifier("A")
    private final TranslateService destinationAService;

    @Qualifier("B")
    private final TranslateService destinationBService;

    public Map<String, Object> translate(Map<String, Object> element) {
        var route = element.get("route");

        if (route.equals("DESTINATION_A")) {
            return destinationAService.translateToExternalProtocol(element);
        } else if (route.equals("DESTINATION_B")) {
            return destinationBService.translateToExternalProtocol(element);
        }
        // TODO 처리 로직 수정
        log.warn("[지원하지않는 라우팅 경로 감지] ::: route {}", route);
        throw new RuntimeException("지원히지 않는 라우팅 경로입니다");
    }

}
