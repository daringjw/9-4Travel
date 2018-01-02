package com.jkkc.travel.listento1a.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jkkc.travel.R;
import com.jkkc.travel.bean.GuideState;
import com.jkkc.travel.listento1a.GuideSpeak;
import com.jkkc.travel.utils.PrefUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Guan on 2017/6/21.
 */

public class GuideFragment extends Fragment {


    private static final String TAG = "GuideFragment";
    private Button mBtnReceive;

    private View mView;
    private ListView mLvGuide;

    private List<GuideState> mGuideStateList;
    private GuideAdapter mGuideAdapter;
    private String mUsedTime;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGuideStateList = new ArrayList<>();

        initData();

        if (mGuideAdapter != null) {

            mGuideAdapter.notifyDataSetChanged();//有新消息时，刷新ListView中的显示
            mLvGuide.setSelection(mGuideStateList.size() - 1);

        }



    }


    /**
     * 得到现在时间
     *
     * @return 字符串 yyyyMMdd HHmmss
     */
    public static String getStringToday() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm");
        String dateString = formatter.format(currentTime);
        return dateString;
    }


    private void initData() {

        GuideState mGuideState = new GuideState();


        mGuideState.startTime = getStringToday();
        mGuideState.state = "导游正在讲解中";
        mGuideState.anniu = 1;
        mGuideStateList.add(mGuideState);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {

            mUsedTime = data.getStringExtra("usedTime");
            Log.e(TAG, mUsedTime);

        }

        mGuideAdapter.notifyDataSetChanged();

    }

    GuideViewHolder holder = null;

    class GuideAdapter extends BaseAdapter {


        @Override
        public int getCount() {

            return mGuideStateList.size();
        }

        @Override
        public GuideState getItem(int position) {


            return mGuideStateList.get(position);
        }

        @Override
        public long getItemId(int position) {


            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {


            if (convertView == null) {

                convertView = View.inflate(getActivity(), R.layout.item_guide, null);
                holder = new GuideViewHolder();
                holder.tvStartTime = (TextView) convertView.findViewById(R.id.tvStartTime);
                holder.tvGuideState = (TextView) convertView.findViewById(R.id.tvGuideState);
                holder.tvGuideUsedTime = (TextView) convertView.findViewById(R.id.tvGuideUsedTime);
                holder.ivGuidePlay = (Button) convertView.findViewById(R.id.ivGuidePlay);
                holder.llGuideBg = (LinearLayout) convertView.findViewById(R.id.llGuideBg);

                convertView.setTag(holder);


            } else {
                holder = (GuideViewHolder) convertView.getTag();

            }

            holder.tvStartTime.setText(mGuideStateList.get(position).startTime);
            holder.tvGuideState.setText(mGuideStateList.get(position).state);

            if (mGuideStateList.get(position).anniu == 1) {

                holder.ivGuidePlay.setVisibility(View.VISIBLE);
                holder.tvGuideUsedTime.setVisibility(View.GONE);
                holder.llGuideBg.setBackgroundResource(R.mipmap.guide_speaking);


                holder.ivGuidePlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        GuideState guideState = new GuideState();
                        guideState.startTime = getStringToday();
                        guideState.state = "导游正在讲解中";
                        guideState.anniu = 1;
                        mGuideStateList.add(guideState);


                        mGuideStateList.get(position).state = "导游讲解已结束";
                        mGuideStateList.get(position).anniu = 0;
                        String usedTime = PrefUtils.getString(getActivity(), "usedTime", "");
                        if (!TextUtils.isEmpty(usedTime)) {
                            mGuideStateList.get(position).usedTime = usedTime;
                        } else {
                            mGuideStateList.get(position).usedTime = "0分1秒";
                        }
                        mGuideAdapter.notifyDataSetChanged();
                        mLvGuide.setSelection(mGuideStateList.size() - 1);


                        startActivityForResult(new Intent(getActivity(),
                                GuideSpeak.class), 1);


                    }
                });


            } else {

                holder.ivGuidePlay.setVisibility(View.GONE);
                holder.tvGuideUsedTime.setText("用时" + mGuideStateList.get(position).usedTime);
                holder.tvGuideUsedTime.setVisibility(View.VISIBLE);
                holder.llGuideBg.setBackgroundResource(R.mipmap.guide_speak_end);

            }


            return convertView;
        }
    }


    static class GuideViewHolder {

        TextView tvStartTime;
        TextView tvGuideState;
        TextView tvGuideUsedTime;

        Button ivGuidePlay;
        LinearLayout llGuideBg;


    }

    @Override
    public void onDestroy() {

        PrefUtils.setString(getActivity(), "usedTime", "");
        super.onDestroy();

    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_guide, container, false);

        mLvGuide = (ListView) mView.findViewById(R.id.lvGuide);

        mGuideAdapter = new GuideAdapter();
        mLvGuide.setAdapter(mGuideAdapter);


        return mView;


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



    }


}
