package com.uascent.jz.ua420r.hangerPrj;

import android.content.Context;
import android.widget.Toast;


import com.uascent.jz.ua420r.utils.Lg;

import java.util.Arrays;

/**
 * 作者：HWQ on 2017/6/20 17:52
 * 描述：主机发给WIFI模块的数据定义
 */

public class HangerBean implements Cloneable {
    public byte customerId; //客户编号,用于请求获取背景图片
    public byte deviceNum; //机型编号
    public byte reserve = -76; //备用 B4
    public byte lightState; //照明设置  Bit7–4：亮度调整参数  F：照明灯开 0：照明灯关  Bit3–0：备用
    public byte heightState; //晾干状态 FFH：晾干不变化 F0H：为停止(停止键) 0：为上升到顶(上升键)64：为下降到底(下降键) 0-64：为位置设置
    public byte buttonState; //按钮状态   Bit7  1：消毒 Bit6  1：风干 Bit5  1：热风  Bit4:1--体感
    public byte voiceState; //语音状态   Bit7–4：语音参数 0：语音关 F：语音开最大 Bit3–0：备用
    public byte otherState; //其它状态
    public byte temperature; //温度
    public byte humidity; //湿度
    public byte endCode; //结束码   App发送:69H   主机收到 A5H：收到应答 A5H：收到应答
    public byte appVersion = 0x01; //App版本  从01H开始编

    public long   deviceId;
    public String physicalDeviceId;
    private byte appSendEndCode = 0x69;

    public         ButtonBean buttonBean;
    public         DeviceBean deviceBean;
    public         OtherBean  otherBean;
    public         String     bgUrl;
    private static HangerBean sHangerBean;

    private HangerBean(byte[] receiveData) {
        setReceiveData(receiveData);
    }

    public static HangerBean getInstance(byte[] receiveData) {
        if (sHangerBean == null) {
            synchronized (HangerBean.class) {
                if (sHangerBean == null) {
                    sHangerBean = new HangerBean(receiveData);
                }
            }
        } else {
            sHangerBean.setReceiveData(receiveData);
        }
        return sHangerBean;
    }

    public void setReceiveData(byte[] receiveData) {
        if (receiveData.length < 10) {
            return;
        }
        customerId = 0x10; //TODO:暂时写死   receiveData[0]
        deviceNum = receiveData[1];
        lightState = receiveData[2];
        heightState = receiveData[3];
        buttonState = receiveData[4];
        voiceState = receiveData[5];
        otherState = receiveData[6];
        temperature = receiveData[7];
        humidity = receiveData[8];
        endCode = receiveData[9];

        otherBean = new OtherBean((otherState));
        Lg.e(otherBean.toString());
        deviceBean = new DeviceBean((deviceNum));
        Lg.e(deviceBean.toString());
        buttonBean = new ButtonBean((byte) (buttonState >> 4));
        Lg.e(buttonBean.toString());
    }

    @Override
    public HangerBean clone() {
        HangerBean hangerBean = null;
        try {
            hangerBean = (HangerBean) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return hangerBean;
    }

    //public void setSendData(byte[] sendData) {
    //    //Lg.d("---初始化HangerBean：---" + Arrays.toString(receiveData));
    //    customerId = sendData[0];
    //    reserve = sendData[1];
    //    lightState = sendData[2];
    //    heightState = sendData[3];
    //    buttonState = sendData[4];
    //    voiceState = sendData[5];
    //    appVersion = sendData[6];
    //    appSendEndCode = sendData[7];
    //}

    //TODO:发送给wifi设备的值
    public byte[] getSendData() {
        byte[] bytes = new byte[8];
        bytes[0] = customerId;
        bytes[1] = reserve;  //备用位
        bytes[2] = lightState;
        bytes[3] = heightState;
        bytes[4] = buttonState;
        bytes[5] = voiceState;
        bytes[6] = appVersion;
        bytes[7] = appSendEndCode;  //69H
        return bytes;
    }


    public byte setbuttonState(int type, boolean state) {
        if (state) {
            //置1
            buttonState = (byte) (buttonState | (1 << type));
        } else {
            //置0
            buttonState = (byte) (buttonState & ~(1 << type));
        }
        return buttonState;
    }

    public class OtherBean {
        public boolean overweightPro; //超重保护
        public boolean stickPro;//继电器粘连
        public boolean blockPro;//有阻碍
        public boolean testPro;//自动测试

        public OtherBean(byte deviceState) {
            setData(deviceState);
        }

        public void setData(byte deviceState) {
            int data = deviceState & 0xff;
            overweightPro = (data & 128) == 128;
            stickPro = (data & 64) == 64;
            blockPro = (data & 32) == 32;
            testPro = (data & 1) == 1;
        }

        @Override
        public String toString() {
            return "OtherBean{" +
                    "overweightPro=" + overweightPro +
                    ", stickPro=" + stickPro +
                    ", blockPro=" + blockPro +
                    ", testPro=" + testPro +
                    '}';
        }
    }

    public class DeviceBean {
        public boolean haveDisinfect; //带消毒
        public boolean haveAirDrying;//带风干
        public boolean haveHotwind;//带热风
        public boolean haveLightSet;//带照明调光
        public boolean haveVoiceSet;//带语音
        public boolean haveBody;//带体感

        public DeviceBean(byte deviceState) {
            setData(deviceState);
        }

        public void setData(byte deviceState) {
            int data = deviceState & 0xff;
            haveDisinfect = (data & 128) == 128;
            haveAirDrying = (data & 64) == 64;
            haveHotwind = (data & 32) == 32;
            haveBody = (data & 4) == 4;
            haveLightSet = (data & 2) == 2;
            haveVoiceSet = (data & 1) == 1;
        }

        @Override
        public String toString() {
            return "DeviceBean{" +
                    "haveDisinfect=" + haveDisinfect +
                    ", haveAirDrying=" + haveAirDrying +
                    ", haveHotwind=" + haveHotwind +
                    ", haveLightSet=" + haveLightSet +
                    ", haveVoiceSet=" + haveVoiceSet +
                    '}';
        }
    }

    public class ButtonBean {
        public boolean isDisinfect; //消毒
        public boolean isAirDrying; //风干
        public boolean isHotwind; //热风
        public boolean isKinect; //体感

        public ButtonBean(byte buttonState) {
            setData(buttonState);
        }

        public void setData(byte buttonState) {
            int data = buttonState & 0xff;
            isDisinfect = (data & 128) == 128;
            isAirDrying = (data & 64) == 64;
            isHotwind = (data & 32) == 32;
            isKinect = (data & 16) == 16;
        }

        @Override
        public String toString() {
            return "ButtonBean{" +
                    "isDisinfect=" + isDisinfect +
                    ", isAirDrying=" + isAirDrying +
                    ", isHotwind=" + isHotwind +
                    ", isKinect=" + isKinect +
                    '}';
        }
    }

    public static void sendCommand(
            final Context context, final String physicalDeviceId,
            int code, byte[] data) {
        Toast.makeText(context, "发送:" + Arrays.toString(data) + "  code:" + code + "  Id:" + physicalDeviceId, Toast.LENGTH_SHORT);
        Lg.d("sendCommand发送:" + Arrays.toString(data) + "  code:" + code + "  Id:" + physicalDeviceId);
    }

    //TODO:读取设备状态
    public static void readState(final Context context, String physicalDeviceId, int code, byte[] data) {
        Toast.makeText(context, "发送:" + Arrays.toString(data) + "  code:" + code + "  Id:" + physicalDeviceId, Toast.LENGTH_SHORT);
        Lg.d("readState发送:" + Arrays.toString(data) + "  code:" + code + "  Id:" + physicalDeviceId);
    }


    public int getCustomerId() {
        return customerId & 0xff;
    }

    public int getDeviceNum() {
        return deviceNum & 0xff;
    }

    public int getReserve() {
        return reserve & 0xff;
    }

    public int getHeightState() {
        return heightState & 0xff;
    }

    public int getButtonState() {
        return buttonState & 0xff;
    }

    public int getVoiceState() {
        return voiceState & 15;
    }

    public int getLightState() {
        return lightState & 15;
    }

    public boolean getButtonState(int type) {
        return (buttonState & (1 << type)) == (1 << type);
    }

    public int getOtherState() {
        return otherState & 0xff;
    }

    public int getTemperature() {
        return temperature & 0xff;
    }

    public int getHumidity() {
        return humidity & 0xff;
    }

    public int getEndCode() {
        return endCode & 0xff;
    }

    public int getAppVersion() {
        return appVersion & 0xff;
    }

    public int getAppSendEndCode() {
        return appSendEndCode & 0xff;
    }

    public String getBgUrl() {
        return bgUrl;
    }


    public boolean isLightOpen() {
        int data = lightState & 0xff;
        return (data & 128) == 128;
    }

    public int onLightClickData() {
        int data = lightState & 0xff;
        return isLightOpen() ? (data & 127) : (data | 128);
    }

    public boolean isVoiceState() {
        int data = voiceState & 0xff;
        return (data & 128) == 128;
    }

    public int onVoiceClickData() {
        int data = voiceState & 0xff;
        return isVoiceState() ? (data & 127) : (data | 128);
    }


    public static byte getProgressState(byte data, int progress) {
        return (byte) (data & 240 | progress);
    }

    //不能直接 + progress，无限滑动进度条数据变动！！
    public static byte getProgressVoiceState(byte data, int progress) {
        return (byte) (data & 248 | progress);
    }
}
