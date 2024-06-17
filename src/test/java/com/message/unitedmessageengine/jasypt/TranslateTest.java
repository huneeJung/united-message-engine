package com.message.unitedmessageengine.jasypt;

import com.message.unitedmessageengine.core.socket.vo.FirstConnectVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

import static com.message.unitedmessageengine.core.socket.constant.ProtocolConstant.First.CHARSET;

@SpringJUnitConfig
class TranslateTest {

    @Test
    void test() {
        String expected = "USERNAME:USERNAME2\r\nPASSWORD:PASSWORD2\r\nLINE:LINE2\r\nVERSION:VERSION2\r\n";
        var obj = FirstConnectVo.builder()
                .LINE("LINE2")
                .USERNAME("USERNAME2")
                .PASSWORD("PASSWORD2")
                .VERSION("VERSION2")
                .build();
        var converted = new String(convertToByteArray(obj));
        Assert.isTrue(expected.equals(converted), "SUCCESS");
    }

    private byte[] convertToByteArray(Object obj) {
        StringBuilder sb = new StringBuilder();

        // 리플렉션을 사용하여 객체의 필드와 값을 맵에 저장
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(obj.getClass(), field.getName());
            if (pd != null) {
                BeanWrapper beanWrapper = new BeanWrapperImpl(obj);
                Object value = beanWrapper.getPropertyValue(pd.getName());
                sb.append(field.getName()).append(":").append(value).append("\r\n");
            }
        }
        return sb.toString().getBytes(CHARSET);
    }

}
