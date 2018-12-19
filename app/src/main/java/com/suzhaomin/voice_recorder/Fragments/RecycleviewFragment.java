package com.suzhaomin.voice_recorder.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.suzhaomin.voice_recorder.Activity.MainActivity;
import com.suzhaomin.voice_recorder.Adapters.FileViewerAdapter;
import com.suzhaomin.voice_recorder.DBHelper;
import com.suzhaomin.voice_recorder.Listeners.OnDatabaseChangedListener;
import com.suzhaomin.voice_recorder.R;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.suzhaomin.voice_recorder.Adapters.FileViewerAdapter.isDeletemodel;
import static com.suzhaomin.voice_recorder.Adapters.FileViewerAdapter.isShowCheckBox;
import static com.suzhaomin.voice_recorder.Adapters.FileViewerAdapter.map;
import static com.suzhaomin.voice_recorder.Adapters.FileViewerAdapter.setDeletemodel;
import static com.suzhaomin.voice_recorder.Adapters.FileViewerAdapter.setShowCheckBox;


public class RecycleviewFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = "FileViewerFragment";

    private int position;
    private FileViewerAdapter mFileViewerAdapter;
    private RecyclerView mRecyclerView;

    public static boolean isopened=false;
    public static RecycleviewFragment newInstance(int position) {
        RecycleviewFragment f = new RecycleviewFragment();
        Bundle b = new Bundle();
        position = 1;
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        position = getArguments().getInt("1");
        //打开observer的文件监控
        observer.startWatching();
        setHasOptionsMenu(true);
        isopened=true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isopened=true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isopened=false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isopened=false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());

        //确保尺寸是通过用户输入从而确保RecyclerView的尺寸是一个常数
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        //新添加的在下面，当超出屏幕范围的时候吧上面的顶上去
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mFileViewerAdapter = new FileViewerAdapter(getActivity(), llm);
        mRecyclerView.setAdapter(mFileViewerAdapter);

        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle presses on the action bar items

        switch (item.getItemId()) {
            case R.id.action_back:
                Log.v("asf", "i am coming");
                if (isShowCheckBox())
                    setShowCheckBox(false);
                if (isDeletemodel())
                    setDeletemodel(false);
                mFileViewerAdapter.notifyDataSetChanged();
                //将键值对进行初始化
                map = new HashMap<>();
                for (int i = 0; i < 1000; i++)
                    map.put(i, false);
                return true;
//            case R.id.action_share:
//
//                return true;
            case R.id.action_delete:
                if (!isDeletemodel()) {
                    //如果不是删除模式，那么显示出取消按钮和复选框
                    Log.v("Agasfga","非删除模式");
                    setDeletemodel(true);
                    for (int i = 0; i < 1000; i++)
                        map.put(i, false);
                    mFileViewerAdapter.notifyDataSetChanged();

                }
               else {
                    //如果是删除模式，进行删除，然后初始化
                    Log.v("Agasfga","删除模式");
                    int deletecenter=0;
                    Set<Map.Entry<Integer, Boolean>> entries = map.entrySet();
                    for (Map.Entry<Integer, Boolean> entry : entries) {
                        if (entry.getValue()) {
                            Log.v("Agasfga", "删除positon" + entry.getKey() + "");
                            mFileViewerAdapter.remove(entry.getKey()-deletecenter);
                            deletecenter++;
                        }
                    }
                    //将键值对进行初始化
                    for (int i = 0; i < 1000; i++)
                        map.put(i, false);
                    setDeletemodel(false);
                    setShowCheckBox(false);
                    mFileViewerAdapter.notifyDataSetChanged();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void specialUpdate() {
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                mFileViewerAdapter.notifyDataSetChanged();
            }
        };
        handler.post(r);
    }

    //FileObserver是一个监听文件的创建、删除、更改，移动等操作的抽象类
    //小tips：手机内部内置有一个sd卡，当你的apk装在内置还是额外的就读取那个
    FileObserver observer =
            new FileObserver(
                    //检索全部以SoundRecorder结尾的文件
                    android.os.Environment.getExternalStorageDirectory().toString()
                            + "/SoundRecorder") {
                // 设置文件监听器observer
                @Override
                public void onEvent(int event, String file) {
                    if (event == FileObserver.DELETE) {
                        //当该文件被删除的时候

                        String filePath = android.os.Environment.getExternalStorageDirectory().toString()
                                + "/SoundRecorder" + file + "]";

                        Log.d(LOG_TAG, "File deleted ["
                                + android.os.Environment.getExternalStorageDirectory().toString()
                                + "/SoundRecorder" + file + "]");

                        // 删除该文件并更新视图
                        mFileViewerAdapter.removeOutOfApp(filePath);
                    }
                }
            };

}


