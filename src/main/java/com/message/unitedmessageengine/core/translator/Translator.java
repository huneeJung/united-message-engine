package com.message.unitedmessageengine.core.translator;

import com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.ProtocolType;

import java.io.IOException;
import java.util.Optional;

public interface Translator {

    Optional<byte[]> translateToExternalProtocol(ProtocolType type, Object element) throws IOException;

    Optional<byte[]> translateToInternalProtocol(ProtocolType type, Object element) throws IOException;

}
