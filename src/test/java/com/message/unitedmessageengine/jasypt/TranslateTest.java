package com.message.unitedmessageengine.jasypt;

import com.message.unitedmessageengine.core.socket.vo.ConnectA;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.Assert;

import static com.message.unitedmessageengine.core.translater.service.TranslateService.convertToByteArray;

@SpringJUnitConfig
public class TranslateTest {

    @Test
    void test() {
        String expected = "USERNAME:USERNAME2\r\nPASSWORD:PASSWORD2\r\nLINE:LINE2\r\nVERSION:VERSION2\r\n";
        var obj = ConnectA.builder()
                .LINE("LINE2")
                .USERNAME("USERNAME2")
                .PASSWORD("PASSWORD2")
                .VERSION("VERSION2")
                .build();
        var converted = new String(convertToByteArray(obj));
        Assert.isTrue(expected.equals(converted), "SUCCESS");
    }

}
