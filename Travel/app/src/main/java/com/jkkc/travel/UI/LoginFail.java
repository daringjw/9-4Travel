package com.jkkc.travel.UI;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;

import com.jkkc.travel.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Guan on 2017/7/15.
 */

public class LoginFail extends Activity {

    @BindView(R.id.btnBack)
    Button mBtnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login_fail);
        ButterKnife.bind(this);


    }

    @OnClick(R.id.btnBack)
    public void onViewClicked() {


        finish();

    }
}
