package com.zzming.xsapi.util;

import com.google.gson.Gson;

public class GsonUtil {

    public static String createJson(Object object){
        return new Gson().toJson(object);
    }

}
