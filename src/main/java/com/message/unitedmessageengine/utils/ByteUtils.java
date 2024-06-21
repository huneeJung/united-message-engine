package com.message.unitedmessageengine.utils;

public class ByteUtils {

    public static byte[] mergeByteArrays(byte[] byteArr1, byte[] byteArr2) {
        byte[] result = new byte[byteArr1.length + byteArr2.length];
        System.arraycopy(byteArr1, 0, result, 0, byteArr1.length);
        System.arraycopy(byteArr2, 0, result, byteArr1.length, byteArr2.length);
        return result;
    }

}
