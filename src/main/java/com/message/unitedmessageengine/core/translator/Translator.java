package com.message.unitedmessageengine.core.translator;

import com.message.unitedmessageengine.constant.ProtocolConstant.ProtocolType;

import java.util.Optional;

public interface Translator {

    Object translateToInternalProtocol(ProtocolType type, Object element);

    Optional<byte[]> addTcpFraming(ProtocolType type, byte[] element);

}
