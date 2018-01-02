package com.jkkc.travel.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by Xxyou on 2017/6/9.
 */

@Entity
public class NewsBean {
    @Id
    private String uuid;
    private String time;
    private String content;
    private String title;
    private String isRead;
    private String city;
    private String level;

    @Override
    public String toString() {
        return "NewsBean{" +
                "uuid='" + uuid + '\'' +
                ", time='" + time + '\'' +
                ", content='" + content + '\'' +
                ", title='" + title + '\'' +
                ", isRead='" + isRead + '\'' +
                '}';
    }

    @Generated(hash = 1821853681)
    public NewsBean(String uuid, String time, String content, String title,
                    String isRead, String city, String level) {
        this.uuid = uuid;
        this.time = time;
        this.content = content;
        this.title = title;
        this.isRead = isRead;
        this.city = city;
        this.level = level;
    }

    @Generated(hash = 1662878226)
    public NewsBean() {
    }
    public String getUuid() {
        return this.uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getTime() {
        return this.time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getIsRead() {
        return this.isRead;
    }
    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }



    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLevel() {
        return this.level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

 
}
