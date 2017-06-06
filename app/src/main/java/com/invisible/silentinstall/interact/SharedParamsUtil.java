package com.invisible.silentinstall.interact;

import android.content.Context;
import android.content.SharedPreferences;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhengnan on 2015/10/27.
 * 模块间共享参数用的工具类
 */
public enum SharedParamsUtil {
    INS;
    private String spName = "share_params";
//    private

    private SharedPreferences getSp(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(spName, Context.MODE_PRIVATE);
        return sp;
    }

    public void putParam(Context ctx, String key, String value){
        getSp(ctx).edit().putString(key,value).commit();
    }
    public String getParam(Context ctx, String key, String def) {
        return getSp(ctx).getString(key, def);
    }

    //public void
    public List<NameValuePair> getParams(Context ctx, List<NameValuePair> params){
        params.addAll(sp2params(ctx, spName));
        return params;
    }


    public JSONObject getParams(Context ctx, JSONObject jsonObject){
        if(jsonObject==null)return jsonObject;
        try{
            Map<String,Object> map = (Map<String,Object>)getSp(ctx).getAll();
            if(map!=null&&map.size()>0){
                Set<String> cts = map.keySet();
                for(String ct: cts){
                    jsonObject.putOpt(ct,map.get(ct).toString());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonObject;
    }

    ///////////////////
    public static List<NameValuePair> sp2params(Context ctx, String spname) {
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        SharedPreferences master = ctx.getSharedPreferences(spname, Context.MODE_PRIVATE);
        Map<String,Object> map = (Map<String,Object>)master.getAll();
        if(map!=null&&map.size()>0){
            Set<String> cts = map.keySet();
            for(String ct: cts){
                pairs.add(new BasicNameValuePair(ct, map.get(ct).toString()));
            }
        }
        return pairs;
    }

}
