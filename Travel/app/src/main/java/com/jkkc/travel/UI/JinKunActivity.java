package com.jkkc.travel.UI;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.jkkc.travel.R;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Guan on 2017/7/13.
 */

public class JinKunActivity extends Activity {

    @BindView(R.id.btnBack)
    ImageView mBtnBack;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_jinkun);
        ButterKnife.bind(this);


    }


    @OnClick(R.id.btnBack)
    public void onViewClicked() {

        finish();

    }
}
