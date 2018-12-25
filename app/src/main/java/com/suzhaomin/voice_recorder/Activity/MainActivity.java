package com.suzhaomin.voice_recorder.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.suzhaomin.voice_recorder.Adapters.FileViewerAdapter;
import com.suzhaomin.voice_recorder.Fragments.RecorderFragment;
import com.suzhaomin.voice_recorder.Fragments.RecycleviewFragment;
import com.suzhaomin.voice_recorder.Fragments.speech_recognition;
import com.suzhaomin.voice_recorder.R;
import com.suzhaomin.voice_recorder.Adapters.TabPagerAdapter;

import static com.suzhaomin.voice_recorder.Adapters.FileViewerAdapter.isIsopened;
import static com.suzhaomin.voice_recorder.Adapters.FileViewerAdapter.isShowCheckBox;
import static com.suzhaomin.voice_recorder.Adapters.FileViewerAdapter.setShowCheckBox;
import static com.suzhaomin.voice_recorder.Fragments.RecycleviewFragment.isopened;

public class MainActivity extends AppCompatActivity {

    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PagerSlidingTabStrip tabStrip = findViewById(R.id.tab);
        // 设置Tab底部选中的指示器 Indicator的颜色
        tabStrip.setIndicatorColor(Color.GRAY);
        tabStrip.setIndicatorHeight(1);
        //设置Tab标题文字的颜色
        tabStrip.setTextColor(Color.BLACK);
        // 设置Tab标题文字的大小
        tabStrip.setTextSize(32);
        //设置Tab底部分割线的颜色
        tabStrip.setUnderlineColor(Color.TRANSPARENT);
        // 设置点击某个Tab时的背景色,设置为0时取消背景色
        tabStrip.setTabBackground(0);
        // 设置Tab是自动填充满屏幕的
        tabStrip.setShouldExpand(true);
        //!!!设置选中的Tab文字的颜色!!!
//        tabStrip.setSelectedTextColor(Color.GREEN);
        //tab间的分割线
        tabStrip.setDividerColor(Color.GRAY);
        //底部横线与字体宽度一致
//        tabStrip.setIndicatorinFollower(true);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        Fragment[] fragments = {new RecorderFragment(), new RecycleviewFragment(),new speech_recognition()};
        String[] titles = {"录音", "列表","识别"};
        TabPagerAdapter adapter = new TabPagerAdapter(getSupportFragmentManager(), fragments, titles);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.inflateMenu(R.menu.menu);
        }
        viewPager.setAdapter(adapter);
        tabStrip.setViewPager(viewPager);
        initPhotoError();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_delete).setVisible(isIsopened());
        menu.findItem(R.id.action_back).setVisible(isShowCheckBox());
        invalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }


    private void initPhotoError() {
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }
}
