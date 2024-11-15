package com.message.unitedmessageengine.core.translator;

import com.message.unitedmessageengine.constant.FirstConstant.ProtocolType;

public interface Translator {

    byte[] addTcpFraming(ProtocolType type, byte[] element);

}
