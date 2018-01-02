package com.jkkc.travel.SOS;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.jkkc.travel.R;

/**
 * Created by Guan on 2017/7/17.
 */

public class SosNetDialog extends Dialog {

    Context context;

    private ImageView mIvDel;
    private Button btnConfirm;

    public SosNetDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.context = context;
    }

    public SosNetDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_sos_net_fail);

        mIvDel = (ImageView) findViewById(R.id.ivDel);

        mIvDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
            }
        });

        btnConfirm = (Button) findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                dismiss();

            }
        });


    }


}
