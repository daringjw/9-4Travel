package com.jkkc.travel.db;


import android.content.Context;
import android.util.Log;

import com.jkkc.travel.bean.NewsBean;
import com.jkkc.travel.bean.NewsBeanDao;
import com.jkkc.travel.global.DemoApplication;

import java.util.List;

/**
 * Created by Xxyou on 2017/5/20.
 */

public class NewsHelper {
    private static NewsBeanDao sNewsBeanDao;
    private static NewsHelper sNewsHelper;

    public static NewsHelper getDBHelper() {
        if (sNewsHelper == null) {
            sNewsHelper = new NewsHelper();
            sNewsBeanDao = DemoApplication.getDaoSession().getNewsBeanDao();
        }
        return sNewsHelper;

    }

    /**
     * 新插入数据
     *
     * @param chatUser
     */
    public void insert(NewsBean chatUser, Context context) {
        Log.e("context: ", "insert: " + context);
        sNewsBeanDao.insert(chatUser);
    }

    /**
     * 查询所有数据
     *
     * @return
     */
    public List<NewsBean> queryAll() {
        return sNewsBeanDao.loadAll();
    }

    public int queryNotRead() {
        return sNewsBeanDao.queryBuilder().where(NewsBeanDao.Properties.IsRead.eq("0")).build().list().size();
    }

    public void updateRead() {
        List<NewsBean> newsBeen = sNewsBeanDao.queryBuilder()
                .where(NewsBeanDao.Properties.IsRead.eq("0")).build().list();

        for (int i= 0;i<newsBeen.size();i++) {
            NewsBean newsBean = newsBeen.get(i);
            newsBean.setIsRead("1");
            sNewsBeanDao.update(newsBean);
        }

    }

    public void delAll(){
        sNewsBeanDao.deleteAll();
    }

    public void delByBean(NewsBean newsBeen){
//        NewsBean newsBeen = sNewsBeanDao.queryBuilder()
// .where(NewsBeanDao.Properties.Uuid.eq(uuid)).build().unique();
        sNewsBeanDao.delete(newsBeen);
    }
}
