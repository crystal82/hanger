package com.uascent.jz.ua420r.timer;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.uascent.jz.ua420r.R;

/**
 * Created by maxiao on 2017/7/14.
 */

class GridViewAdapter extends BaseAdapter {

    String week[] = new String[7];
    Context context;
    LayoutInflater mInflater = null;
    private String[] weeks = new String[7];

    public GridViewAdapter(Context context, String[] week) {
        this.week = week;
        this.context = context;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        weeks = context.getResources().getStringArray(R.array.week);
    }

    @Override
    public int getCount() {
        return week.length;
    }

    @Override
    public Object getItem(int i) {
        return week[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            view = mInflater.inflate(R.layout.week_dialog_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (weeks[0].equals("Mon")) {//判断中英文
            holder.tv_Week.setTextSize(12);
        }
        if (week[i].equals("1")) {
            holder.tv_Week.setText(weeks[i]);
            holder.tv_Week.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.tv_Week.setBackgroundResource(R.drawable.week_text_bg);
        } else {
            holder.tv_Week.setText(weeks[i]);
            holder.tv_Week.setTextColor(ContextCompat.getColor(context, R.color.colorNormal));
            holder.tv_Week.setBackgroundResource(R.drawable.week_text);
        }


        return view;
    }

    class ViewHolder {
        TextView tv_Week;

        public ViewHolder(View vv) {
            tv_Week = (TextView) vv.findViewById(R.id.tv_week);
        }
    }
}

