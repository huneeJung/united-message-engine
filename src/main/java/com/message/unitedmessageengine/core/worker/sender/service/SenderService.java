package com.message.unitedmessageengine.core.worker.sender.service;

import com.message.unitedmessageengine.core.worker.sender.repository.SenderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class SenderService {

    private final SenderRepository senderRepository;

    public void send() {

    }
}
