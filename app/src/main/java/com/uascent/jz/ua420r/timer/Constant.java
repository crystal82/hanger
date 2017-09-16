package com.uascent.jz.ua420r.timer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxiao on 2017/7/5.
 */

public class Constant {

    public static final String DPID = "timer";
    public static final String RSSI_LVL = "rssi_lvl";

    public static final String CMD_1 = "13";
    public static final String CMD_2 = "14";

    public static final String TIMER_CMD_1 = "05";
    public static final String TIMER_CMD_2 = "06";
    public static final String TIMER_FLAG = "04";
    public static final String FLAG = "07";

    public static final String VERSION = "01";
    public static final String RESERVED = "00";

    public static final String READ = "02";
    public static final String WRITE = "01";
    public static final String DEVST = "01";

    public static final String TIMER_ACTION_OPEN = "01";
    public static final String TIMER_ACTION_CLOSE = "00";

    public static final String TIMER_WEEK_FLAG = "00";
    public static final String TIMER_WEEK_FLAG_CF = "7f";
    public static final String TIMER_WEEK_ = "";

    public static final String ACTION_ADD_OR_EDIT = "00";
    public static final String ACTION_DEL = "01";

    public static final int  SEND_SN = 5;
    public static final String  DEVICE = "device";


    public static List<Timer> list = new ArrayList<>();
}
