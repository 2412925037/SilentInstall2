package com.invisible.silentinstall.utils;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Properties;

public class Util_Json {

    /**
     * 检测json对象是否合法
     *
     * @param json
     * @param keys 是否包含相关key
     * @return
     */
    public static boolean checkJson(JSONObject json, String... keys) {
        boolean isValid = true;
        if (json == null) {
            isValid = false;

        } else
            for (String key : keys) {
                if (json.isNull(key)) {
                    Util_Log.log("checkJosn,but has no " + key);
                    isValid = false;
                    break;
                }
            }
        return isValid;
    }

    public static boolean checkJson(String json, String... keys) {
        return checkJson(getJo(json), keys);
    }

    /**
     * @param jsonAd
     * @param parameter
     * @param initValue
     * @return 如果 json中包含 参数就返回参数，否则返回initValue
     */
    public static String getJsonParameter(JSONObject jsonAd, String parameter,
                                          String initValue) {
        if (jsonAd == null)
            return initValue;
        String returnValue = "-1";
        try {
            if (jsonAd.has(parameter)) {
                if (!jsonAd.getString(parameter).equals("")) {
                    returnValue = jsonAd.getString(parameter);
                } else {
                    returnValue = initValue;
                }
            } else {
                returnValue = initValue;
            }
        } catch (Exception ex1) {
            returnValue = initValue;
        }
        return returnValue;
    }

    /**
     * @param jsonStr
     * @return
     * @date 2014-7-4
     * @des 获取一个json对象。（为了避免try catch块的复杂性）
     */
    public static JSONObject getJo(String jsonStr) {
        if (jsonStr == null) return null;
        JSONObject j = null;
        try {
            j = new JSONObject(jsonStr);
        } catch (Exception e) {
            // if (Util_Log.logShow)
            // e.printStackTrace();
        }
        return j;
    }

    /**
     * 将json转成pty文件，并返回
     */
    public static Properties json2pty(JSONObject json) {
        Properties pty = new Properties();
        try {
            Iterator<String> its = json.keys();
            while (its.hasNext()) {
                String key = its.next();
                pty.put(key, json.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pty;
    }
}
