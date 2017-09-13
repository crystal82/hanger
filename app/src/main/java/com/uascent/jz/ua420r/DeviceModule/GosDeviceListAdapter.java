package com.uascent.jz.ua420r.DeviceModule;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.uascent.jz.ua420r.R;

import java.util.List;

@SuppressLint("InflateParams")
public class GosDeviceListAdapter extends BaseAdapter {

    Handler handler = new Handler();
    protected static final int UNBOUND = 99;
    private GizWifiDevice mDevice;

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    Context context;
    List<GizWifiDevice> deviceList;

    public GosDeviceListAdapter(Context context, List<GizWifiDevice> deviceList) {
        super();
        this.context = context;
        this.deviceList = deviceList;
    }

    OnClockClickListener clockClickListener;

    public void setOnClockClickListener(OnClockClickListener clockClickListener) {
        this.clockClickListener = clockClickListener;
    }

    OnTrashClickListener trashClickListener;

    public void setOnTrashClickListener(OnTrashClickListener trashClickListener) {
        this.trashClickListener = trashClickListener;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Holder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(
                    R.layout.item_gos_device_list, null);
            holder = new Holder(view);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }
        mDevice = deviceList.get(position);
        String LAN, noLAN, unbind;
        LAN = (String) context.getText(R.string.lan);
        noLAN = (String) context.getText(R.string.no_lan);
        unbind = (String) context.getText(R.string.unbind);
        String deviceAlias = mDevice.getAlias();
        String devicePN = mDevice.getProductName();
        if (mDevice.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceOnline
                || mDevice.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceControlled) {
            if (mDevice.isBind()) {
                // 已绑定设备，在线
                holder.getRlItem().setBackgroundColor(Color.parseColor("#3fc2fb"));
                holder.getTvDeviceMac().setText(mDevice.getMacAddress());
                holder.getImgWifi().setVisibility(View.VISIBLE);
                //holder.getImgClock().setVisibility(View.VISIBLE);
                holder.getImgClock().setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        clockClickListener.onClick(position);
                    }
                });
                holder.getImgTrash().setVisibility(View.VISIBLE);
                holder.getImgTrash().setOnClickListener(onImgTrashClick(deviceList.get(position)));

                if (mDevice.isLAN()) {
                    holder.getTvDeviceStatus().setText(LAN);
                    holder.getImgLeft().setSelected(true);
                    holder.imgWifi.setImageResource(R.mipmap.wifi_icon2);
                } else {
                    holder.getImgLeft().setSelected(false);
                    holder.getTvDeviceStatus().setText(noLAN);
                    holder.imgWifi.setImageResource(R.mipmap.internet_icon);
                }
               //if (TextUtils.isEmpty(deviceAlias)) {
               //    holder.getTvDeviceName().setText(devicePN);
               //} else {
               //    holder.getTvDeviceName().setText(deviceAlias);
               //}

            } else {// 未绑定设备
                holder.getImgTrash().setVisibility(View.GONE);
                //holder.getImgClock().setOnClickListener(imgClockListener);
                holder.getTvDeviceMac().setText(mDevice.getMacAddress());
                holder.getRlItem().setBackgroundColor(Color.parseColor("#808080"));
                holder.getTvDeviceStatus().setText(unbind);
               //if (TextUtils.isEmpty(deviceAlias)) {
               //    holder.getTvDeviceName().setText(devicePN);
               //} else {
               //    holder.getTvDeviceName().setText(deviceAlias);
               //}
            }
        } else {// 设备不在线
            holder.getImgWifi().setVisibility(View.GONE);
            holder.getImgClock().setVisibility(View.GONE);
            holder.getImgTrash().setVisibility(View.VISIBLE);
            holder.getImgTrash().setOnClickListener(onImgTrashClick(deviceList.get(position)));

            holder.getRlItem().setBackgroundColor(ContextCompat.getColor(context, R.color.background_gray));
            holder.getTvDeviceMac().setText(mDevice.getMacAddress());
            //holder.getTvDeviceMac().setTextColor(context.getResources().getColor(R.color.gray));
            holder.getTvDeviceStatus().setText(context.getString(R.string.out_line)
            );
            //holder.getTvDeviceStatus().setTextColor(context.getResources().getColor(R.color.gray));
            holder.getImgRight().setVisibility(View.GONE);
            //holder.getLlLeft().setBackgroundResource(R.drawable.btn_getcode_shape_gray);
            holder.getImgLeft().setSelected(false);

           //if (TextUtils.isEmpty(deviceAlias)) {
           //    holder.getTvDeviceName().setText(devicePN);
           //} else {
           //    holder.getTvDeviceName().setText(deviceAlias);
           //}

            //holder.getTvDeviceName().setTextColor(
            //        context.getResources().getColor(R.color.gray));
        }
        holder.getDelete2().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUnBoundCmd(mDevice);
            }
        });
        return view;
    }

    @NonNull
    private OnClickListener onImgTrashClick(final GizWifiDevice gizWifiDevice) {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                trashClickListener.onClick();
                bindErrorDialog(gizWifiDevice);
            }
        };
    }

    private void bindErrorDialog(final GizWifiDevice gizWifiDevice) {
        final Dialog dialog = new AlertDialog.Builder(context)
                .setView(new EditText(context)).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_judge2);

        LinearLayout llNo, llSure;
        llNo = (LinearLayout) window.findViewById(R.id.tv_dialog_cancel);
        llSure = (LinearLayout) window.findViewById(R.id.tv_dialog_enter);
        ((TextView) window.findViewById(R.id.tv_title)).setText(R.string.is_to_delete);

        llNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        llSure.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendUnBoundCmd(gizWifiDevice);
                if (dialog.isShowing()) {
                    dialog.cancel();
                }
            }
        });
    }

    private void sendUnBoundCmd(GizWifiDevice device) {
        Message message = new Message();
        message.what = UNBOUND;
        message.obj = device.getDid().toString();
        handler.sendMessage(message);
    }

    public interface OnClockClickListener {
        void onClick(int position);
    }

    public interface OnTrashClickListener {
        boolean onClick();
    }

    class Holder {
        View view;

        public Holder(View view) {
            this.view = view;
        }

        private TextView tvDeviceMac, tvDeviceStatus, tvDeviceName;

        private RelativeLayout delete2, rlItem, mRlOnlineIcon;

        private ImageView imgRight, imgTrash, imgWifi, imgClock, imgLeft;

        private LinearLayout llLeft;

        public RelativeLayout getRlItem() {
            if (null == rlItem) {
                rlItem = (RelativeLayout) view.findViewById(R.id.rl_item);
            }
            return rlItem;
        }

        public RelativeLayout getRlOnlineIcon() {
            if (null == mRlOnlineIcon) {
                mRlOnlineIcon = (RelativeLayout) view.findViewById(R.id.rl_online_icons);
            }
            return mRlOnlineIcon;
        }

        public LinearLayout getLlLeft() {
            if (null == llLeft) {
                llLeft = (LinearLayout) view.findViewById(R.id.llLeft);
            }
            return llLeft;
        }

        public ImageView getImgLeft() {
            if (null == imgLeft) {
                imgLeft = (ImageView) view.findViewById(R.id.imgLeft);
            }
            return imgLeft;
        }

        public ImageView getImgWifi() {
            if (null == imgWifi) {
                imgWifi = (ImageView) view.findViewById(R.id.iv_wifi_icon);
            }
            return imgWifi;
        }


        public ImageView getImgClock() {
            if (null == imgClock) {
                imgClock = (ImageView) view.findViewById(R.id.iv_clock_icon);
            }
            return imgClock;
        }

        OnClickListener imgTrashListener;

        public void setTrashListener(OnClickListener imgTrashListener) {
            this.imgTrashListener = imgTrashListener;
        }

        public ImageView getImgTrash() {
            if (null == imgTrash) {
                imgTrash = (ImageView) view.findViewById(R.id.iv_trash_icon);
                imgTrash.setOnClickListener(imgTrashListener);
            }
            return imgTrash;
        }

        public ImageView getImgRight() {
            if (null == imgRight) {
                imgRight = (ImageView) view.findViewById(R.id.imgRight);
            }
            return imgRight;
        }

        public RelativeLayout getDelete2() {
            if (null == delete2) {
                delete2 = (RelativeLayout) view.findViewById(R.id.delete2);
            }
            return delete2;
        }

        public TextView getTvDeviceMac() {
            if (null == tvDeviceMac) {
                tvDeviceMac = (TextView) view.findViewById(R.id.tvDeviceMac);
            }
            return tvDeviceMac;
        }

        public TextView getTvDeviceStatus() {
            if (null == tvDeviceStatus) {
                tvDeviceStatus = (TextView) view.findViewById(R.id.tvDeviceStatus);
            }
            return tvDeviceStatus;
        }

        //TODO:名称
       //public TextView getTvDeviceName() {
       //    if (null == tvDeviceName) {
       //        tvDeviceName = (TextView) view.findViewById(R.id.tvDeviceName);
       //    }
       //    return tvDeviceName;
       //}
    }
}
