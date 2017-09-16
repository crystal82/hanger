package com.uascent.jz.ua420r.timer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.uascent.jz.ua420r.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by maxiao on 2017/7/4.
 */

public class TimerListAdapter extends BaseSwipeAdapter {

    private List<Timer> list;
    private Context context;
    private final LayoutInflater mInflater;
    DeleteOnClickListener deleteOnClickListener;

    private String[] weeks = new String[7];

    public TimerListAdapter(Context context, List<Timer> list) {
        this.context = context;
        this.list = list;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        weeks = context.getResources().getStringArray(R.array.weeks);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(int position, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.timer_list_item, null);
        return v;
    }

    @Override
    public void fillValues(final int position, View convertView) {
        TextView tv_cf = (TextView) convertView.findViewById(R.id.cf);
        TextView tv_cz = (TextView) convertView.findViewById(R.id.cz);
        final SwipeLayout mSwipeLayout = (SwipeLayout) convertView.findViewById(getSwipeLayoutResourceId(position));
        if (list.get(position).getTimerAction().equals(Constant.TIMER_ACTION_OPEN)) {
            tv_cz.setText(list.get(position).getTime() + " " + context.getString(R.string.device_open));
        } else {
            tv_cz.setText(list.get(position).getTime() + " " + context.getString(R.string.device_close));
        }
        if (list.get(position).getWeekFlag().equals(Constant.TIMER_WEEK_FLAG)) {
            long l = System.currentTimeMillis();
            long ll = Long.parseLong(list.get(position).getDeltaTime(), 16);
            long delta = Utils.getRelative("24:00");
            long t1 = 24 * 3600 + delta;
            long t2 = t1 * 2 + delta;
            String res = "";
            if (ll < delta) {
                res = "今天";
            } else if (ll < t1) {
                res = "明天";
            } else if (ll < t2) {
                res = "后天";
            } else {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
                Date date = new Date(l + ll * 1000);
                res = simpleDateFormat.format(date);
            }
            tv_cf.setText(res + " " + context.getString(R.string.only_once));
        } else if (list.get(position).getWeekFlag().equals(Constant.TIMER_WEEK_FLAG_CF)) {
            tv_cf.setText(context.getString(R.string.every_day));

        } else {
            String w = "";
            String week = Integer.toBinaryString(Integer.valueOf(list.get(position).getWeekFlag(), 16));
            week = new StringBuilder(week).reverse().toString();
            for (int i = 0; i < week.length(); i++) {
                if (week.substring(i, i + 1).equals("1")) {
                    if (w.equals(""))
                        w = weeks[i];
                    else
                        w = w + "," + weeks[i];
                }
            }
            tv_cf.setText(w);
        }
        //删除监听
        convertView.findViewById(R.id.bottom_wrapper_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteOnClickListener.deleteOnClick(list.get(position));
                Log.e("TAG", "bottom_wrapper_delete");
                list.remove(position);
                notifyDataSetChanged();
                mSwipeLayout.close();
            }
        });
    }

    public interface DeleteOnClickListener {
        void deleteOnClick(Timer timer);
    }

    public void setDeleteOnClickListener(DeleteOnClickListener listener) {
        this.deleteOnClickListener = listener;
    }

}
