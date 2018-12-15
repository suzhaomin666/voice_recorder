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

    private TimerTask mIncrementTimerTask = null;

    public IBinder onBind(Intent intent) {
        return new RecordingServiceBinder();
    }
    public class RecordingServiceBinder extends Binder {
        public RecordingServiceBinder(){

        }
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

            //startTimer();
            //startForeground(1, createNotification());

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }
    public void pauseRecording(){
        //暂停录音
        mRecorder.pause();
    }

    public void resumeRecording(){
        //继续录音
        mRecorder.resume();
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
