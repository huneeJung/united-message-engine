package com.message.unitedmessageengine.core.translator;

import com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.ProtocolType;
import com.message.unitedmessageengine.core.worker.result.dto.ResultDto;

import java.io.IOException;
import java.util.Optional;

public interface Translator {

    ResultDto translateToInternalProtocol(ProtocolType type, Object element);

    Optional<byte[]> translateToExternalProtocol(ProtocolType type, Object element) throws IOException;

}
