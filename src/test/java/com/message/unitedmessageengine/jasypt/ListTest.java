package com.message.unitedmessageengine.jasypt;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

@SpringJUnitConfig
public class ListTest {

    @Test
    public void test() {
        Set<Integer> numList = new HashSet<>();
        numList.add(1);
        numList.add(2);
        numList.add(3);

        var iterator = numList.iterator();
        while (iterator.hasNext()) {
            var num = iterator.next();
//            numList.add(4);
            if (num == 2) {
                iterator.remove();
            }
        }
        Assert.isTrue(numList.contains(1), "1 성공");
        Assert.isTrue(!numList.contains(2), "2 성공");
        Assert.isTrue(numList.contains(3), "3 성공");
        Assert.isTrue(numList.contains(4), "4 성공");
    }

}
