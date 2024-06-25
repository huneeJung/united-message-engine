package com.message.unitedmessageengine.core.translator;

import com.message.unitedmessageengine.constant.FirstConstant.ProtocolType;

import java.util.Optional;

public interface Translator {

    Optional<byte[]> addTcpFraming(ProtocolType type, byte[] element);

}
