package com.message.unitedmessageengine.core.translator;

import com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.ProtocolType;

import java.util.Optional;

public interface Translator {

    Object translateToInternalProtocol(ProtocolType type, Object element);

    Optional<byte[]> translateToExternalProtocol(ProtocolType type, Object element);

}
