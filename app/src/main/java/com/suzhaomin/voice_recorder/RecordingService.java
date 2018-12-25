package com.suzhaomin.voice_recorder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.TimerTask;

public class RecordingService extends Service {
    private static final String LOG_TAG = "RecordingService";
    //表示录音文件的名字
    private String mFileName = null;
    //表示录音文件的存储路径
    private String mFilePath = null;
    //调用MediaRecorder类
    private MediaRecorder mRecorder = null;
    //调用DBHelper类
    private DBHelper mDatabase;
    //表示开始录音的时间
    private long mStartingTimeMillis = 0;
    //表示录音消耗的时间
    private long mElapsedMillis = 0;

    private Thread thread;
    public volumcallback mCallback;


    private TimerTask mIncrementTimerTask = null;

    public IBinder onBind(Intent intent) {
        return new RecordingServiceBinder();
    }

    public class RecordingServiceBinder extends Binder {
        public RecordingService getService(){
            return RecordingService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
        //生成DBHelper对象
        mDatabase = new DBHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            //血淋淋的教训，搞了一个上午，一定先tm关了线程再回收资源
            thread.interrupt();
            thread=null;
            stopRecording();
        }

        super.onDestroy();
    }

    public void setFileNameAndPath(){
        //设置录音文件的文件名和存储路径
        int count = 0;
        File f;

        do{
            count++;

            mFileName = getString(com.suzhaomin.voice_recorder.R.string.default_file_name)
                    + "_" + (mDatabase.getCount() + count) + ".mp4";
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/SoundRecorder/" + mFileName;

            f = new File(mFilePath);
        }while (f.exists() && !f.isDirectory());
    }

    public void registerCallback(volumcallback paramICallback) {
        this.mCallback = paramICallback;
    }

    public void startRecording() {
        setFileNameAndPath();
        //在录音之前，初始化MediaRecorder类
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);

        try {
            //开始录音
            mRecorder.prepare();
            mRecorder.start();
            //获取当前系统时间来作为开始录音的时间
            mStartingTimeMillis = System.currentTimeMillis();
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(thread!=null)
                            startListenAudio();
                    }
                });
                thread.start();
                thread.sleep(100);

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startListenAudio() {
                if (mRecorder == null&&thread==null) return;
                double ratio = (double) mRecorder.getMaxAmplitude() / 100;
                int db = 0;// 分贝
                //默认的最大音量是100,可以修改，但其实默认的，在测试过程中就有不错的表现
                //你可以传自定义的数字进去，但需要在一定的范围内，比如0-200，就需要在xml文件中配置maxVolume
                //同时，也可以配置灵敏度sensibility
                if (ratio > 1)
                    db = (int) (20 * Math.log10(ratio));
                if (mCallback != null) {
                    Log.i("BindService", "数据更新");
                    mCallback.updatevolum(db);
                } else {
                    Log.v(LOG_TAG, "mcall is null");
                }
            }
    public void stopRecording() {
        //停止录音
        mRecorder.stop();
        //计算录音消耗的时间
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        //释放录音对象
        mRecorder.release();
        mRecorder = null;
        try {
            //把录音文件的信息存储到数据库
            mDatabase.addRecording(mFileName, mFilePath, mElapsedMillis);
        } catch (Exception e){
            Log.e(LOG_TAG, "exception", e);
        }
    }
}