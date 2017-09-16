package com.uascent.jz.ua420r.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uascent.jz.ua420r.R;
import com.uascent.jz.ua420r.utils.Lg;

/**
 * Created by maxiao on 2017/9/4.
 */

public class LinkAPTipDialog {
    private Activity mActivity;
    private AlertDialog mDialog;
    private EventListener mListener;

    public abstract static class EventListener {
        public void onClickChanged() {
        }
        public void onBackListener(){}
    }

    public LinkAPTipDialog(Activity activity, EventListener listener) {
        mActivity = activity;
        mListener = listener;
        init();
    }

    public AlertDialog show() {
        mDialog.show();
        return mDialog;
    }

    public void dismiss() {
        mDialog.dismiss();
    }

    private void init() {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.ap_tip_dialog,
                                       (ViewGroup) mActivity.findViewById(R.id.ll_luminance));
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, AlertDialog.THEME_HOLO_LIGHT);
        mDialog = builder.show();
        mDialog.setCanceledOnTouchOutside(false);
        layout.findViewById(R.id.btn_link_ap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClickChanged();
            }
        });
        mDialog.getWindow().setContentView(layout);
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                Lg.e(keyCode+"---------------setOnKeyListener--------------------------"+event);
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mListener.onBackListener();
                    Lg.e("---------------setOnKeyListener--------------------------");
                    return true;
                } else {
                    return false; //默认返回 false，这里false不能屏蔽返回键，改成true就可以了
                }
            }
        });
    }


    public boolean isShowing() {

        return mDialog.isShowing();
    }

}
