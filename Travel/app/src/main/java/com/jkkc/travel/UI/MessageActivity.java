package com.jkkc.travel.UI;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jkkc.travel.R;
import com.jkkc.travel.bean.NewsBean;
import com.jkkc.travel.db.NewsHelper;
import com.umeng.analytics.MobclickAgent;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by Xxyou on 2017/7/3.
 */

public class MessageActivity extends AppCompatActivity {
    @BindView(R.id.recycle_news)
    RecyclerView mRecycleNews;
    private List<NewsBean> mNewsBeen = new ArrayList<>();
    private CommonAdapter<NewsBean> mStringCommonAdapter;
    private TextView tvClear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        ButterKnife.bind(this);

        Observable.create(new Observable.OnSubscribe<List<NewsBean>>() {
            @Override
            public void call(Subscriber<? super List<NewsBean>> subscriber) {
                mNewsBeen = NewsHelper.getDBHelper().queryAll();
                Collections.reverse(mNewsBeen);
                subscriber.onNext(mNewsBeen);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<NewsBean>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("MessageActivity.this: ", "onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(List<NewsBean> newsBeen) {
                        initRecycle();
                    }
                });


        NewsHelper.getDBHelper().updateRead();


        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        tvClear = (TextView) findViewById(R.id.tvClear);
        tvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NewsHelper.getDBHelper().delAll();
                recreate();

            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void initRecycle() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MessageActivity.this);
        mRecycleNews.setLayoutManager(linearLayoutManager);
        mStringCommonAdapter = new CommonAdapter<NewsBean>(MessageActivity.this,
                R.layout.item_notification, mNewsBeen) {
            @Override
            protected void convert(ViewHolder holder, NewsBean newsBean, int position) {
                if (newsBean.getLevel().equals(" 3")) {
                    holder.setImageResource(R.id.img_level, R.mipmap.img_no_normal);
                    holder.setBackgroundRes(R.id.layout_top, R.drawable.normal_bg);
                    holder.setText(R.id.tv_no_level, "系统消息");
                } else if (newsBean.getLevel().equals(" 4")) {
                    holder.setImageResource(R.id.img_level, R.mipmap.img_no_primarily);
                    holder.setBackgroundRes(R.id.layout_top, R.drawable.primaily_bg);
                    holder.setText(R.id.tv_no_level, "重要消息");
                } else if (newsBean.getLevel().equals(" 5")) {
                    holder.setImageResource(R.id.img_level, R.mipmap.img_no_urgency);
                    holder.setBackgroundRes(R.id.layout_top, R.drawable.urgency_bg);
                    holder.setText(R.id.tv_no_level, "紧急消息");
                }else if (newsBean.getLevel().equals("")){
                    holder.setImageResource(R.id.img_level, R.mipmap.img_no_normal);
                    holder.setBackgroundRes(R.id.layout_top, R.drawable.normal_bg);
                    holder.setText(R.id.tv_no_level, "空消息");
                }
                holder.setText(R.id.notification_time, newsBean.getTime());
                holder.setText(R.id.notification_title, newsBean.getTitle());
                holder.setText(R.id.notification_content, "\u3000\u3000" + newsBean.getContent());
                holder.setText(R.id.tv_no_city, newsBean.getCity());
            }
        };
        mRecycleNews.setAdapter(mStringCommonAdapter);
    }
}
