package com.message.unitedmessageengine.core.translater.service;

import com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.ProtocolType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.AgentA.CHARSET;

public interface TranslateService {

    static byte[] convertToByteArray(Object obj) {
        StringBuilder sb = new StringBuilder();

        // 리플렉션을 사용하여 객체의 필드와 값을 맵에 저장
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(obj.getClass(), field.getName());
                if (pd != null) {
                    BeanWrapper beanWrapper = new BeanWrapperImpl(obj);
                    Object value = beanWrapper.getPropertyValue(pd.getName());
                    sb.append(field.getName()).append(":").append(value).append("\r\n");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return sb.toString().getBytes(CHARSET);
    }

    byte[] translateToExternalProtocol(ProtocolType type, Object element);
}
