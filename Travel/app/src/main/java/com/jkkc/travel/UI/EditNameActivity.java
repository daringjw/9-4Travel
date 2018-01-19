package com.jkkc.travel.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;

import com.jkkc.travel.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Guan on 2018/1/18.
 */

public class EditNameActivity extends Activity {

    @BindView(R.id.etUserName)
    EditText mEtUserName;
    @BindView(R.id.btnConfirm)
    Button mBtnConfirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_name);
        ButterKnife.bind(this);


    }


    @OnClick(R.id.btnConfirm)
    public void onViewClicked() {

        Intent intent = new Intent(getApplicationContext(),
                HomeActivity.class);

        intent.putExtra("name",
                mEtUserName.getText().toString().trim());
        startActivity(intent);

        finish();

    }
}
