package com.jkkc.travel.listento1a.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Guan on 2017/3/30.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "guide.db"; //数据库名称
    private static final int version = 1; //数据库版本

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, version);

        // TODO Auto-generated constructor stub


    }

    @Override
    public void onCreate(SQLiteDatabase db) {


//        "create table info (_id integer primary key autoincrement,name varchar(20),phone varchar(11))"

        String sql = "create table guide(_id integer primary key autoincrement," +
                " startTime varchar(100)  ," +
                " state varchar(200) ," +
                "anniu integer(10)   ," +
                " usedTime varchar(200) );";

        db.execSQL(sql);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }


}
