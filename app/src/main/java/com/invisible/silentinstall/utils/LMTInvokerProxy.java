package com.invisible.silentinstall.utils;//package com.invisible.silentinstall.utils;
//
//import android.content.Context;
//
//import com.joy.lmt.LMTInvoker;
//
///**
// * Created by zhengnan on 2015/9/23.
// * -代理使用lmt接口，防止无意的未依赖对应接口模块而导致的崩溃。
// * ---因为崩溃是 不联网时触发所以不是及时的。
// */
//public class LMTInvokerProxy {
//    {
//        isValid = DataUtil.isClassExist("com.joy.lmt.LMTInvoker");
//    }
//    private boolean isValid = false;
//    Object lmtIns = null;
//
//    public LMTInvokerProxy(Context ctx, String tag) {
//        if(!isValid)return;
//        lmtIns = new LMTInvoker(ctx, tag);
//
//    }
//
//    public void BindLMT() {
//        if(!isValid)return;
//        LMTInvoker ins = (LMTInvoker)lmtIns;
//        ins.BindLMT();
//    }
//
//    public void unBindLMT() {
//        if(!isValid)return;
//        LMTInvoker ins = (LMTInvoker)lmtIns;
//        ins.unBindLMT();
//    }
//
//    public boolean isNetConnected(boolean mobile) {
//        if(!isValid)return false;
//        LMTInvoker ins = (LMTInvoker)lmtIns;
//        return ins.isNetConnected(mobile);
//    }
//
//
//
//}
