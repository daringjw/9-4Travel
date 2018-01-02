package com.jkkc.travel.bean;

/**
 * Created by Guan on 2017/7/24.
 */

public class UpdateInfo {


    /**
     * target_size : 13.74M
     * update : Yes
     * constraint : false
     * new_version : 1.1.11
     * apk_file_url : https://raw.githubusercontent.com/daringjw/updateapk/master/com.jkkc.travel.apk
     * new_md5 : A818AD325EACC199BC62C552A32C35F2
     * update_log : 1，金坤科创研发
     */
    private String target_size;
    private String update;
    private boolean constraint;
    private String new_version;
    private String apk_file_url;
    private String new_md5;
    private String update_log;

    public void setTarget_size(String target_size) {
        this.target_size = target_size;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public void setConstraint(boolean constraint) {
        this.constraint = constraint;
    }

    public void setNew_version(String new_version) {
        this.new_version = new_version;
    }

    public void setApk_file_url(String apk_file_url) {
        this.apk_file_url = apk_file_url;
    }

    public void setNew_md5(String new_md5) {
        this.new_md5 = new_md5;
    }

    public void setUpdate_log(String update_log) {
        this.update_log = update_log;
    }

    public String getTarget_size() {
        return target_size;
    }

    public String getUpdate() {
        return update;
    }

    public boolean isConstraint() {
        return constraint;
    }

    public String getNew_version() {
        return new_version;
    }

    public String getApk_file_url() {
        return apk_file_url;
    }

    public String getNew_md5() {
        return new_md5;
    }

    public String getUpdate_log() {
        return update_log;
    }
}
