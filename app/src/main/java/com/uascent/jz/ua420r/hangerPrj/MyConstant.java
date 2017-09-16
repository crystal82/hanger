package com.uascent.jz.ua420r.hangerPrj;


import com.uascent.jz.ua420r.R;

/**
 * 作者：HWQ on 2017/5/11 18:13
 * 描述：
 */

public interface MyConstant {

    int COLOR_DEFAULT_JT  = R.color.darkYellow;
    int COLOR_DEFAULT_SB  = R.color.black;
    int COLOR_DEFAULT_SSJ = R.color.colorBlue;
    int COLOR_DEFAULT_LG  = R.color.black;

    String DOMAIN         = "uascent";
    String SUBDOMAIN      = "uasocket";
    int    DOMAINID       = 5463;
    int    DEVICE_TYPE_ID = 11;
    //int    DEVICE_TYPE_ID = AC.DEVICE_HF;

    int CONFIT_BINARY = 0;
    int CONFIT_JSON   = 1;

    int CODE_MSG       = 68; //发送消息的CODE
    int CODE_REQ_STATE = 69; //初始化状态

    int  FLEX_LEVEL    = 63;//伸缩等级，63位
    byte DEFAULT_LIGHT = -16; //初始化亮度 1111 0000 表示开
    byte DEFAULT_VOICE = -16; //初始化音量 1111 0000 表示开

    long REQUEST_STATE_POLL_TIME = 10 * 1000; //请求设备状态轮询间隔时间
}
