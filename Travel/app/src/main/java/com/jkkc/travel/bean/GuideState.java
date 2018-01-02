package com.jkkc.travel.bean;

/**
 * Created by Guan on 2017/6/23.
 */

public class GuideState {

    public String startTime;

    public String state; // 导游正在讲解中

    public String  usedTime;

    public Integer  anniu;

    public GuideState() {


    }

    public GuideState(String startTime, String state, String usedTime, Integer anniu) {
        this.startTime = startTime;
        this.state = state;
        this.usedTime = usedTime;
        this.anniu = anniu;
    }


}
