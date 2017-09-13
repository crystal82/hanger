package com.uascent.jz.ua420r.timer;

import java.io.Serializable;

/**
 * Created by maxiao on 2017/7/4.
 */

public class Timer implements Serializable {
    private String timerId;
    private String timerZone;
    private String action;
    private String timerAction;
    private String weekFlag;
    private String deltaTime;
    private String time;

    public String getTimerId() {
        return timerId;
    }

    public void setTimerId(String timerId) {
        this.timerId = timerId;
    }

    public String getTimerZone() {
        return timerZone;
    }

    public void setTimerZone(String timerZone) {
        this.timerZone = timerZone;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTimerAction() {
        return timerAction;
    }

    public void setTimerAction(String timerAction) {
        this.timerAction = timerAction;
    }

    public String getWeekFlag() {
        return weekFlag;
    }

    public void setWeekFlag(String weekFlag) {
        this.weekFlag = weekFlag;
    }

    public String getDeltaTime() {
        return deltaTime;
    }

    public void setDeltaTime(String deltaTime) {
        this.deltaTime = deltaTime;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String toString() {

        return "timerId=" + timerId + " timerZone=" + timerZone + " timerAction=" + timerAction + " weekFlag=" + weekFlag + " deltaTime=" + deltaTime+" time=" + time;
    }
}
