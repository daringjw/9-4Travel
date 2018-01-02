package com.jkkc.travel.listento1a.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jkkc.travel.R;
import com.jkkc.travel.bean.Spots;
import com.jkkc.travel.sweepcodebindlogin.PrefUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.jkkc.travel.R.id.ivPlay1;


/**
 * Created by Guan on 2017/6/21.
 */

public class AutoGuideFragment extends Fragment {

    @BindView(R.id.ivPlay1)
    ImageView mIvPlay1;
    @BindView(R.id.tvSpotsName1)
    TextView mTvSpotsName1;
    @BindView(R.id.tvUsedTime1)
    TextView mTvUsedTime1;
    @BindView(R.id.tvDistance1)
    TextView mTvDistance1;
    @BindView(R.id.rlItemAuto1)
    RelativeLayout mRlItemAuto1;
    @BindView(R.id.ivPlay2)
    ImageView mIvPlay2;
    @BindView(R.id.tvSpotsName2)
    TextView mTvSpotsName2;
    @BindView(R.id.tvUsedTime2)
    TextView mTvUsedTime2;
    @BindView(R.id.tvDistance2)
    TextView mTvDistance2;
    @BindView(R.id.rlItemAuto2)
    RelativeLayout mRlItemAuto2;
    @BindView(R.id.ivPlay3)
    ImageView mIvPlay3;
    @BindView(R.id.tvSpotsName3)
    TextView mTvSpotsName3;
    @BindView(R.id.tvUsedTime3)
    TextView mTvUsedTime3;
    @BindView(R.id.tvDistance3)
    TextView mTvDistance3;
    @BindView(R.id.rlItemAuto3)
    RelativeLayout mRlItemAuto3;
    @BindView(R.id.llAuto)
    LinearLayout mLlAuto;
    Unbinder unbinder;
    private MediaPlayer mMediaPlayer;
    private ArrayList<Spots> mSpotsList;
    private ListView mLvAuto;
    private String mSpotsName;
    private int mRawId;


    private Double mLat;
    private Double mLng;
    private ImageView mImg;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.ivPlay1, R.id.ivPlay2, R.id.ivPlay3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivPlay1:

                if (!isPlaying) {
                    mMediaPlayer = MediaPlayer.create(getActivity(),
                            mSpotsList.get(0).rawId);
                    mMediaPlayer.start();
                    isPlaying = true;
                    mIvPlay1.setImageResource(R.mipmap.stop);
                    mIvPlay2.setImageResource(R.mipmap.play);
                    mIvPlay3.setImageResource(R.mipmap.play);


                } else {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                    isPlaying = false;
                    mIvPlay1.setImageResource(R.mipmap.play);
                    mIvPlay2.setImageResource(R.mipmap.play);
                    mIvPlay3.setImageResource(R.mipmap.play);

                    mMediaPlayer = MediaPlayer.create(getActivity(),
                            mSpotsList.get(0).rawId);
                    mMediaPlayer.start();
                    isPlaying = true;
                    mIvPlay1.setImageResource(R.mipmap.stop);

                }


                break;
            case R.id.ivPlay2:

                if (!isPlaying) {
                    mMediaPlayer = MediaPlayer.create(getActivity(),
                            mSpotsList.get(1).rawId);
                    mMediaPlayer.start();
                    isPlaying = true;
                    mIvPlay1.setImageResource(R.mipmap.play);
                    mIvPlay2.setImageResource(R.mipmap.stop);
                    mIvPlay3.setImageResource(R.mipmap.play);
                } else {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                    isPlaying = false;
                    mIvPlay1.setImageResource(R.mipmap.play);
                    mIvPlay2.setImageResource(R.mipmap.play);
                    mIvPlay3.setImageResource(R.mipmap.play);

                    mMediaPlayer = MediaPlayer.create(getActivity(),
                            mSpotsList.get(1).rawId);
                    mMediaPlayer.start();
                    isPlaying = true;
                    mIvPlay1.setImageResource(R.mipmap.play);
                    mIvPlay2.setImageResource(R.mipmap.stop);
                    mIvPlay3.setImageResource(R.mipmap.play);

                }

                break;
            case R.id.ivPlay3:
                if (!isPlaying) {
                    mMediaPlayer = MediaPlayer.create(getActivity(),
                            mSpotsList.get(2).rawId);
                    mMediaPlayer.start();
                    isPlaying = true;
                    mIvPlay1.setImageResource(R.mipmap.play);
                    mIvPlay2.setImageResource(R.mipmap.play);
                    mIvPlay3.setImageResource(R.mipmap.stop);
                } else {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                    isPlaying = false;
                    mIvPlay1.setImageResource(R.mipmap.play);
                    mIvPlay2.setImageResource(R.mipmap.play);
                    mIvPlay3.setImageResource(R.mipmap.play);

                    mMediaPlayer = MediaPlayer.create(getActivity(),
                            mSpotsList.get(2).rawId);
                    mMediaPlayer.start();
                    isPlaying = true;
                    mIvPlay1.setImageResource(R.mipmap.play);
                    mIvPlay2.setImageResource(R.mipmap.play);
                    mIvPlay3.setImageResource(R.mipmap.stop);

                }

                break;
        }
    }


    public class SortComparator implements Comparator<Spots> {


        @Override
        public int compare(Spots s1, Spots s2) {

            return s1.dis - s2.dis;
        }


    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String lat1 = PrefUtils.getString(getActivity(), "lat", "");
        String lng1 = PrefUtils.getString(getActivity(), "lng", "");

        if (!TextUtils.isEmpty(lat1)) {

            mLat = Double.valueOf(lat1);
            mLng = Double.valueOf(lng1);

            initData();

        }


    }


    private void initData() {

        mSpotsList = new ArrayList<>();

        Spots spots0 = new Spots();
        spots0.spotsName = "协和门";
//        spots0.distance = "11km";
        spots0.rawId = R.raw.xiehemen;
        spots0.usedTime = "时长 2:08";
        spots0.lat = 39.9209578931;
        spots0.lng = 116.4049406839;
        double mDi = DistanceOfTwoPoints(mLat, mLng, spots0.lat, spots0.lng);
        //进行四舍五入操作：
        int mDis0 = Integer.parseInt(new DecimalFormat("0").format(mDi));
        spots0.dis = mDis0;
        double i = (double) mDis0 / 1000;
        DecimalFormat df = new DecimalFormat("#.00");
        String format = df.format(i);
        spots0.distance = format + "km";
        mSpotsList.add(spots0);

        Spots spots1 = new Spots();
        spots1.spotsName = "故宫";
//        spots1.distance = "12km";
        spots1.usedTime = "时长 4:47";
        spots1.rawId = R.raw.gugong;
        spots1.lat = 39.9217754649;
        spots1.lng = 116.4034636823;

        double mDi1 = DistanceOfTwoPoints(mLat, mLng, spots1.lat, spots1.lng);
        //进行四舍五入操作：
        int mDis1 = Integer.parseInt(new DecimalFormat("0").format(mDi1));
        spots1.dis = mDis1;
        double i1 = (double) mDis1 / 1000;
        df = new DecimalFormat("#.00");
        String format1 = df.format(i1);
        spots1.distance = format1 + "km";

        mSpotsList.add(spots1);


       /* Spots spots2 = new Spots();
        spots2.spotsName = "内金水河";
//        spots2.distance = "15km";
        spots2.usedTime = "时长 2:11";
        spots2.rawId = R.raw.neijinshuihe;
        spots2.lat = 39.9208078931;
        spots2.lng = 116.4036406839;

        double mDi2 = DistanceOfTwoPoints(mLat, mLng, spots2.lat, spots2.lng);
        //进行四舍五入操作：
        int mDis2 = Integer.parseInt(new DecimalFormat("0").format(mDi2));
        spots2.dis = mDis2;
        double i2 = (double) mDis2 / 1000;
        df = new DecimalFormat("#.00");
        String format2 = df.format(i2);
        spots2.distance = format2 + "km";

        mSpotsList.add(spots2);*/

        Spots spots3 = new Spots();
        spots3.spotsName = "太和门";
//        spots3.distance = "19km";
        spots3.usedTime = "时长 2:32";
        spots3.rawId = R.raw.taihemen;
        spots3.lat = 39.9217654649;
        spots3.lng = 116.4035136823;

        double mDi3 = DistanceOfTwoPoints(mLat, mLng, spots3.lat, spots3.lng);
        //进行四舍五入操作：
        int mDis3 = Integer.parseInt(new DecimalFormat("0").format(mDi3));
        spots3.dis = mDis3;
        double i3 = (double) mDis3 / 1000;
        df = new DecimalFormat("#.00");
        String format3 = df.format(i3);
        spots3.distance = format3 + "km";
        mSpotsList.add(spots3);

        Spots spots4 = new Spots();
        spots4.spotsName = "午门";
//        spots4.distance = "21km";
        spots4.usedTime = "时长 3:10";
        spots4.rawId = R.raw.wumen;
        spots4.lat = 39.9199478931;
        spots4.lng = 116.4036806839;

        double mDi4 = DistanceOfTwoPoints(mLat, mLng, spots4.lat, spots4.lng);
        //进行四舍五入操作：
        int mDis4 = Integer.parseInt(new DecimalFormat("0").format(mDi4));
        spots4.dis = mDis4;
        double i4 = (double) mDis4 / 1000;
        df = new DecimalFormat("#.00");
        String format4 = df.format(i4);
        spots4.distance = format4 + "km";
        mSpotsList.add(spots4);

        Spots spots5 = new Spots();
        spots5.spotsName = "内金水桥";
//        spots5.distance = "28km";
        spots5.usedTime = "时长 1:27";
        spots5.rawId = R.raw.neijinshuiqiao;
        spots5.lat = 39.9208078931;
        spots5.lng = 116.4036406839;

        double mDi5 = DistanceOfTwoPoints(mLat, mLng, spots5.lat, spots5.lng);
        //进行四舍五入操作：
        int mDis5 = Integer.parseInt(new DecimalFormat("0").format(mDi5));
        spots5.dis = mDis5;
        double i5 = (double) mDis5 / 1000;
        df = new DecimalFormat("#.00");
        String format5 = df.format(i5);
        spots5.distance = format5 + "km";
        mSpotsList.add(spots5);


        Comparator comp = new SortComparator();
        Collections.sort(mSpotsList, comp);

    }


    private boolean isPlaying = false;
    ViewHolder holder = null;

    private String mUsedTime;


    private static final double EARTH_RADIUS = 6378137;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 根据两点间经纬度坐标（double值），计算两点间距离，
     *
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return 距离：单位为米
     */
    public static double DistanceOfTwoPoints(double lat1, double lng1,
                                             double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }


    class AutoGuideAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return mSpotsList.size();
        }

        @Override
        public Spots getItem(int position) {
            return mSpotsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (mLvAuto == null) {
                mLvAuto = (ListView) parent;
            }

            if (convertView == null) {

                convertView = View.inflate(getActivity(), R.layout.item_auto, null);
                holder = new ViewHolder();
                holder.ivPlay = (ImageView) convertView.findViewById(ivPlay1);
                holder.tvSpotsName = (TextView) convertView.findViewById(R.id.tvSpotsName1);
                holder.tvUsedTime = (TextView) convertView.findViewById(R.id.tvUsedTime1);
                holder.tvDistance = (TextView) convertView.findViewById(R.id.tvDistance1);

                convertView.setTag(holder);

            } else {

                holder = (ViewHolder) convertView.getTag();

            }

            mRawId = mSpotsList.get(position).rawId;
            mUsedTime = mSpotsList.get(position).usedTime;

            holder.ivPlay.setTag(mRawId);
            holder.tvUsedTime.setTag(mUsedTime);

            holder.tvSpotsName.setText(mSpotsList.get(position).spotsName);
            holder.tvDistance.setText(mSpotsList.get(position).distance);
            holder.tvUsedTime.setText(mSpotsList.get(position).usedTime);


            holder.ivPlay.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    int mRawId = (int) v.getTag();

                    mImg = (ImageView) mLvAuto.findViewWithTag(mRawId);

                    if (isPlaying) {

                        //停止
                        mImg.setImageResource(R.mipmap.play);
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                        isPlaying = false;


                    } else {

                        //播放
                        mImg.setImageResource(R.mipmap.stop);
                        mMediaPlayer = MediaPlayer.create(getActivity(),
                                mSpotsList.get(position).rawId);
                        mMediaPlayer.start();
                        isPlaying = true;


                    }

                    /*if (mMediaPlayer == null) {

                        mMediaPlayer = MediaPlayer.create(getActivity(),
                                mSpotsList.get(position).rawId);
                        mMediaPlayer.start();

                        isPlaying = true;
                        mImg.setImageResource(R.mipmap.stop);


                    } else {

                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                        mMediaPlayer = null;

                        isPlaying = false;

                        mImg.setImageResource(R.mipmap.play);

                    }*/


                }

            });


            return convertView;
        }


    }


    /**
     * @param
     * @return 该毫秒数转换为 * days * hours * minutes * seconds 后的格式
     * @author fy.zhang
     */
    public static String formatDuring(long mss) {
        long days = mss / (1000 * 60 * 60 * 24);
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        return days + " days " + hours + " hours " + minutes + " minutes "
                + seconds + " seconds ";

    }

    public static String formatDuring1(long mss) {
        long days = mss / (1000 * 60 * 60 * 24);
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        return minutes + " : "
                + seconds;

    }


    static class ViewHolder {

        ImageView ivPlay;
        TextView tvSpotsName;
        TextView tvUsedTime;
        TextView tvDistance;

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_auto, container, false);



       /* mLvAuto = (ListView) view.findViewById(R.id.lvAuto);

        mLvAuto.setDivider(null);

        if (mSpotsList != null) {

            mLvAuto.setAdapter(new AutoGuideAdapter());


        }*/


        unbinder = ButterKnife.bind(this, view);

        mTvSpotsName1.setText(mSpotsList.get(0).spotsName);
        mTvUsedTime1.setText("时长 " + mSpotsList.get(0).usedTime);
        mTvDistance1.setText(mSpotsList.get(0).distance);

        mTvSpotsName2.setText(mSpotsList.get(1).spotsName);
        mTvUsedTime2.setText("时长 " + mSpotsList.get(1).usedTime);
        mTvDistance2.setText(mSpotsList.get(1).distance);

        mTvSpotsName3.setText(mSpotsList.get(2).spotsName);
        mTvUsedTime3.setText("时长 " + mSpotsList.get(2).usedTime);
        mTvDistance3.setText(mSpotsList.get(2).distance);


        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    @Override
    public void onDestroy() {

        if (mMediaPlayer != null) {

            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            isPlaying = false;

        }

        super.onDestroy();


    }
}
