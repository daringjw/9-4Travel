package com.jkkc.travel.listento1a.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.jkkc.travel.bean.GuideState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guan on 2017/4/8.
 */
public class GuideStateManager {

    private DatabaseHelper databaseHelper;

    public GuideStateManager(Context context) {
        //创建一个帮助类对象
        databaseHelper = new DatabaseHelper(context);


    }

    public boolean add(GuideState msg) {

        //执行sql语句需要sqliteDatabase对象
        //调用getReadableDatabase方法,来初始化数据库的创建
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        //sql:sql语句，  bindArgs：sql语句中占位符的值
//        db.execSQL("insert into chat_msg(content,type,time) values(?,?);", new Object[]{msg.getContent(), msg.getType(),msg.time});
        ContentValues values = new ContentValues();
        values.put("startTime", msg.startTime);
        values.put("state", msg.state);
        values.put("anniu", msg.anniu);
        values.put("usedTime", msg.usedTime);


        long result = db.insert("guide", null, values);
        //关闭数据库对象
        db.close();

        if (result != -1) {//-1代表添加失败
            return true;
        } else {
            return false;
        }

    }


    /**
     * @return
     */
    public List<GuideState> findAll() {

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        Cursor cursor = db.query("guide", new String[]{"startTime", "state", "anniu", "usedTime"},
                null, null, null, null, "_id desc");

        List<GuideState> guideStateList = new ArrayList<GuideState>();

        while (cursor.moveToNext()) {

            GuideState msg = new GuideState();
            msg.startTime = cursor.getString(0);
            msg.state = cursor.getString(1);
            msg.anniu = cursor.getInt(2);
            msg.usedTime = cursor.getString(3);

            guideStateList.add(msg);

        }

        cursor.close();
        db.close();

        return guideStateList;

    }


}
