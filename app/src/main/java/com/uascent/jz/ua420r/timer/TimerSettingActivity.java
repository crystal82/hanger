package com.uascent.jz.ua420r.timer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.larksmart7618.sdk.communication.tools.commen.ToastTools;
import com.uascent.jz.ua420r.ControlModule.GosControlModuleBaseActivity;
import com.uascent.jz.ua420r.R;
import com.uascent.jz.ua420r.utils.HexStrUtils;
import com.uascent.jz.ua420r.utils.Lg;
import com.uascent.jz.ua420r.view.VerticalSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by maxiao on 2017/7/3.
 */

public class TimerSettingActivity extends GosControlModuleBaseActivity implements TimerListAdapter.DeleteOnClickListener {
    private static final String TAG = "TimerSetting";
    public static final int ADD_CODE = 0;
    public static final String TIMER = "timer";
    public static final String ISEDIT = "isedit";

    @Bind(R.id.timer_add)
    public FloatingActionButton button;
    @Bind(R.id.timer_list)
    public ListView mTimerListView;
    @Bind(R.id.no_timer)
    public TextView no_Timer;
    public TimerListAdapter adapter;
    public List<Timer> list = new ArrayList<>();
    private VerticalSwipeRefreshLayout swipeRefreshView;

    private Handler handler = new Handler();
    private boolean isgetdata = false;

    GizWifiDevice mDevice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_setting);
        ButterKnife.bind(this);
        mDevice = (GizWifiDevice) getIntent().getParcelableExtra(Constant.DEVICE);
        setActionBar(true, true, getString(R.string.bar_set_time));
        initView();
        getTimers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDevice.setListener(gizWifiDeviceListener);
    }

    private void initView() {
        swipeRefreshView = (VerticalSwipeRefreshLayout) findViewById(R.id.srl);
        swipeRefreshView.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        swipeRefreshView.setColorSchemeResources(R.color.red, R.color.google_blue, R.color.green);

        adapter = new TimerListAdapter(this, list);
        adapter.setDeleteOnClickListener(this);
        mTimerListView.setAdapter(adapter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Lg.e("list.size=" + list.size());
                if (list.size() >= 8) {
                    ToastTools.short_Toast(TimerSettingActivity.this, getString(R.string.max_set_time));
                    return;
                }
                Intent intent = new Intent(TimerSettingActivity.this, AddTimerActivity.class);
                intent.putExtra(ISEDIT, false);
                intent.putExtra(Constant.DEVICE, mDevice);
                startActivityForResult(intent, 0);
            }
        });
        mTimerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(TimerSettingActivity.this, AddTimerActivity.class);
                intent.putExtra(Constant.DEVICE, mDevice);
                intent.putExtra(ISEDIT, true);
                intent.putExtra(TIMER, list.get(position));
                startActivityForResult(intent, 0);
            }
        });

        swipeRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTimers();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (requestCode == 0 && resultCode == RESULT_OK) {
                getTimers();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                mDevice.setListener(null);
                finish();
                break;
        }

        return true;
    }

    public void updateTimerList(List<Timer> l) {
        Log.e(TAG, "updateTimerList");
        swipeRefreshView.setRefreshing(false);
        isgetdata = true;
        list.clear();
        list.addAll(l);
        adapter.notifyDataSetChanged();
        Constant.list.clear();
        Constant.list.addAll(list);
        if (list.size() < 1 || list == null) {
            no_Timer.setVisibility(View.VISIBLE);
            mTimerListView.setVisibility(View.GONE);
        } else {
            no_Timer.setVisibility(View.GONE);
            mTimerListView.setVisibility(View.VISIBLE);
        }
    }


    public void showError(String e) {
        swipeRefreshView.setRefreshing(false);
        ToastTools.short_Toast(this, e);
        isgetdata = true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        handler.removeCallbacks(runnable);
    }

    @Override
    public void deleteOnClick(Timer timer) {
        Log.e("TAG", "deleteOnClick");
        deleteTimer(timer);
    }


    /**
     * 获取定时列表
     */
    public void getTimers() {
        isgetdata = false;
        List<String> attrs=new ArrayList<>();
        attrs.add(Constant.DPID);
        mDevice.getDeviceStatus(attrs);
        if (!swipeRefreshView.isRefreshing())
            swipeRefreshView.setRefreshing(true);
        handler.postDelayed(runnable, 8000);//设置超时 8s
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!isgetdata) {
                showError(getString(R.string.timer_error));
                isgetdata = false;
            }
        }
    };

    public void deleteTimer(Timer timer) {
        String timer_id = timer.getTimerId();
        String timeZone = timer.getTimerZone();
        String timer_action = timer.getTimerAction();
        String week_flag = timer.getWeekFlag();
        String delta_time = timer.getDeltaTime();
        StringBuilder builder = new StringBuilder();
        builder.append(Constant.TIMER_CMD_1);
        builder.append(Constant.VERSION);
        builder.append(Constant.WRITE);
        builder.append(Constant.DEVST);
        builder.append(Constant.ACTION_DEL);
        builder.append(timer_id);
        builder.append(timeZone);
        builder.append(timer_action);
        builder.append(week_flag);
        builder.append(delta_time);
        Log.e(TAG, builder.toString());
        sendCommand(Constant.DPID, HexStrUtils.hexStringToBytes(builder.toString()));
    }

    private void sendCommand(String key, Object value) {
        ConcurrentHashMap<String, Object> hashMap = new ConcurrentHashMap<String, Object>();
        hashMap.put(key, value);
        mDevice.write(hashMap, Constant.SEND_SN);
        Log.e(TAG, "下发数据=" + hashMap.toString());

    }

    public void onAnalysisData(String data) {//06 01 03 00 01 / 11 01 01 00 0800ed71/

        Log.e("TAG", "data=" + data);
        String timer_cmd = data.substring(0, 2);
        Log.i(TAG, "timer_cmd=" + timer_cmd);
        if (timer_cmd.equals(Constant.TIMER_CMD_2)) {
            String timerNum = data.substring(8, 10);//timer 个数
            int num = Integer.parseInt(timerNum, 16);// 转为10进制整型
            int l=num*16+10;
            String timers = data.substring(10, l);//timer
            Log.i(TAG, timers+"timers=" + timers.length());
            if (num < 1)//时钟个数为 0
            {
                //showError("没有定时数据");
                list.clear();
                updateTimerList(list);
                return;
            }
            List<Timer> list = new ArrayList<>();
            Timer timer;
            for (int i = 0; i < timers.length(); i += 16) {
                String tm = timers.substring(i, i + 16);//11 01 01 00 0800ed71
                timer = new Timer();
                timer.setTimerId(tm.substring(0, 2));//11
                timer.setTimerZone(tm.substring(2, 4));//01
                timer.setTimerAction(tm.substring(4, 6));//01
                timer.setWeekFlag(tm.substring(6, 8));//00
                timer.setDeltaTime(tm.substring(8, 16));
                String time = Utils.stampToDate(System.currentTimeMillis() + Long.parseLong(tm.substring(8, 16), 16) * 1000);
                timer.setTime(time);
                list.add(timer);
                Log.e(TAG, timer.toString());
            }
            updateTimerList(list);
        }
    }


    @Override
    public void didReceiveData(GizWifiErrorCode result, GizWifiDevice device, ConcurrentHashMap<String, Object> dataMap, int sn) {
        getDataFromReceiveDataMap(dataMap);
        Lg.e(TAG, "sn=" + sn + "收到数据：" + dataMap.toString());
        // 透传数据，无数据点定义，适合开发者自行定义协议自行解析
        if (dataMap.get("binary") != null) {
            byte[] binary = (byte[]) dataMap.get("binary");
            String data = HexStrUtils.bytesToHexString(binary);
            data = data.toLowerCase();
            Log.e(TAG, "data:" + data);
            String cmd = data.substring(0, 2);
            String flag = data.substring(2, 4);
            Lg.e(TAG, "cmd=" + cmd+"flag=" + flag);
            if (cmd.equals(Constant.CMD_1)&&flag.equals(Constant.TIMER_FLAG)) {
                onAnalysisData(data.substring(4, data.length()));
            } else if (cmd.equals(Constant.CMD_2)&&flag.equals(Constant.TIMER_FLAG)) {
                onAnalysisData(data.substring(4, data.length()));
            }
        }
    }

}
