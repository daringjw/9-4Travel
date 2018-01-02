package com.jkkc.travel.SOS;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.jkkc.travel.R;

/**
 * Created by Guan on 2017/7/17.
 */

public class SendSuccessDialog extends Dialog {

    Context context;
    private ImageView mIvDel;
    private Button mBtnCancelAlarm;


    public SendSuccessDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.context = context;
    }

    public SendSuccessDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_send_success);

        mIvDel = (ImageView) findViewById(R.id.ivDel);

        mIvDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
            }
        });

        mBtnCancelAlarm = (Button) findViewById(R.id.btnCancelAlarm);

        mBtnCancelAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //取消发送报警
                Toast.makeText(getContext(), "已经取消发送报警", Toast.LENGTH_SHORT).show();
                dismiss();

            }
        });


    }


}
