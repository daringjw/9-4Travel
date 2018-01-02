package com.jkkc.travel.systemmanagerment.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.jkkc.travel.R;


/**
 * Created by Guan on 2017/4/2.
 */

public class LoadingDialog {

    private Context mContext;

    private View mDialogView;
    private TextView mTxtMsg;

    private Dialog mDialog;

    public LoadingDialog(Context context) {

        this.mContext = context;

        mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);

        mTxtMsg = (TextView) mDialogView.findViewById(R.id.txtMsg);

        initDialog();

    }

    private void initDialog() {

        mDialog = new Dialog(mContext,R.style.dialog);

        mDialog.setContentView(mDialogView);
//        mDialog.setCanceledOnTouchOutside(true);


    }

    public void setMessage(CharSequence msg) {

        mTxtMsg.setText(msg);

    }

    public void setMessage(int msg) {

        mTxtMsg.setText(msg);

    }

    public void show() {

        if (mDialog != null) {
            mDialog.show();
        }

    }

    public void dismiss() {

        if (mDialog != null) {

            mDialog.dismiss();
        }
    }


}
