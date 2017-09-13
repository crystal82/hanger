package com.uascent.jz.ua420r.timer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;


import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.larksmart7618.sdk.communication.tools.commen.ToastTools;
import com.uascent.jz.ua420r.ControlModule.GosControlModuleBaseActivity;
import com.uascent.jz.ua420r.R;
import com.uascent.jz.ua420r.utils.HexStrUtils;
import com.uascent.jz.ua420r.view.PickTimeView;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.drakeet.materialdialog.MaterialDialog;


/**
 * Created by maxiao on 2017/7/3.
 */

public class AddTimerActivity extends GosControlModuleBaseActivity implements PickTimeView.onSelectedChangeListener {

    private boolean OPEN = true;
    private String REPEAT = Constant.TIMER_WEEK_FLAG;
    private boolean ISEDIT = false;
    private GizWifiDevice mDevice = null;
    @Bind(R.id.tv_cz)
    public TextView tv_Cz;
    @Bind(R.id.tv_cf)
    public TextView tv_Cf;
    @Bind(R.id.timePicker)
    public TimePicker timePicker;
    @Bind(R.id.pickTime)
    public PickTimeView pickTimeView;
    public Timer ti;


    public String[] week = {"0", "0", "0", "0", "0", "0", "0"};
    public List<String> week_list = new ArrayList<>();
    MaterialDialog mMaterialDialog;

    SimpleDateFormat sdfTime;
    public long delta;//仅一次时的hex时间差值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_timer);
        ButterKnife.bind(this);
        setActionBar(true, true, getString(R.string.bar_add_time));
        initView();
        initData();
    }

    private void initView() {

        pickTimeView.setOnSelectedChangeListener(this);
        pickTimeView.setViewType(PickTimeView.TYPE_PICK_TIME);
        sdfTime = new SimpleDateFormat("MM-dd EEE HH:mm");
        pickTimeView.setVisibility(View.VISIBLE);
        timePicker.setVisibility(View.GONE);
        timePicker.setIs24HourView(true);

    }


    private void initData() {

        Intent intent = getIntent();
        mDevice = (GizWifiDevice) intent.getParcelableExtra(Constant.DEVICE);
        mDevice.setListener(gizWifiDeviceListener);
        if (intent.getBooleanExtra(TimerSettingActivity.ISEDIT, false)) {
            ISEDIT = true;
            ti = (Timer) intent.getSerializableExtra(TimerSettingActivity.TIMER);
            String[] ttt = ti.getTime().split(":");// 10:09 hour=10 minute=09
            timePicker.setCurrentHour(Integer.parseInt(ttt[0]));
            timePicker.setCurrentMinute(Integer.parseInt(ttt[1]));
            if (ti.getTimerAction().equals(Constant.TIMER_ACTION_OPEN)) {
                tv_Cz.setText(getString(R.string.device_open));
                OPEN = true;
            } else {
                tv_Cz.setText(getString(R.string.device_close));
                OPEN = false;
            }
            if (ti.getWeekFlag().equals(Constant.TIMER_WEEK_FLAG)) {
                tv_Cf.setText(getString(R.string.only_once));
                REPEAT = Constant.TIMER_WEEK_FLAG;
                pickTimeView.setVisibility(View.VISIBLE);
                timePicker.setVisibility(View.GONE);
                pickTimeView.setTimeMillis(System.currentTimeMillis() + Long.parseLong(ti.getDeltaTime(), 16) * 1000);
                delta = Long.parseLong(ti.getDeltaTime(), 16);
            } else if (ti.getWeekFlag().equals(Constant.TIMER_WEEK_FLAG_CF)) {
                tv_Cf.setText(getString(R.string.every_day));
                REPEAT = Constant.TIMER_WEEK_FLAG_CF;
                pickTimeView.setVisibility(View.GONE);
                timePicker.setVisibility(View.VISIBLE);
            } else {
                pickTimeView.setVisibility(View.GONE);
                timePicker.setVisibility(View.VISIBLE);
                tv_Cf.setText(getString(R.string.week_repeat));
                REPEAT = ti.getWeekFlag();
                String weeks = Integer.toBinaryString(Integer.valueOf(REPEAT, 16));
                weeks = new StringBuilder(weeks).reverse().toString();
                for (int i = 0; i < weeks.length(); i++) {
                    week[i] = weeks.substring(i, i + 1);
                    if (week[i].equals("1")) {
                        week_list.add("" + (i + 1));
                    }
                }
            }
        } else {
            pickTimeView.setTimeMillis(System.currentTimeMillis() + 60 * 1000);
            delta = (pickTimeView.getTimeMillis() - System.currentTimeMillis()) / 1000;
        }
    }

    public void addOrEditTimer() {
        progressDialog.setMessage(getString(R.string.processing));
        progressDialog.show();
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();
        Timer timer = new Timer();
        if (ISEDIT) {
            timer.setTimerId(ti.getTimerId());
        } else {
            timer.setTimerId(Utils.getID());

        }
        Log.e("TAG", "id=" + timer.getTimerId());
        timer.setTimerZone(Utils.getCurrentTimeZone());//设置时区 08
        timer.setAction(Constant.ACTION_ADD_OR_EDIT);
        if (REPEAT.equals(Constant.TIMER_WEEK_FLAG_CF)) {
            timer.setWeekFlag(Constant.TIMER_WEEK_FLAG_CF);//每天 11111110
        } else if (REPEAT.equals(Constant.TIMER_WEEK_FLAG)) {
            timer.setWeekFlag(Constant.TIMER_WEEK_FLAG);//仅一次 00
        } else {
            timer.setWeekFlag(REPEAT);
        }
        if (OPEN) {
            timer.setTimerAction(Constant.TIMER_ACTION_OPEN); //开启 01
        } else {
            timer.setTimerAction(Constant.TIMER_ACTION_CLOSE); //关闭 00
        }
        if (REPEAT.equals(Constant.TIMER_WEEK_FLAG)) {
            if (delta < 1) {
                progressDialog.cancel();
                ToastTools.short_Toast(this, getString(R.string.time_setting_is_wrong));
                return;
            }
            String tim = Long.toHexString(delta);
            int l = tim.length();
            for (int i = 0; i < (8 - l); i++) {
                tim = "0" + tim;
            }
            timer.setDeltaTime(tim);//差值
        } else if (REPEAT.equals(Constant.TIMER_WEEK_FLAG_CF)) {
            String tim = Utils.convert10To16((int) Utils.getRelativeTime(hour + ":" + minute));
            int l = tim.length();
            for (int i = 0; i < (8 - l); i++) {
                tim = "0" + tim;
            }
            Log.e("TAG", "tim=" + tim);
            timer.setDeltaTime(tim);//差值
        } else {//周重复计算时间差
            String tim = "";
            Calendar cal = Calendar.getInstance();
            int wk = cal.get(Calendar.DAY_OF_WEEK);
            if (wk == 1) {
                wk = 7;
            } else {
                wk = wk - 1;
            }
            Log.e("TAG", "week_list(0)=" + week_list.get(0));
            Collections.sort(week_list);//排序
            Log.e("TAG", "week_list(0)==" + week_list.get(0));
            if (week_list.contains(wk + "")) {
                if (Utils.getRelative(hour + ":" + minute) > 0)
                    tim = Utils.convert10To16((int) Utils.getRelativeTime(hour + ":" + minute));
                else {
                    int loc = week_list.indexOf(wk + "");
                    if (loc < week_list.size() - 1)
                        tim = Utils.convert10To16((int) Utils.getRelativeTime2(hour + ":" + minute, Integer.parseInt(week_list.get(loc + 1)) - wk));
                    else if (loc > 0)
                        tim = Utils.convert10To16((int) Utils.getRelativeTime2(hour + ":" + minute, Integer.parseInt(week_list.get(0)) - wk + 7));
                    else
                        tim = Utils.convert10To16((int) Utils.getRelativeTime2(hour + ":" + minute, 7));
                }
            } else {
                boolean is = false;
                for (int i = 0; i < week_list.size(); i++) {
                    if (Integer.parseInt(week_list.get(i)) > wk) {
                        tim = Utils.convert10To16((int) Utils.getRelativeTime2(hour + ":" + minute, Integer.parseInt(week_list.get(i)) - wk));
                        is = true;
                        break;
                    }
                }
                if (!is) {
                    tim = Utils.convert10To16((int) Utils.getRelativeTime2(hour + ":" + minute, Integer.parseInt(week_list.get(0)) - wk + 7));
                }
            }


            int l = tim.length();
            for (int i = 0; i < (8 - l); i++) {
                tim = "0" + tim;
            }
            Log.e("TAG", "tim=" + tim);
            timer.setDeltaTime(tim);//差值
        }
        addTimer(timer);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.timerok, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;
            case R.id.timer_ok:
                addOrEditTimer();
                break;
        }

        return true;
    }


    /**
     * 操作选择 Dialog
     *
     * @param open
     */
    private void operationDialog(boolean open) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.add_time_dialog, null);// 得到加载view
        final RelativeLayout rl_O = (RelativeLayout) v.findViewById(R.id.rl_o);
        final RelativeLayout rl_C = (RelativeLayout) v.findViewById(R.id.rl_c);
        final ImageButton imageButton_O = (ImageButton) v.findViewById(R.id.open);
        final ImageButton imageButton_C = (ImageButton) v.findViewById(R.id.close);
        if (open) {
            imageButton_O.setImageResource(R.drawable.check);
            imageButton_C.setImageResource(R.drawable.check_c);
        } else {
            imageButton_O.setImageResource(R.drawable.check_c);
            imageButton_C.setImageResource(R.drawable.check);
        }
        rl_O.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageButton_O.setImageResource(R.drawable.check);
                imageButton_C.setImageResource(R.drawable.check_c);
                OPEN = true;
            }
        });
        rl_C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageButton_O.setImageResource(R.drawable.check_c);
                imageButton_C.setImageResource(R.drawable.check);
                OPEN = false;
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Dialog_Alert);
        builder.setView(v)
                .setTitle(getString(R.string.operation))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.besure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (OPEN) {
                            tv_Cz.setText(getString(R.string.device_open));
                        } else {
                            tv_Cz.setText(getString(R.string.device_close));
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        canCloseDialog(dialogInterface, true);
                    }
                })
                .show();
    }

    /**
     * 重复设置 Dialog
     *
     * @param cf
     */
    private void showRepltDialog(String cf) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.add_relpt_dialog, null);// 得到加载view
        final RelativeLayout rl_O = (RelativeLayout) v.findViewById(R.id.rl_o);
        final RelativeLayout rl_C = (RelativeLayout) v.findViewById(R.id.rl_c);
        final RelativeLayout rl_W = (RelativeLayout) v.findViewById(R.id.rl_w);
        final ImageButton imageButton_O = (ImageButton) v.findViewById(R.id.check1);
        final ImageButton imageButton_C = (ImageButton) v.findViewById(R.id.check2);
        final ImageButton imageButton_W = (ImageButton) v.findViewById(R.id.check3);

        if (cf.equals(Constant.TIMER_WEEK_FLAG_CF)) {
            imageButton_O.setImageResource(R.drawable.check);
            imageButton_C.setImageResource(R.drawable.check_c);
            imageButton_W.setImageResource(R.drawable.check_c);
        } else if (cf.equals(Constant.TIMER_WEEK_FLAG)) {
            imageButton_O.setImageResource(R.drawable.check_c);
            imageButton_C.setImageResource(R.drawable.check);
            imageButton_W.setImageResource(R.drawable.check_c);
        } else {
            imageButton_O.setImageResource(R.drawable.check_c);
            imageButton_C.setImageResource(R.drawable.check_c);
            imageButton_W.setImageResource(R.drawable.check);
        }
        mMaterialDialog = new MaterialDialog(this)
                .setTitle(getString(R.string.repeat))
                .setContentView(v)
                .setPositiveButton(getString(R.string.besure), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (REPEAT.equals(Constant.TIMER_WEEK_FLAG_CF)) {
                            pickTimeView.setVisibility(View.GONE);
                            timePicker.setVisibility(View.VISIBLE);
                            tv_Cf.setText(getString(R.string.every_day));
                        } else if (REPEAT.equals(Constant.TIMER_WEEK_FLAG)) {
                            pickTimeView.setVisibility(View.VISIBLE);
                            timePicker.setVisibility(View.GONE);
                            tv_Cf.setText(getString(R.string.only_once));
                        } else {
                            pickTimeView.setVisibility(View.GONE);
                            timePicker.setVisibility(View.VISIBLE);
                            tv_Cf.setText(getString(R.string.week_repeat));
                        }
                        mMaterialDialog.dismiss();

                    }
                })
                .setNegativeButton(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();

                    }
                });
        mMaterialDialog.setCanceledOnTouchOutside(false);
        mMaterialDialog.show();

        rl_O.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageButton_O.setImageResource(R.drawable.check);
                imageButton_C.setImageResource(R.drawable.check_c);
                imageButton_W.setImageResource(R.drawable.check_c);
                REPEAT = Constant.TIMER_WEEK_FLAG_CF;
            }
        });
        rl_C.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageButton_O.setImageResource(R.drawable.check_c);
                imageButton_W.setImageResource(R.drawable.check_c);
                imageButton_C.setImageResource(R.drawable.check);
                REPEAT = Constant.TIMER_WEEK_FLAG;
            }
        });
        rl_W.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weekSettingDialog();
                imageButton_O.setImageResource(R.drawable.check_c);
                imageButton_W.setImageResource(R.drawable.check);
                imageButton_C.setImageResource(R.drawable.check_c);
                mMaterialDialog.dismiss();
            }
        });

    }

    /**
     * 周重复选择  Dialog
     */
    public void weekSettingDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.week_dialog, null);// 得到加载view
        GridView gv = (GridView) v.findViewById(R.id.week_list);
        final GridViewAdapter adapter = new GridViewAdapter(this, week);
        gv.setAdapter(adapter);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (week[i].equals("0")) {
                    week[i] = "1";
                    week_list.add((i + 1) + "");
                } else {
                    week[i] = "0";
                    week_list.remove((i + 1) + "");
                }

                adapter.notifyDataSetChanged();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Dialog_Alert);
        builder.setView(v)
                .setTitle(getString(R.string.week_repeat))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.besure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        pickTimeView.setVisibility(View.GONE);
                        timePicker.setVisibility(View.VISIBLE);
                        String str = "";
                        for (int w = 0; w < 7; w++) {
                            str = str + week[w];
                        }
                        str = new StringBuilder(str).reverse().toString();
                        Log.e("TAG", "str=" + str);
                        REPEAT = Integer.toHexString(Integer.parseInt(str, 2));
                        if (REPEAT.length() == 1) {
                            REPEAT = "0" + REPEAT;
                        }
                        Log.e("TAG", "REPEAT=" + REPEAT);
                        tv_Cf.setText(getString(R.string.week_repeat));
                        canCloseDialog(dialogInterface, true);

                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        canCloseDialog(dialogInterface, true);
                        showRepltDialog(REPEAT);
                    }
                })
                .show();
    }

    //  手动关闭对话框
    private void canCloseDialog(DialogInterface dialogInterface, boolean close) {
        try {
            Field field = dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialogInterface, close);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.ll_cz, R.id.ib_cz})
    public void cz() {
        operationDialog(OPEN);
    }

    @OnClick({R.id.ll_cf, R.id.ib_cf})
    public void cf() {
        showRepltDialog(REPEAT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public void onSelected(PickTimeView view, long timeMillis) {
        if (view == pickTimeView) {
            String str = sdfTime.format(timeMillis);
            Log.e("TAG", "str=" + str);
            delta = (timeMillis - System.currentTimeMillis()) / 1000;//ms=>s
            Log.e("TAG", delta + "deltaTime=" + Long.toHexString(delta));
        }
    }

    public void addTimer(Timer timer) {
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
        builder.append(Constant.ACTION_ADD_OR_EDIT);
        builder.append(timer_id);
        builder.append(timeZone);
        builder.append(timer_action);
        builder.append(week_flag);
        builder.append(delta_time);
        Log.e("TAG", builder.toString());
        sendCommand(Constant.DPID, HexStrUtils.hexStringToBytes(builder.toString()));
    }


    private void sendCommand(String key, Object value) {
        ConcurrentHashMap<String, Object> hashMap = new ConcurrentHashMap<String, Object>();
        hashMap.put(key, value);
        mDevice.write(hashMap, Constant.SEND_SN);
        Log.e("TAG", "下发数据=" + hashMap.toString());
        handler.postDelayed(runnable, 5000);

    }

    @Override
    public void didReceiveData(GizWifiErrorCode result, GizWifiDevice device, ConcurrentHashMap<String, Object> dataMap, int sn) {
        getDataFromReceiveDataMap(dataMap);
        Log.e("TAG", "sn=" + sn + "收到数据：" + dataMap.toString());
        if (sn == 5) {
            handler.removeCallbacks(runnable);
            handler.sendEmptyMessage(0x01);
        }

    }

    public Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x01) {
                progressDialog.cancel();
                ToastTools.short_Toast(AddTimerActivity.this, getString(R.string.add_time_success));
                Intent intent = new Intent();
                intent.putExtra("add", true);
                setResult(RESULT_OK, intent);
                finish();
            }
            if (msg.what == 0x02) {
                progressDialog.cancel();
                ToastTools.short_Toast(AddTimerActivity.this, getString(R.string.add_time_error));
            }
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(0x02);
        }
    };

}