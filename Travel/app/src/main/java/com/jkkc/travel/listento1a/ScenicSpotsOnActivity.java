package com.jkkc.travel.listento1a;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.jkkc.travel.R;
import com.jkkc.travel.listento1a.fragment.AutoGuideFragment;
import com.jkkc.travel.listento1a.fragment.GuideFragment;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Guan on 2017/6/21.
 */

public class ScenicSpotsOnActivity extends AppCompatActivity {

    private TabLayout mTablayout;
    private ViewPager mViewPager;
    private TabLayout.Tab one;
    private TabLayout.Tab two;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenic_spots_on);

        initViews();
        initEvents();
    }


    private void initEvents() {

        mTablayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab == mTablayout.getTabAt(0)) {

                    mViewPager.setCurrentItem(0);

                } else if (tab == mTablayout.getTabAt(1)) {

                    mViewPager.setCurrentItem(1);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab == mTablayout.getTabAt(0)) {


                } else if (tab == mTablayout.getTabAt(1)) {


                }


            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        ImageView btnBack = (ImageView) findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private void initViews() {

        mTablayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);

        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

            private String[] mTitles = new String[]{"导游讲解", "自助讲解"};

            @Override
            public Fragment getItem(int position) {
                if (position == 1) {
                    return new AutoGuideFragment();
                }
                return new GuideFragment();
            }

            @Override
            public int getCount() {
                return mTitles.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTitles[position];
            }

        });

        mTablayout.setupWithViewPager(mViewPager);

        one = mTablayout.getTabAt(0);
        two = mTablayout.getTabAt(1);


//        one.setIcon(getResources().getDrawable(R.mipmap.ic_launcher));
//        two.setIcon(getResources().getDrawable(R.mipmap.ic_launcher));


    }


}
