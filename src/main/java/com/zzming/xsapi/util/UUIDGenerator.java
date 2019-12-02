package com.zzming.xsapi.util;

import java.util.UUID;

public class UUIDGenerator {

    /**
     *      * 获取32位UUID字符串
     *      * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     *      * 获取32位UUID大写字符串
     *      * @return
     *     
     */
    public static String getUpperCaseUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }


    public static void main(String[] args) {
        System.out.println(UUIDGenerator.getUUID());
    }

}
